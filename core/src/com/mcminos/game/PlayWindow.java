package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;


/**
 * Created by ulno on 05.10.15.
 * Handles all drawing-related to the Play-Screen Window
 */
public class PlayWindow {
    public final static int virtualBlockResolution = 128; // How many virtual pixels is a block big (independent of actually used resolution), must be a power of 2
    public final static int virtualBlockResolutionExponent = Util.log2binary(virtualBlockResolution);
    private final McMinos mcminos;
    private final OrthographicCamera camera;
    private final Main main;
    private final Preferences preferences;
    public SpriteBatch batch;
    public BitmapFont defaultFont;
    public Skin skin;
//    Entities gfx = null;
    int projectionX, projectionY;
    int windowVPixelXPos; // windowVPixelXPos-position (left) of game window in main game-screen in virtual pixels
    int windowVPixelYPos; // windowVPixelYPos-position (bottom) of game window n main game-screen in virtual pixels
    int visibleWidthInPixels; // width of game-window in physical pixels
    int visibleHeightInPixels; // height of game-window in physical pixels
    int visibleWidthInVPixels; // width of visible game-window in virtual pixels
    int visibleHeightInVPixels; // height of visible game-window in virtual pixels
    int resolution;
    int resolutionExponent;
    public int virtual2MiniExponent;
    int levelWidthInPixels =0;
    int levelHeightInPixels =0; // Size of virtual playingfield in physical pixels (blocks * physical resolution)
    Level level;
    Game game;
    private int visibleWidthInBlocks; // Number of blocks visible (even fractions off)
    private int visibleHeightInBlocks; // Number of blocks visible (even fractions off)
    private int viewWidthInPixels;
    private int viewHeightInPixels;
    private Rectangle scissors = new Rectangle();
    private int currentResolutionBitsLeftShifter;
    public int virtual2MiniResolution;
    private int miniX;
    private int miniY;
    private boolean miniMapLeft = false;

    public PlayWindow(Main main, SpriteBatch batch, OrthographicCamera camera, Level level, McMinos mcminos) {
        this.main = main;
        this.batch = batch;
        this.camera = camera;
        this.level = level;
        this.game = level.getGame();
        this.mcminos = mcminos;
        this.preferences = main.getPreferences();
    }

/*    seems obsolete public void loadLevel() {
        gfx = Entities.getInstance();
        // Load after graphics have been loaded
        windowVPixelXPos = 0;
        windowVPixelYPos = 0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
    }*/


    public void setResolution(int res, int toolboxWidth) {
        resolution = preferences.setGameResolution(res);
        resolutionExponent = Util.log2binary(resolution);
        Graphics.setResolutionAll(this, resolution, toolboxWidth);
        currentResolutionBitsLeftShifter = Util.log2binary(resolution) - PlayWindow.virtualBlockResolutionExponent;
        resize(toolboxWidth);
    }

