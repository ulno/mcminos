package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the class having all static game content which needs to be accessed by all other modules.
 *
 */
public class Game {
    static SpriteBatch batch;
    static Entities gfx = null;
    static long gameTime = 0;
    static double windowXPos; // windowXPos-position (left) of game window in main game-screen in block coordinates
    static double windowYPos; // windowYPos-position (bottom) of game window in main game-screen in blocks
    static int windowPixelWidth; // width of game-window in pixels
    static int windowPixelHeight; // height of game-window in pixels
    static double windowBlockWidth; // width of game-window in blocks
    static double windowBlockHeight; // height of game-window in blocks
    static int resolution;
    static int gameResolutionCounter;
    static int fullWidth=0, fullHeight=0; // Size of virtual playingfield in pixels
    static private float density;
    public static LevelObject mcminos = null;
    public static LevelObject destination = null;
    private static Level level;


    private static Game ourInstance = new Game();

    public static Game getInstance() {
        return ourInstance;
    }

    private Game() {
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public void updateWindowPosition() {

        windowXPos = computeWindowCoordinate(windowXPos, mcminos.getX(), level.getScrollX(), getLevelWidth(), windowBlockWidth);
        windowYPos = computeWindowCoordinate(windowYPos, mcminos.getY(), level.getScrollY(), getLevelHeight(), windowBlockHeight);

    }

    private double computeWindowCoordinate(double inputPos, double mcmPos, boolean scroll, int totalBlocks, double visibleBlocks) {
        // We compute the view based on McMinos' position
        // when we are calling this, we try to make sure McMinos is visible near the center of the screen
        // However, scrollability of the level needs to be respected.
        // compute the distance of mcminos from the center
        // TODO: factor in delta time to make scrolling smooth
        if (totalBlocks > visibleBlocks) { // if it is not totally visible
            double center = inputPos + visibleBlocks / 2; // center of the visible screen
            double delta = mcmPos - center; // < 0 means mcminos is under center
            if (scroll) { // the level scrolls in this direction
                if (Math.abs(delta) > totalBlocks / 2.0)
                    delta = Math.signum(delta) * (Math.abs(delta) - totalBlocks);
                delta /= 20; // do it a little slowly depending on distance
                if (Math.abs(delta) < 0.01) delta = 0;
                else {
                    inputPos += delta;
                    if (inputPos < 0) inputPos += totalBlocks;
                    else {
                        if (inputPos >= totalBlocks) inputPos -= totalBlocks;
                    }
                }
            } else { // not all is visible, so we can scroll, but not as far as in the scrolling case
                // the corner should still stay a corner
                inputPos += delta;
                if (inputPos < 0) inputPos = 0;
                else {
                    if (inputPos >= totalBlocks - visibleBlocks) inputPos = totalBlocks - visibleBlocks;
                }
            }
        }
        else {
            if( ! scroll )// not scroll and not too small -> make sure level aligned
                inputPos = 0.0;
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
        windowBlockWidth = (double) windowPixelWidth / resolution;
        windowBlockHeight = (double) windowPixelHeight / resolution;
        // eventually restrict visibility
        if(level != null) // might not be set
        {
            if (windowBlockWidth > level.getVisibleWidth()) {
                windowBlockWidth = level.getVisibleWidth();
                windowPixelWidth = (int) (windowBlockWidth * resolution);
            }
            if (windowBlockHeight > level.getVisibleHeight()) {
                windowBlockHeight = level.getVisibleHeight();
                windowPixelHeight = (int) (windowBlockHeight * resolution);
            }
            Game.fullWidth = Game.getLevelWidth() * Game.resolution;
            Game.fullHeight = Game.getLevelHeight() * Game.resolution;
        }
    }

    public static void resize() {
        resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight() );
    }

    public void init() {
        gfx = Entities.getInstance();
        windowXPos = 0.0;
        windowYPos = 0.0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
        //resize();  now donne in reolsution setting
        batch = new SpriteBatch();
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        resolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        GameGraphics.setResolutionAll();
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

    public static double getWindowBlockWidth() {
        return windowBlockWidth;
    }

    public static double getWindowBlockHeight() {
        return windowBlockHeight;
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
}
