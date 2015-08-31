package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Timer;

import java.util.concurrent.Semaphore;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the class having all static game content which needs to be accessed by all other modules.
 *
 */
public class Game {
    // constants
    public final static int timeResolution = 128; // How often per second movements are updated?
    public final static int virtualBlockResolution = 128; // How many virtual pixels is a block big (independent of actually used resolution), must be a power of 2
    public final static double baseSpeed = 2.0; // in blocks per second
    // derived constants
    public final static int timeResolutionExponent = Util.log2binary(timeResolution);
    public final static int virtualBlockResolutionExponent = Util.log2binary(virtualBlockResolution);
    // not needed (yet?) public final static int baseVPixelSpeedPerFrame = (int) Math.round(baseSpeed * virtualBlockResolution / timeResolution);

    static SpriteBatch batch;
    static Entities gfx = null;
    static long gameTime = 0;
    static int windowVPixelXPos; // windowVPixelXPos-position (left) of game window in main game-screen in virtual pixels
    static int windowVPixelYPos; // windowVPixelYPos-position (bottom) of game window n main game-screen in virtual pixels
    static int windowPixelWidth; // width of game-window in physical pixels
    static int windowPixelHeight; // height of game-window in physical pixels
    static int windowVPixelWidth; // width of game-window in virtual pixels
    static int windowVPixelHeight; // height of game-window in virtual pixels
    static int resolution;
    static int resolutionExponent;
    static int gameResolutionCounter;
    static int fullPixelWidth =0, fullPixelHeight =0; // Size of virtual playingfield in physical pixels (blocks * physical resolution)
    static private float density;
    public static LevelObject mcminos = null;
    public static LevelObject destination = null;
    public static Level level;
    private Mover mcmMover;
    static Semaphore updateLock = new Semaphore(1);

    public static void setResolution(int resolution) {
        Game.resolution = resolution;
        resolutionExponent = Util.log2binary(resolution);
        GameGraphics.setResolutionAll();
    }

    private static Game ourInstance = new Game();
    public static long gameFrame = 0;

    public static Game getInstance() {
        return ourInstance;
    }