    /**
     *
     * @param inputPos current window corner position in virtual pixels
     * @param mcmPos current mcminos position in virtual pixels
     * @param scroll is scrolling for current level and respective coordinate switched on?
     * @param totalBlocks total number of blocks for this coordinate (level block width or height)
     * @param visibleVPixels total number of visible virtual pixels in this coordinate
     * @param scrollSpeed if 0 make the full shift so window is at right position (>0: just make small shift to follow, bigger=faster)
     * @return newCoordinate
     */
    private int computeWindowCoordinate(int inputPos, int mcmPos, boolean scroll, int totalBlocks, int visibleVPixels, int scrollSpeed) {
        // We compute the view based on McMinos' position
        // when we are calling this, we try to make sure McMinos is visible near the center of the screen
        // However, scrollability of the level needs to be respected.

        int totalVPixels = totalBlocks << virtualBlockResolutionExponent;
        int center = (inputPos + (visibleVPixels >> 1)) % totalVPixels; // center of the visible screen
        // compute the distance of mcminos from the center
        int delta = mcmPos - center; // < 0 means mcminos is under/left of center
        if (scroll) { // the level scrolls in this direction
            if (Math.abs(delta) > totalBlocks << (virtualBlockResolutionExponent - 1))
                delta = (int) Math.signum(delta) * (Math.abs(delta) - totalVPixels);
            if (scrollSpeed > 0) // if this is 0, apply full delta
                delta /= (virtualBlockResolution >> 1) / scrollSpeed; // do it a little slowly depending on distance
            inputPos += delta;
            if (inputPos < 0) inputPos += totalVPixels;
            else {
                if (inputPos >= totalVPixels)
                    inputPos -= totalVPixels;
            }
        } else { // not all is visible, so we can scroll, but not as far as in the scrolling case
//            if (totalVPixels > visibleVPixels) { // if it is not totally visible
                if (scrollSpeed > 0) // if this is 0, apply full delta
                    delta /= (virtualBlockResolution >> 1) / scrollSpeed; // do it a little slowly depending on distance
                inputPos += delta;
                if (inputPos < 0)
                    inputPos = 0;
                else {
                    if (inputPos >= totalVPixels - visibleVPixels)
                        inputPos = totalVPixels - visibleVPixels;
                }
//            }
        }
        return inputPos;
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public void updateCoordinates(int scrollSpeed, int toolboxWidth) {
        int mcmx = mcminos.getLevelObject().getVX();
        int mcmy = mcminos.getLevelObject().getVY();

        windowVPixelXPos = computeWindowCoordinate(windowVPixelXPos, mcmx, level.getScrollX(), getLevelWidth(), visibleWidthInVPixels, scrollSpeed);
        windowVPixelYPos = computeWindowCoordinate(windowVPixelYPos, mcmy, level.getScrollY(), getLevelHeight(), visibleHeightInVPixels, scrollSpeed);
        // minimap coordinates
        // TODO: only show, when not all fits on the screen
        // Check, if mcminos is rather in upper right part of screen or in lower left and position minimap
        // accordingly
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        int mcminosScreenX = projectionX + Graphics.vPixelToScreen(mcmx,windowVPixelXPos,width,currentResolutionBitsLeftShifter);
        //int mcminosScreenY = Graphics.vPixelToScreen(mcmy,windowVPixelYPos,height,currentResolutionBitsLeftShifter);
        if( mcminosScreenX > width*3/5 ) {
            miniMapLeft = true;
        } else if(mcminosScreenX < width*2/5){
            miniMapLeft = false;
        }
        if(miniMapLeft) {
            miniX = toolboxWidth;
            miniY = 0;
        } else {
            miniX = width - ((level.getWidth() + 2) << (virtualBlockResolutionExponent - virtual2MiniExponent));
            miniY = height - ((level.getHeight() + 2) << (virtualBlockResolutionExponent - virtual2MiniExponent));
        }
    }

    public void resize(int width, int height, int toolboxWidth) {
        // apply globally
        viewWidthInPixels = width;
        viewHeightInPixels = height;
        visibleWidthInPixels = width;
        visibleHeightInPixels = height;
        visibleWidthInVPixels = Util.shiftLeftLogical(visibleWidthInPixels, virtualBlockResolutionExponent - resolutionExponent );
        visibleHeightInVPixels = Util.shiftLeftLogical(visibleHeightInPixels, virtualBlockResolutionExponent - resolutionExponent );
        // eventually restrict visibility from Levelsettings
        if (visibleWidthInVPixels > level.getVisibleWidth() << virtualBlockResolutionExponent) {
            visibleWidthInVPixels = level.getVisibleWidth() << virtualBlockResolutionExponent;
            visibleWidthInPixels = level.getVisibleWidth() << resolutionExponent;
        }
        if (visibleHeightInVPixels > level.getVisibleHeight() << virtualBlockResolutionExponent) {
            visibleHeightInVPixels = level.getVisibleHeight() << virtualBlockResolutionExponent;
            visibleHeightInPixels = level.getVisibleHeight() << resolutionExponent;
        }
        // eventually restrict visibility by level-size
        if (visibleWidthInVPixels > level.getWidth() << virtualBlockResolutionExponent) {
            visibleWidthInVPixels = level.getWidth() << virtualBlockResolutionExponent;
            visibleWidthInPixels = level.getWidth() << resolutionExponent;
        }
        if (visibleHeightInVPixels > level.getHeight() << virtualBlockResolutionExponent) {
            visibleHeightInVPixels = level.getHeight() << virtualBlockResolutionExponent;
            visibleHeightInPixels = level.getHeight() << resolutionExponent;
        }
        /* obsolete due to new restriction concerning scrolling
         The following code also had a bug in cutting of the block (not from enough?)
         causing weird artifacts in some resolutions

        // eventually restrict due to non scrolling
        if(!getScrollX() && visibleWidthInPixels > viewWidthInPixels - resolution) {
            visibleWidthInPixels = viewWidthInPixels - resolution; // cut off a block
            visibleWidthInVPixels = Util.shiftLeftLogical(visibleWidthInPixels, virtualBlockResolutionExponent - resolutionExponent );
        }
        if(!getScrollY() && visibleWidthInPixels > viewHeightInPixels - resolution) {
            visibleHeightInPixels = viewHeightInPixels - resolution; // cut off a block
            visibleHeightInVPixels = Util.shiftLeftLogical(visibleHeightInPixels, virtualBlockResolutionExponent - resolutionExponent );
        }*/

        // finish up in computing related variables
        levelWidthInPixels = getLevelWidth() << resolutionExponent;
        levelHeightInPixels = getLevelHeight() << resolutionExponent;
        visibleWidthInBlocks = (visibleWidthInVPixels + virtualBlockResolution - 1)  >> virtualBlockResolutionExponent;
        visibleHeightInBlocks = (visibleHeightInVPixels + virtualBlockResolution - 1)  >> virtualBlockResolutionExponent;
        // Solution from here: http://gamedev.stackexchange.com/questions/68785/why-does-resizing-my-game-window-move-and-distort-my-rendering
        Matrix4 matrix = new Matrix4();
        projectionX = (width - visibleWidthInPixels) / 2;
        projectionY = (height - visibleHeightInPixels) / 2;
        matrix.setToOrtho2D(-projectionX, -projectionY, visibleWidthInPixels + 2 * projectionX, visibleHeightInPixels + 2 * projectionY);
//        matrix.setToOrtho2D(0, 0, width, height);
        batch.setProjectionMatrix(matrix);
        camera.setToOrtho(false,viewWidthInPixels,viewHeightInPixels);

        // add clipping
        Rectangle clipBounds = new Rectangle( projectionX, projectionY, visibleWidthInPixels, visibleHeightInPixels);
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);

        // resize minimap
        //virtual2MiniResolution = resolution >=64 ? 8 : 4;
        // set resolution based on size of level in relation to screen
        int hint = Math.min(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()) / Math.max(level.getWidth(),level.getHeight());
        if(hint < 16) virtual2MiniResolution = 4;
        else if(hint < 40) virtual2MiniResolution = 8;
        else virtual2MiniResolution = 16;
        virtual2MiniExponent = virtualBlockResolutionExponent - Util.log2binary(virtual2MiniResolution);

        // fully center mcminos
        updateCoordinates(0, toolboxWidth);

    }

