package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the class having all game content which needs to be accessed by all other modules.
 *
 */
public class Game {
    // constants
    public static final int timeResolution = 128; // How often per second movements are updated?
    public static final int timeResolutionExponent = Util.log2binary(timeResolution);
    public static final int baseSpeed = 2; // base speed of all units (kind of the slowest anybody usually moves) in blocks per second

    private final Audio audio;
    private final SpriteBatch batch;
    private final Play playScreen;
    private final OrthographicCamera camera;
    private long gameFrame = 0; // The game time - there is a getter for this
    private long gameTime = 0;
    private Main main;
    //private Semaphore updateLock = new Semaphore(1);
    private Timer.Task timerTask = null;
    String currentLevelName = null;
    private Level level;
    private McMinos mcminos;
    private Ghosts ghosts;
    private FrameTimer frameTimer;
    private ArrayList<Mover> movers; // all Movers (not from mcminos at the moment - handled separately) - i.e. for ghosts and rocks
    private PlayWindow playwindow = null;

    private Random randomGenerator = new Random();
    private boolean movement = false; // only do animations but don't move anything
    boolean toolboxShown = false;



    public PlayWindow getPlayWindow() {
        return playwindow;
    }

    public Game(Main main, Play playScreen, OrthographicCamera camera) {
        this.main = main;
        this.playScreen = playScreen;
        movers = new ArrayList<>();
        audio = main.getAudio();
        mcminos = new McMinos(this);
        ghosts = new Ghosts(this);
        batch = main.getBatch();
        this.camera = camera;
    }

    public void loadFromPlay(String levelName) {
        playScreen.init(levelName);
    }

    public void updateTime() {
        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
    }

    public Level loadLevel(String s) {
        // Load a level
        level = new Level(this, s);
        // is al done in load level mcminos.init(); // trigger update
        playwindow = new PlayWindow(batch,camera,level,mcminos);
        initAfterLoad();

        // start the own timer (which triggers also the movemnet)
        startTimer();
        return level;
    }

    private void initAfterLoad() {
        // Now init some of the level elements
        getGhosts().init(); // update references
        Mover mover = new McMinosMover(this);
        // done in mover creation mcminos.setMover(mover); // needs to be created this late

    }

    public LevelBlock getLevelBlock( int x, int y) {
        return level.get( x, y );
    }

    public LevelBlock getLevelBlockFromVPixel( int vPixelX, int vPixelY) {
        int w = playwindow.getLevelWidth();
        int h = playwindow.getLevelHeight();
        int x = vPixelX  >> playwindow.virtualBlockResolutionExponent;
        int y = vPixelY  >> playwindow.virtualBlockResolutionExponent;
        x = (  x + w ) % w;
        y = ( y  + h ) % h;
        return level.get( x, y );
    }

    public LevelBlock getLevelBlockFromVPixelRounded( int vPixelX, int vPixelY) {
        int w = playwindow.getLevelWidth();
        int h = playwindow.getLevelHeight();
        int roundx = (vPixelX + (playwindow.virtualBlockResolution >> 1)) >> playwindow.virtualBlockResolutionExponent;
        int roundy = (vPixelY + (playwindow.virtualBlockResolution >> 1)) >> playwindow.virtualBlockResolutionExponent;
        //if( level.getScrollX() )
        roundx = (  roundx + w ) % w;
        //else
        //    roundx = Math.max(0,Math.min(w,roundx));
        //if( level.getScrollY() )
        roundy = ( roundy  + h ) % h;
        //else
        //    roundy = Math.max(0,Math.min(h,roundy));
        return level.get( roundx, roundy );
    }


    /**
     * Start the moving thread which will manage all movement of objects in the game
     */
    public void startTimer() {
        frameTimer = new FrameTimer();

        if( timerTask != null)
            timerTask.cancel(); // cancelold one
        timerTask = new Timer.Task() {
            @Override
            public void run() {
                synchronizedNextGameFrame();
            }
        };
        Timer.schedule(timerTask, 0, 1 / (float) timeResolution);
    }

    public long getGameFrame() {
        return gameFrame;
    }

    /**
     * This is called
     */
    private synchronized void synchronizedNextGameFrame() { // to allow serialized iterations
        if( !toolboxShown) { // if game is not paused
            // do timers
            gameFrame++;
            /* done with synchronize // get lock
            try { // needs to be synchronized against drawing
                updateLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            frameTimer.update(gameFrame);


            if(movement) {
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
                    }
                }
            }
            /* done with synchronize updateLock.release(); // release lock */
        }

    }

    public void disposeFrameTimer() {
        frameTimer.dispose();
    }

    public void disposeTimerTask() {
        timerTask.cancel();
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
        //disposeTimerTask();
        //mcminos.dispose(); // will be reused
        ghosts.dispose();
        movers.clear();
        level.dispose();
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
            m.setSpeed(0);
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

    public void setLevel(Level level) {
        this.level = level;
    }

    public void reload() {
        level.load(currentLevelName);
        initAfterLoad();
    }

    public boolean isToolboxShown() {
        return toolboxShown;
    }

    public void setToolboxShown(boolean toolboxShown) {
        this.toolboxShown = toolboxShown;
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

    synchronized public void synchronizedDraw() {
        // not in gwt game.acquireLock();
        level.draw(playwindow);
        // not in gwt game.releaseLock(); // TODO: think about moving this to the end of draw

    }
}
