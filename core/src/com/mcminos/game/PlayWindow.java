package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by ulno on 05.10.15.
 * Handles all drawing-related to the Play-Screen Window
 */
public class PlayWindow {
    public final static int virtualBlockResolution = 128; // How many virtual pixels is a block big (independent of actually used resolution), must be a power of 2
    public final static int virtualBlockResolutionExponent = Util.log2binary(virtualBlockResolution);
    private final McMinos mcminos;
    public SpriteBatch batch;
    public BitmapFont defaultFont;
    public Skin skin;
    Entities gfx = null;
    int windowVPixelXPos; // windowVPixelXPos-position (left) of game window in main game-screen in virtual pixels
    int windowVPixelYPos; // windowVPixelYPos-position (bottom) of game window n main game-screen in virtual pixels
    int windowPixelWidth; // width of game-window in physical pixels
    int windowPixelHeight; // height of game-window in physical pixels
    int windowVPixelWidth; // width of game-window in virtual pixels
    int windowVPixelHeight; // height of game-window in virtual pixels
    int resolution;
    int resolutionExponent;
    int fullPixelWidth =0;
    int fullPixelHeight =0; // Size of virtual playingfield in physical pixels (blocks * physical resolution)
    float density;
    Level level;
    Game game;

    public PlayWindow(SpriteBatch batch, Level level, McMinos mcminos) {
        this.batch = batch;
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        this.level = level;
        this.game = level.getGame();
        this.mcminos = mcminos;
    }

    public void init() {
        gfx = Entities.getInstance();
        // Load after graphics have been loaded
        windowVPixelXPos = 0;
        windowVPixelYPos = 0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
    }


    public void setResolution(int resolutionCounter) {
        resolution = Entities.resolutionList[resolutionCounter];
        resolutionExponent = Util.log2binary(resolution);
        Graphics.setResolutionAll(this, resolution);
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public void updateWindowPosition() {
        if( ! game.isToolboxShown()) {
            windowVPixelXPos = computeWindowCoordinate(windowVPixelXPos, mcminos.getLevelObject().getVX(), level.getScrollX(), getLevelWidth(), windowVPixelWidth);
            windowVPixelYPos = computeWindowCoordinate(windowVPixelYPos, mcminos.getLevelObject().getVY(), level.getScrollY(), getLevelHeight(), windowVPixelHeight);
        }
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
        // We compute the view based on Main' position
        // when we are calling this, we try to make sure Main is visible near the center of the screen
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

    public void resize(int width, int height) {
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
        if (windowVPixelWidth > level.getVisibleWidth() << virtualBlockResolutionExponent) {
            windowVPixelWidth = level.getVisibleWidth() << virtualBlockResolutionExponent;
            windowPixelWidth = level.getVisibleWidth() << resolutionExponent;
        }
        if (windowVPixelHeight > level.getVisibleHeight() << virtualBlockResolutionExponent) {
            windowVPixelHeight = level.getVisibleHeight() << virtualBlockResolutionExponent;
            windowPixelHeight = level.getVisibleHeight() << resolutionExponent;
        }
        fullPixelWidth = getLevelWidth() << resolutionExponent;
        fullPixelHeight = getLevelHeight() << resolutionExponent;
    }

    public void resize() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public int getVisibleWidth() {
        return level.getVisibleWidth();
    }

    public int getVisibleHeight() {
        return level.getVisibleHeight();
    }

    public boolean getScrollX() {
        return level.getScrollX();
    }

    public boolean getScrollY() {
        return level.getScrollY();
    }

    public int getLevelWidth() {
        return level.getWidth();
    }

    public int getLevelHeight() {
        return level.getHeight();
    }

    public int getVPixelsLevelWidth() {
        return level.getVPixelsWidth();
    }

    public int getVPixelsLevelHeight() {
        return level.getVPixelsHeight();
    }

    public int getWindowVPixelWidth() {
        return windowVPixelWidth;
    }

    public int getWindowVPixelHeight() {
        return windowVPixelHeight;
    }

    public int getWindowPixelWidth() {
        return windowPixelWidth;
    }

    public int getWindowPixelHeight() {
        return windowPixelHeight;
    }

    public Skin getSkin() {
        return skin;
    }

    public Game getGame() {
        return game;
    }

    public BitmapFont getFont() {
        return defaultFont;
    }
}
