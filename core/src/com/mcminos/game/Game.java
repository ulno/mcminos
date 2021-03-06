package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.util.ObjectMap;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by ulno on 27.08.15.
 * <p/>
 * This is the class having all game content which needs to be accessed by all other modules.
 * It also manages the game content but nothing graphical.
 * Graphical things like projection or coordinates would go to playwindow or the PlayScreen.
 * The timing of the game is done here.
 * Audio events can be (still) triggered from here.
 * Also the toolbox needs to be controlled from outside and doesn't belong in here.
 */
public class Game {
    // constants
    public static final int timeResolution = 120; // How often per second movements are updated? TODO: if better 128 aor 120 against stuttering
    //public static final int timeResolutionExponent = Util.log2binary(timeResolution);
    public static final int baseSpeed = 2; // base speed of all units (kind of the slowest anybody usually moves) in blocks per second
    public static final int timeResolutionSquare = timeResolution * timeResolution; // someimes needed for precision
    private Main main;
    private final Play playScreen;
    private final Audio audio;
    private Level level;
    private McMinos mcminos;
    private Ghosts ghosts;
    private EventManager eventManager;
    private ArrayList<Mover> movers; // all Movers (not from mcminos at the moment - handled separately) - i.e. for ghosts and rocks

    private Random randomGenerator = new Random();
    private boolean movement = true; // when false, don't call the movement method (no movement of objects)
    private long timerFrame = 0; // The game time in frames
    private boolean timer = false; // Does the timer currently run (or only the animations)?
    private long animationFrame = 0; // This one continues running when movement is stopped, and is updated to animationframe when game continues
    private long realFrame = 0; // This frame is always coutned up and never synced to the timerFrame
    private long realGameTime = 0; // this value comes from libgdx, we just sync our frames against it
    private long lastDeltaTimeLeft = 0;
    public static final String suspendFileName = "user-save";

    public Game(Main main, Play playScreen) {
        this.main = main;
        this.playScreen = playScreen;
        movers = new ArrayList<>();
        audio = main.getAudio();
        mcminos = new McMinos(this);
        ghosts = new Ghosts(this);
        initKryo(); // for saving objects
    }

    /**
     * @return usually true, when game continues, if next level or return to menu, return false
     */
    public boolean updateTime() {
        // use square to be more precise
        float gdxtime = Gdx.graphics.getDeltaTime();
        realGameTime += (long) (gdxtime * 1000);

        long deltaTime = (long) (gdxtime * Game.timeResolutionSquare); // needs to have long in front as gdxtime is float (don't apply long directly to gdxtime)
        deltaTime += lastDeltaTimeLeft; // apply what is left
        while (deltaTime >= Game.timeResolution) {
            if (!nextGameFrame()) return false;
            deltaTime -= Game.timeResolution;
        }
        lastDeltaTimeLeft = deltaTime;
        return true;
    }

    /**
     * Start the moving thread which will manage all movement of objects in the game
     */
    public void initEventManager() {
        if (eventManager == null) {
            eventManager = new EventManager();
            eventManager.init(this);
        }
    }

    /**
     * This is the framecounter which is not updated, when in pause-mode
     * Can stopped with stopTimer and started with startTimer.
     *
     * @return
     */
    public long getTimerFrame() {
        return timerFrame;
    }

    /**
     * This is the framecounter which is also updated, when in pause-mode
     * this is resynced with timer when unpaused
     *
     * @return
     */
    public long getAnimationFrame() {
        return animationFrame;
    }

    /**
     * This is the framecounter which is always updated and guarantied to always increase
     *
     * @return
     */
    public long getRealFrame() {
        return realFrame;
    }

