package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

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
    private long gameFrame = 0; // The game time - there is a getter for this
    private long gameTime = 0;
    private Main main;
    private Semaphore updateLock = new Semaphore(1);
    private Timer.Task timerTask = null;
    String currentLevelName = null;
    private Level level;
    private McMinos mcminos;
    private Ghosts ghosts;
    private FrameTimer frameTimer;
    private ArrayList<Mover> movers; // all Movers (not from mcminos at the moment - handled separately) - i.e. for ghosts and rocks
    private PlayWindow playwindow = null;

    private Random randomGenerator = new Random();

    public PlayWindow getPlayWindow() {
        return playwindow;
    }

    public Game(Main main) {
        this.main = main;
        movers = new ArrayList<>();
        audio = main.getAudio();
        mcminos = new McMinos(this);
        ghosts = new Ghosts(this);
        batch = main.getBatch();
    }

    public void updateTime() {
        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
        playwindow.updateWindowPosition();
    }

    public Level loadLevel(String s) {
        // Load a level
        level = new Level(this, s);
        // is al done in load level mcminos.init(); // trigger update
        playwindow = new PlayWindow(batch,level,mcminos);
        ghosts.init(); // update references
        Mover mover = new McMinosMover(this);
        // done in mover creation mcminos.setMover(mover); // needs to be created this late

        // start the own timer (which triggers also the movemnet)
        startTimer();
        return level;
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
                nextGameFrame();
            }
        };
        Timer.schedule(timerTask, 0, 1 / (float) timeResolution);
    }

    public long getGameFrame() {
        return gameFrame;
    }

    public void pause() {
        timerTask.cancel();
    }

    /**
     * This is called
     */
    private void nextGameFrame() { // to allow serialized iterations
        if( !playwindow.toolboxShown) { // if game is not paused
            // do timers
            gameFrame++;
            frameTimer.update(gameFrame);
            // update durations and trigger events, if necessary
            mcminos.updateDurations();
            // move everybody
            try { // needs to be synchronized against drawing
                updateLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ghosts.checkSpawn();

            // move everybody
            mcminos.move();
            for (int i= movers.size()-1; i>=0; i--) { // works as synchronized
                Mover m = movers.get(i);
                if(m.move()) {
                    movers.remove(i);
                    LevelObject lo = m.getLevelObject();
                    lo.getLevelBlock().removeMovable(lo);
                    lo.dispose();
                }
            }

        }

        updateLock.release();
    }

    public void dispose() {
        movers.clear();
        LevelObject.disposeAll();
    }

    public void setScreen(Screen scr) {
        main.setScreen(scr);
    }

    public void setDestination(int x, int y) {
        playwindow.destination.setGfx(Entities.destination);
        playwindow.destination.moveTo(x, y, getLevelBlockFromVPixelRounded(x, y));
        playwindow.destinationSet = true;
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

    public void acquireLock() {
        try {
            updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseLock() {
        updateLock.release();
    }

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
        for(Mover m: movers) {
            m.setSpeed(0);
        }
    }

    public ArrayList<Mover> getMovers() {
        return movers;
    }
}
