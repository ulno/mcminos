package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the class having all game content which needs to be accessed by all other modules.
 * It also manages the game content but nothing graphical.
 * Graphical things like projection or coordinates would go to playwindow or the PlayScreen.
 * The timing of the game is done here.
 * Audio events can be (still) triggered from here.
 * Also the toolbox needs to be controlled from outside and doesn't belong in here.
 *
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
    private FrameTimer frameTimer;
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
     *
     * @return usually true, when game continues, if next level or return to menu, return false
     */
    public boolean updateTime() {
        // use square to be more precise
        float gdxtime = Gdx.graphics.getDeltaTime();
        gameTime += (long)( gdxtime* 1000);

        if(timerTaskActive) {
            long deltaTime = (long) (gdxtime * Game.timeResolution * Game.timeResolution);
            deltaTime += lastDeltaTimeLeft; // apply what is left
            while (deltaTime >= Game.timeResolution) {
                if(! nextGameFrame() ) return false;
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
        frameTimer = new FrameTimer();

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
        if( level.isFinished() ) {
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
        frameTimer.update(gameFrame);


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
                        lo.getLevelBlock().removeMovable(lo);
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
        frameTimer.dispose();
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
        movers.clear();
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

    public void schedule(FrameTimer.Task task, int interval) {
        frameTimer.schedule(task, interval);
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
        initAfterLoad( );
        return level;
    }

    public void reload() {
        level.load(level.getName());
        initAfterLoad( );
    }

    private void initAfterLoad( ) {
        // Now init some of the level elements
        getGhosts().init(); // update references
        Mover mover = new McMinosMover(this,mcminos);
        // done in mover creation mcminos.setMover(mover); // needs to be created this late
        disableMovement();
    }

    /**
     * Create a persistentsnapshot for the current gamestate
     * (hibernate to disk)
     */
    private void saveSnapshot() {
        Gdx.files.local("settings.json");
        Json output = new Json();
        Gdx.files.local("user-save.json");
        JsonWriter jw = new JsonWriter(         );
        JsonState state = new JsonState(this);
        state.
    }
}