    private Game() {
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public void updateWindowPosition() {

        windowVPixelXPos = computeWindowCoordinate(windowVPixelXPos, mcminos.getX(), level.getScrollX(), getLevelWidth(), windowVPixelWidth);
        windowVPixelYPos = computeWindowCoordinate(windowVPixelYPos, mcminos.getY(), level.getScrollY(), getLevelHeight(), windowVPixelHeight);

    }

    /**
     *
     * @param inputPos current window corner position in virtual pixels
     * @param mcmPos current mcminos position in virtual pixels
     * @param scroll is scrolling for current level switched on?
     * @param totalBlocks total number of blocks for this coordinate (level block width or height)
     * @param visibleVPixels total number of visible virtual pixels in this coordinate
     * @return
     */
    private int computeWindowCoordinate(int inputPos, int mcmPos, boolean scroll, int totalBlocks, int visibleVPixels) {
        // We compute the view based on McMinos' position
        // when we are calling this, we try to make sure McMinos is visible near the center of the screen
        // However, scrollability of the level needs to be respected.
        // compute the distance of mcminos from the center
        // TODO: factor in delta time to make scrolling smooth or move to normal game movement mechanics
        int totalVPixels = totalBlocks << virtualBlockResolutionExponent;
        if ( totalVPixels > visibleVPixels) { // if it is not totally visible
            int center = (inputPos + (visibleVPixels >> 1)) % totalVPixels; // center of the visible screen
            int delta = mcmPos - center; // < 0 means mcminos is under center
            if (scroll) { // the level scrolls in this direction
                if (Math.abs(delta) > totalBlocks << (virtualBlockResolutionExponent - 1))
                    delta = (int) Math.signum(delta) * (Math.abs(delta) - totalVPixels);
                delta >>= virtualBlockResolutionExponent - 2; // do it a little slowly depending on distance TODO: make constant dependent on other constants
                inputPos += delta;
                if (inputPos < 0) inputPos += totalVPixels;
                else {
                    if (inputPos >= totalVPixels)
                        inputPos -= totalVPixels;
                }
            } else { // not all is visible, so we can scroll, but not as far as in the scrolling case
                // the corner should still stay a corner
                inputPos += delta;
                if (inputPos < 0)
                    inputPos = 0;
                else {
                    if (inputPos >= (totalVPixels) - visibleVPixels)
                        inputPos = (totalVPixels) - visibleVPixels;
                }
            }
        }
        else {
            // TODO: reconsider scrolling policy
            // if( ! scroll )// not scroll and not too small -> make sure level aligned
                inputPos = 0;
        }
        return inputPos;
    }

    public void updateTime() {
        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
        updateWindowPosition();
    }

    public static void resize(int width, int height) {
        // Solution from here: http://gamedev.stackexchange.com/questions/68785/why-does-resizing-my-game-window-move-and-distort-my-rendering
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0, 0, width, height); // TODO: eventually respect centering here, if level too small
        batch.setProjectionMatrix(matrix);
        // apply globally
        windowPixelWidth = width;
        windowPixelHeight = height;
        windowVPixelWidth = Util.shiftLeftLogical(windowPixelWidth, virtualBlockResolutionExponent - resolutionExponent );
        windowVPixelHeight = Util.shiftLeftLogical(windowPixelHeight, virtualBlockResolutionExponent - resolutionExponent );
        // eventually restrict visibility
        if(level != null) // might not be set
        {
            if (windowVPixelWidth > level.getVisibleWidth() << virtualBlockResolutionExponent) {
                windowVPixelWidth = level.getVisibleWidth() << virtualBlockResolutionExponent;
                windowPixelWidth = level.getVisibleWidth() << virtualBlockResolutionExponent;
            }
            if (windowVPixelHeight > level.getVisibleHeight() << virtualBlockResolutionExponent) {
                windowVPixelHeight = level.getVisibleHeight() << virtualBlockResolutionExponent;
                windowPixelHeight = windowVPixelHeight << virtualBlockResolutionExponent;
            }
            Game.fullPixelWidth = Game.getLevelWidth() * Game.resolution;
            Game.fullPixelHeight = Game.getLevelHeight() * Game.resolution;
        }
    }

    public static void resize() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void init() {
        gfx = Entities.getInstance();
        windowVPixelXPos = 0;
        windowVPixelYPos = 0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
        //resize();  now donne in resolution setting
        batch = new SpriteBatch();
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        resolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        Game.setResolution(resolution);
        // create destination-object
        destination = new LevelObject(0,0,Entities.destination.getzIndex());
    }


    public void loadLevel(String s) {
        // Load a level
        level = new Level(s);
    }

    public static int getVisibleWidth() {
        return level.getVisibleWidth();
    }

    public static int getVisibleHeight() {
        return level.getVisibleHeight();
    }

    public static boolean getScrollX() {
        return level.getScrollX();
    }

    public static boolean getScrollY() {
        return level.getScrollY();
    }

    public static int getLevelWidth() {
        return level.getWidth();
    }

    public static int getLevelHeight() {
        return level.getHeight();
    }
    public static int getVPixelsLevelWidth() {
        return level.getVPixelsWidth();
    }

    public static int getVPixelsLevelHeight() {
        return level.getVPixelsHeight();
    }

    public static int getWindowVPixelWidth() {
        return windowVPixelWidth;
    }

    public static int getWindowVPixelHeight() {
        return windowVPixelHeight;
    }

    public static SpriteBatch getBatch() {
        return batch;
    }

    public static int getWindowPixelWidth() {
        return windowPixelWidth;
    }

    public static int getWindowPixelHeight() {
        return windowPixelHeight;
    }