    public void resize(int toolboxWidth) {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), toolboxWidth);
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


    public int getVisibleWidthInVPixels() {
        return visibleWidthInVPixels;
    }

    public int getVisibleHeightInVPixels() {
        return visibleHeightInVPixels;
    }

    public int getVisibleWidthInPixels() {
        return visibleWidthInPixels;
    }

    public int getVisibleHeightInPixels() {
        return visibleHeightInPixels;
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

    public int getProjectionX() {
        return projectionX;
    }

    public int getProjectionY() {
        return projectionY;
    }

    public int getWidthInPixels() {
        return viewWidthInPixels;
    }

    public int getHeightInPixels() {
        return viewHeightInPixels;
    }

    public Rectangle getScissors() {
        return scissors;
    }

    public int getCurrentResolutionBitsLeftShifter() {
        return currentResolutionBitsLeftShifter;
    }

    public int vPixelToScreen( int v, int vpixelpos, int levelPixelSize) {
        return  Graphics.vPixelToScreen(v,vpixelpos,levelPixelSize,currentResolutionBitsLeftShifter);
    }

    public int vPixelToScreenX( int v ) {
        return vPixelToScreen( v, windowVPixelXPos, levelWidthInPixels );
    }

    public int vPixelToScreenY( int v ) {
        return vPixelToScreen( v, windowVPixelYPos, levelHeightInPixels );
    }

    public void draw(boolean drawBackground) {
        level.draw(this, drawBackground);
    }

    public void drawMini(SpriteBatch batch) {
        level.drawMini(this, batch);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public int getMiniX() {
        return miniX;
    }

    public int getMiniY() {
        return miniY;
    }
}