    /**
     * This is called all the time to trigger movements and events
     * returns false, if back to menu or next level
     * else should return true
     */
    public boolean nextGameFrame() {
        if (level.isFinished()) {
            return false;
        }
        // do animation timer
        animationFrame++;
        realFrame++;

        if (timer) {
            // timer
            timerFrame++;
            eventManager.update();
            // update durations and trigger events, if necessary
            mcminos.updateDurations();

            if (movement) { // only do this when timer is active
                ghosts.checkSpawn(); // no spawn, if nobody can move
                // move everybody
                mcminos.move(); // first mcminos
                for (int i = movers.size() - 1; i >= 0; i--) {
                    // current could already be destroyed by last mover
                    if (i <= movers.size() - 1) {
                        // TODO: check if this makes sense
                        Mover m = movers.get(i);
                        if (m.move()) {
                            movers.remove(i);
                            LevelObject lo = m.getLevelObject();
                            //lo.getLevelBlock().remove(lo); done in next step
                            if(lo !=null)
                                lo.dispose();
                        }
                        if (level.isFinished()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void disposeEventManagerTasks() {
        if (eventManager != null)
            eventManager.disposeAllTasks();
    }

    public void dispose() {
        stopTimer();
        disposeEventManagerTasks();
        mcminos.dispose();
        ghosts.dispose();
        movers.clear();
        level.dispose();
    }

    public void reset() { // called when restart necessary
        disposeEventManagerTasks();
        // disposeTimerTask(); // will be reused
        // mcminos.dispose(); // will be reused
        ghosts.dispose();
        clearMovers();
        level.dispose(); // also disposes the mcminos-levelobject
        level.addToAllLevelObjects(mcminos.getLevelObject());
        mcminos.clearInventory();
        mcminos.reset();
        playScreen.triggerFade();
    }

/*    int random(int interval) {
        return randomGenerator.nextInt(interval);
    }
*/
    public McMinos getMcMinos() {
        return mcminos;
    }

    public Level getLevel() {
        return level;
    }

    public Ghosts getGhosts() {
        return ghosts;
    }

    public void addMover(Mover mover) {
        movers.add(mover);
    }

    public void removeMover(Mover mover) {
        movers.remove(mover);
    }

    /*does not work in gwt/web public void acquireLock() {
        try {
            updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseLock() {
        updateLock.release();
    }*/

    public long getRealGameTime() {
        return realGameTime;
    }

    public void schedule(EventManager.Types event, LevelObject loWhere) {
        eventManager.schedule(this, event, loWhere.getLevelBlock(), loWhere.getVX(), loWhere.getVY());
    }

    public void schedule(EventManager.Types event, LevelBlock lbWhere) {
        eventManager.schedule(this, event, lbWhere, lbWhere.getVX(), lbWhere.getVY());
    }

    public Audio getAudio() {
        return audio;
    }

    public ArrayList<Mover> getMovers() {
        return movers;
    }

    public void clearMovers() {
        movers.clear();
    }

    public Play getPlayScreen() {
        return playScreen;
    }

    public boolean getMovement() {
        return movement;
    }

    public void startMovement() {
        movement = true;
    }

    public void stopMovement() {
        movement = false;
    }

    public void startTimer() {
        timer = true;
        animationFrame = timerFrame; //sync
    }

    public void stopTimer() {
        timer = false;
    }


    public Level levelNew(LevelConfig levelConfig) {
        level = new Level(main, this, levelConfig);
        initAfterLoad();
        return level;
    }

    public void reload() {
        eventManager.disposeAllTasks();
        level.load(level.getLevelConfig(), true);
        initAfterLoad();
    }

    private void initAfterLoad() {
        // Now init some of the level elements
        getGhosts().init(); // update references
        mcminos.initMover();  // needs to be created this late
        startMovement(); // make sure everything can move
    }


    /* kryo and crypto init */
    private static final String ALGORITHM = "Blowfish";
    private final Key secretKey = new SecretKeySpec("mcminos.ulno.net".getBytes(), ALGORITHM);
    private Cipher cipher;
    private Kryo kryo;

    /**
     * Init cryptographic variables and Kryo for load and save
     */
    void initKryo() {
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (Exception e) {
            Gdx.app.log("exception in initKryo", e.toString());
        }
        kryo = new Kryo();
        DefaultSerializers.KryoSerializableSerializer ser = new DefaultSerializers.KryoSerializableSerializer();
        kryo.register(Level.class, ser);
        kryo.register(McMinos.class, ser);
        kryo.register(Ghosts.class, ser);
        kryo.register(EventManager.class, ser);
        kryo.register(LevelObject.class, ser);
        kryo.register(LevelBlock.class, ser);
        kryo.register(Mover.class, ser);
        kryo.register(McMinosMover.class, ser);
        kryo.register(GhostMover.class, ser);
        kryo.register(RockMover.class, ser);
        //Log.DEBUG();
    }


    /**
     * Create a persistent snapshot for the current gamestate
     * (hibernate to disk)
     * if 0 is given it;s the suspend file
     * 1-3 are normal-save-game slots
     */
    public void saveGame(int slot) {
        FileHandle fh = getSaveFileHandle(slot);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            Output output = new Output(new BufferedOutputStream(new DeflaterOutputStream(new CipherOutputStream(fh.write(false), cipher))));

            kryo.writeObject(output, level);
            kryo.writeObject(output, ghosts);
            kryo.writeObject(output, mcminos);
            kryo.writeObject(output, timerFrame);
            kryo.writeObject(output, eventManager);
            kryo.writeObject(output, movement);

            output.close();
        } catch (Exception e) {
            Gdx.app.log("exception in saveGame", e.toString());
        }
    }

    public static FileHandle getSaveFileHandle( int slot ) {
        return Gdx.files.local(suspendFileName + "-" + slot);
    }

    public boolean loadGame(int slot) {
        FileHandle fh = getSaveFileHandle(slot);

        // check if the save-game exists
        if (fh.exists()) {
            try {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                Input input = new Input(new BufferedInputStream(new InflaterInputStream(new CipherInputStream(fh.read(), cipher))));

                // clearMovers(); will already be cleared
                disposeEventManagerTasks();

                // set context for kryo
                ObjectMap context = kryo.getContext();
                context.put("main",main); // is needed in level

                // restore the state
                level = kryo.readObject(input, Level.class);
                ghosts = kryo.readObject(input, Ghosts.class);
                McMinos tmpmcminos = kryo.readObject(input, McMinos.class);
                mcminos.initAfterKryoLoad(this, tmpmcminos);
                level.initAfterKryoLoad(main, this); // must be done after initializing mcminos
                ghosts.initAfterKryoLoad(this);
                timerFrame = kryo.readObject(input, Long.class);
                animationFrame = timerFrame;
                eventManager = kryo.readObject(input, EventManager.class);
                eventManager.initAfterKryoLoad(this);
                initAfterLoad();

                if (!kryo.readObject(input, Boolean.class)) // must be later as previous line enables movement
                    stopMovement();

                input.close();

                // if this was "just" a suspend file, delete it
                if( slot == 0) {
                    fh.delete();
                }

            } catch (Exception e) {
                Gdx.app.log("Game.loadGame", "Unable to load data file.", e);
                /*// recover by creating a fresh new profile data file;
                // note that the player will lose all game progress
                profile = new Profile();
                persist( profile ); */
                return false; // not successful
            }
        } else {
            return false; // doesn't exist, can't load
        }
        return true; // success
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public boolean isTimerActivated() {
        return timer;
    }

    public Main getMain() {
        return main;
    }
}
