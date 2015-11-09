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

    public PlayWindow(SpriteBatch batch, OrthographicCamera camera, Level level, McMinos mcminos) {
        this.batch = batch;
        this.camera = camera;
        this.level = level;
        this.game = level.getGame();
        this.mcminos = mcminos;
    }

/*    seems obsolete public void init() {
        gfx = Entities.getInstance();
        // Load after graphics have been loaded
        windowVPixelXPos = 0;
        windowVPixelYPos = 0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
    }*/


    public void setResolution(int resolutionCounter) {
        resolution = Entities.resolutionList[resolutionCounter];
        resolutionExponent = Util.log2binary(resolution);
        Graphics.setResolutionAll(this, resolution);
        currentResolutionBitsLeftShifter = Util.log2binary(resolution) - PlayWindow.virtualBlockResolutionExponent;
        resize();
    }

    public int setClosestResolution( int resolutionValue ) {
        int delta = 0x1000000;
        int counter = -1;
        for( int i=0; i<Entities.resolutionList.length; i++ ) {
            int diff = Math.abs( resolutionValue - Entities.resolutionList[i] );
            if( diff < delta) {
                delta = diff;
                counter = i;
            }
        }
        setResolution(counter);
        return counter;
    }


    /**
     *
     * @param inputPos current window corner position in virtual pixels
     * @param mcmPos current mcminos position in virtual pixels
     * @param scroll is scrolling for current level and respective coordinate switched on?
     * @param totalBlocks total number of blocks for this coordinate (level block width or height)
     * @param visibleVPixels total number of visible virtual pixels in this coordinate
     * @return
     */
    private int computeWindowCoordinate(int inputPos, int mcmPos, boolean scroll, int totalBlocks, int visibleVPixels) {
        // We compute the view based on McMinos' position
        // when we are calling this, we try to make sure McMinos is visible near the center of the screen
        // However, scrollability of the level needs to be respected.

        int totalVPixels = totalBlocks << virtualBlockResolutionExponent;
/*        if ( totalVPixels > visibleVPixels) { // if it is not totally visible -- now we scroll allways */
            int center = (inputPos + (visibleVPixels >> 1)) % totalVPixels; // center of the visible screen
            // compute the distance of mcminos from the center
            int delta = mcmPos - center; // < 0 means mcminos is under/left of center
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
                inputPos += delta;
                if (inputPos < 0)
                    inputPos = 0;
                else {
                    if (inputPos >= totalVPixels - visibleVPixels)
                        inputPos = totalVPixels - visibleVPixels;
                }
            }
/*        }
        else { // the visible area is bigger than the actual level
            // if level is small, it needs to be centered
            // When no scrolling is enabled, we
            // also need to account for some black borders to allow giving the direction by setting the
            // destination field
            // if( ! scroll )// not scroll and not too small -> make sure level aligned
            inputPos = 0;
            //inputPos = ((totalBlocks << virtualBlockResolutionExponent) -( visibleVPixels - totalVPixels ) / 2 ) % (totalBlocks << virtualBlockResolutionExponent);
        } */
        return inputPos;
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public void updateCoordinates() {
        if( ! game.isToolboxActivated()) {
            windowVPixelXPos = computeWindowCoordinate(windowVPixelXPos, mcminos.getLevelObject().getVX(), level.getScrollX(), getLevelWidth(), visibleWidthInVPixels);
            windowVPixelYPos = computeWindowCoordinate(windowVPixelYPos, mcminos.getLevelObject().getVY(), level.getScrollY(), getLevelHeight(), visibleHeightInVPixels);
        }
    }

    public void resize(int width, int height) {
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

}