    public static LevelBlock getLevelBlock( int x, int y) {
        return level.get( x, y);
    }

    public static LevelBlock getLevelBlockFromVPixel( int vPixelX, int vPixelY) {
        int w = Game.getLevelWidth();
        int h = Game.getLevelHeight();
        int roundx = (vPixelX + (virtualBlockResolution >> 1)) >> virtualBlockResolutionExponent;
        int roundy = (vPixelY + (virtualBlockResolution >> 1)) >> virtualBlockResolutionExponent;
        if( level.getScrollX() ) roundx = (  roundx + w ) % w;
        else roundx = Math.min(0,Math.max(w,roundx));
        if( level.getScrollY() )  roundy = ( roundy  + h ) % h;
        else roundy = Math.min(0,Math.max(h,roundy));
        return level.get( roundx, roundy );
    }

    /**
     * Start the moving thread which wil manage all movement of objects in the game
     */
    public void startMover() {
        mcmMover = new Mover( mcminos, 1.0, Entities.mcminos_default_front, Entities.mcminos_default_up,
                Entities.mcminos_default_right, Entities.mcminos_default_down, Entities.mcminos_default_left);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                gameFrame++;

                doMovement();

            }
        }
                , 0, 1 / (float)timeResolution);
    }

    /**
     * This is called
     */
    private void doMovement() {
        // move everybody
        try { // neds to be synchronized against drawing
            Game.updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // for now only mcminos
        if(destination.hasGfx()) { // destination is set
            // check screen distance
            int x = mcminos.getX();
            int xdelta = x - destination.getX(); // delta to center of destination (two centers substract)
            int xdiff = Math.abs( xdelta );
            if (xdiff <= virtualBlockResolution >> 1 || xdiff >= getVPixelsLevelWidth() - (virtualBlockResolution >> 1))
                xdelta = 0;
            else {
                if (getScrollX() && xdiff >= getVPixelsLevelWidth() >> 1) xdelta = (int) Math.signum(xdelta);
                else xdelta = - (int) Math.signum(xdelta);
            }
            int y = mcminos.getY();
            int ydelta = y - destination.getY(); // delta to center of destination (two centers substract)
            int ydiff = Math.abs( ydelta );
            if (ydiff <= virtualBlockResolution >> 1 || ydiff >= getVPixelsLevelHeight() - (virtualBlockResolution >> 1))
                ydelta = 0;
            else {
                if( getScrollY() && ydiff >= getVPixelsLevelHeight() >> 1 ) ydelta = (int) Math.signum(ydelta);
                else ydelta = - (int) Math.signum(ydelta);
            }

            Mover.directions tryDirections[] = {Mover.directions.STOP, Mover.directions.STOP};
            int dircount = 0;
            if( ydelta > 0 ) tryDirections[dircount++] = Mover.directions.UP;
            if( ydelta < 0 ) tryDirections[dircount++] = Mover.directions.DOWN;
            if( xdelta > 0 ) tryDirections[dircount++] = Mover.directions.RIGHT;
            if( xdelta < 0 ) tryDirections[dircount++] = Mover.directions.LEFT;
            if(dircount > 1 && xdiff > ydiff) {
                Mover.directions tmp = tryDirections[0];
                tryDirections[0] = tryDirections[1];
                tryDirections[1] = tmp;
            }

            /*double newx = x + xdelta * deltaTime * 2;
            double newy = y + ydelta * deltaTime * 2;

            if(getScrollX()) {
                if (newx < 0.0) newx += getLevelWidth();
                if (newx >= getLevelWidth()) newx -= getLevelWidth();
            }
            if(getScrollY()) {
                if (newy < 0.0) newy += getLevelHeight();
                if (newy >= getLevelHeight()) newy -= getLevelHeight();
            }

            mcminos.moveTo(newx, newy); */
            mcmMover.move(tryDirections);
        }

        Game.updateLock.release();
    }
}
