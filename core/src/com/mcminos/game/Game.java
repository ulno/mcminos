package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Random;

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
    public static final int timeResolution = 128; // How often per second movements are updated?
    public static final int timeResolutionExponent = Util.log2binary(timeResolution);
    public static final int baseSpeed = 2; // base speed of all units (kind of the slowest anybody usually moves) in blocks per second
    private Main main;
    private final Play playScreen;
    private final Audio audio;
    private long gameFrame = 0; // The game time in frames
    private long gameTime = 0;
    private Level level;
    private McMinos mcminos;
    private Ghosts ghosts;
    private EventManager eventManager;
    private ArrayList<Mover> movers; // all Movers (not from mcminos at the moment - handled separately) - i.e. for ghosts and rocks

    private Random randomGenerator = new Random();
    private boolean movement = false; // only do animations but don't move anything
    private long lastDeltaTimeLeft = 0;
    private boolean timerTaskActive = false;

    public Game(Main main, Play playScreen) {
        this.main = main;
        this.playScreen = playScreen;
        movers = new ArrayList<>();
        audio = main.getAudio();
        mcminos = new McMinos(this);
        ghosts = new Ghosts(this);
    }

    /**
     * @return usually true, when game continues, if next level or return to menu, return false
     */
    public boolean updateTime() {
        // use square to be more precise
        float gdxtime = Gdx.graphics.getDeltaTime();
        gameTime += (long) (gdxtime * 1000);

        if (timerTaskActive) {
            long deltaTime = (long) (gdxtime * Game.timeResolution * Game.timeResolution);
            deltaTime += lastDeltaTimeLeft; // apply what is left
            while (deltaTime >= Game.timeResolution) {
                if (!nextGameFrame()) return false;
                deltaTime -= Game.timeResolution;
            }
            lastDeltaTimeLeft = deltaTime;
        }
        return true;
    }

    /**
     * Start the moving thread which will manage all movement of objects in the game
     */
    public void startTimer() {
        eventManager = new EventManager();

        timerTaskActive = true;

        /*
            updates now handled in render based on deltaTime
        if( timerTask != null)
            timerTask.cancel(); // cancel old one
        timerTask = new Timer.Task() {
            @Override
            public void run() {
                nextGameFrame();
            }
        };
        Timer.schedule(timerTask, 0, 1 / (float) timeResolution); */
    }

    public long getGameFrame() {
        return gameFrame;
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
        // do timers
        gameFrame++;
            /* done with synchronize // get lock
            try { // needs to be synchronized against drawing
                updateLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        eventManager.update(this);


        if (movement) {
            // update durations and trigger events, if necessary
            mcminos.updateDurations();
            ghosts.checkSpawn();

            // move everybody
            mcminos.move();
            for (int i = movers.size() - 1; i >= 0; i--) { // works as synchronized
                // current could already be destroyed by last mover
                if (i <= movers.size() - 1) {
                    // TODO: check if this makes sense
                    Mover m = movers.get(i);
                    if (m.move()) {
                        movers.remove(i);
                        LevelObject lo = m.getLevelObject();
                        lo.getLevelBlock().remove(lo);
                        lo.dispose();
                    }
                    if (level.isFinished()) {
                        return false;
                    }
                }
            }
        }
            /* done with synchronize updateLock.release(); // release lock */
        return true;
    }

    public void disposeFrameTimer() {
        eventManager.dispose();
    }

    public void disposeTimerTask() {
        //timerTask.cancel();
        timerTaskActive = false;
    }

    public void dispose() {
        disposeFrameTimer();
        disposeTimerTask();
        mcminos.dispose();
        ghosts.dispose();
        movers.clear();
        level.dispose();
    }

    public void reset() {
        disposeFrameTimer();
        // disposeTimerTask(); // will be reused
        // mcminos.dispose(); // will be reused
        ghosts.dispose();
        clearMovers();
        level.dispose();
        mcminos.clearInventory();
        mcminos.reset();
    }

    int random(int interval) {
        return randomGenerator.nextInt(interval);
    }

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

    public long getGameTime() {
        return gameTime;
    }

    public void schedule(EventManager.Types event, LevelObject loWhere) {
        eventManager.schedule(this, event, loWhere.getLevelBlock(), loWhere.getVX(), loWhere.getVY() );
    }

    public void schedule(EventManager.Types event, LevelBlock lbWhere) {
        eventManager.schedule(this, event, lbWhere, lbWhere.getVX(), lbWhere.getVY() );
    }

    public Audio getAudio() {
        return audio;
    }

    public void stopAllMovers() {
        /*for(Mover m: movers) {
            m.computeSpeeds(0);
        }*/
        movement = false;
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

    public void enableMovement() {
        movement = true;
    }

    public void disableMovement() {
        movement = false;
    }

    public Level levelNew(String levelName) {
        level = new Level(this, levelName);
        initAfterLoad();
        return level;
    }

    public void reload() {
        level.load(level.getName(), true);
        initAfterLoad();
    }

    private void initAfterLoad() {
        // Now init some of the level elements
        getGhosts().init(); // update references
        mcminos.initMover();  // needs to be created this late
        disableMovement();
    }

    /**
     * Create a persistent snapshot for the current gamestate
     * (hibernate to disk)
     */
    public void saveSnapshot() {
        FileHandle settings = Gdx.files.local("settings.json");
        Json json = new Json();
        FileHandle userSave = Gdx.files.local("user-save.json");

        JsonState jsonState = new JsonState(this);
        // convert the given profile to text
        String profileAsText = json.toJson(jsonState);

        Gdx.app.log("profileAsText", json.prettyPrint(profileAsText));
        // encode the text
        String profileAsCode = Base64Coder.encodeString(profileAsText);

        // write the profile data file
        userSave.writeString(profileAsCode, false);
    }

    public void loadSnapshot() {
        FileHandle userSave = Gdx.files.local("user-save.json");
        // create the JSON utility object
        Json json = new Json();

        // check if the profile data file exists
        if (userSave.exists()) {

            // load the profile from the data file
//            try {

            // read the file as text
            String profileAsCode = userSave.readString();

            // decode the contents
            String profileAsText = Base64Coder.decodeString(profileAsCode);

            // clearMovers(); should be cleared before
            // restore the state
            JsonState jsonState = json.fromJson(JsonState.class, profileAsText);
            level = jsonState.getLevel();
            ghosts = jsonState.getGhosts();
            McMinos tmpmcminos = jsonState.getMcminos();
            mcminos.initAfterJsonLoad(this,tmpmcminos);
            level.initAfterJsonLoad(this); // must be done after initializing mcminos
            ghosts.initAfterJsonLoad(this);
            // TODO: update movers and speeds for ghosts and mcminos - do they first have to be removed?
            // find loaded movers and add them to the list of movers
            initAfterLoad(); // TODO: does this make sense?

            // done in game initialization:mcminos.initMover(); TODO: make compliant with movers we read in
            // mcminos levelobject is not in level.allLevelObjects // TODO: make sure it's added
            // TODO: restore explosions and fuses (timed tasks)


//            } catch( Exception e ) {

            // log the exception
//                Gdx.app.error( "info", "Unable to parse existing profile data file", e );

                /*// recover by creating a fresh new profile data file;
                // note that the player will lose all game progress
                profile = new Profile();
                persist( profile ); */

//            }

//        } else {
/*            // create a new profile data file
            profile = new Profile();
            persist( profile ); */
        }
    }
}
