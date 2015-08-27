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
    static double x; // x-position (left) of game window in main game-screen in block coordinates
    static double y; // y-position (bottom) of game window in main game-screen in blocks
    static int w; // width of game-window in pixels
    static int h; // height of game-window in pixels
    static double blockw; // width of game-window in blocks
    static double blockh; // height of game-window in blocks
    static int resolution;
    static int gameResolutionCounter;
    static int fullWidth=0, fullHeight=0; // Size of virtual playingfield in pixels
    static private float density;
    public static LevelObject mcminos = null;
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
        // We compute the view based on McMinos' position
        // when we are calling this, we try to make sure McMinos is visible near the center of the screen
        // However, scrollability of the level needs to be respected.
        // compute the distance of mcminos from the center

        if( level.getWidth() > blockw || level.getScrollX() ) {
            double mcmX = mcminos.getX();
            double centerX = x + blockw / 2; // center of the visible screen
            double deltaX = mcmX + 0.5 - centerX; // < 0 means mcminos is left from center
            deltaX /= 10; // do it a little slowly depending on distance
            if( Math.abs(deltaX) < 0.01 ) deltaX = 0;
            else x += deltaX;
        }
        else { // make sure all is visible
            x = 0.0;
        }

        if( level.getHeight() > blockh || level.getScrollY() ) {
            double mcmY = mcminos.getY();
            double centerY = y + blockh / 2; // center of the visible screen
            double deltaY = mcmY + 0.5 - centerY; // < 0 means mcminos is under center
            deltaY /= 10; // do it a little slowly depending on distance
            if( Math.abs(deltaY) < 0.01 ) deltaY = 0;
            else y += deltaY;
        }
        else { // make sure all is visible
            y = 0.0;
        }

        // TODO: factor in delta time to make scrolling smooth
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
        w = width;
        h = height;
        blockw = (double)w / resolution;
        blockh = (double)h / resolution;
        // eventually restrict visibility
        if(level != null) // might not be set
        {
            if (blockw > level.getVisibleWidth()) {
                blockw = level.getVisibleWidth();
                w = (int) (blockw * resolution);
            }
            if (blockh > level.getVisibleHeight()) {
                blockh = level.getVisibleHeight();
                h = (int) (blockh * resolution);
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
        x = 0.0;
        y = 0.0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
        //resize();  now donne in reolsution setting
        batch = new SpriteBatch();
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        resolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        GameGraphics.setResolutionAll();
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

    public static double getBlockw() {
        return blockw;
    }

    public static double getBlockh() {
        return blockh;
    }
}
