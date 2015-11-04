package com.mcminos.game;

/**
 * Created by ulno on 14.08.15.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * represent on of the graphical
 * entities in the game like mcminos itself, ghosts, walls or other things in the game
 * The class allows to retrieve corresponding textures for specific game/level objects
 */
public class Graphics {
    static int numberImagesLoaded = 0; // for progress-bar
    private char symbol;
    private int anchorX, anchorY; // already shifted to virtual resolution
    private int zIndex;
    private boolean moving;
    private int blockWidth, blockHeight;
    private int totalAnimationFrames; // total length in gameframes
    private int currentResolution = 0;

    /**
     * Shift how many bits to left to achieve the actual game resolution.
     * If virtualBlockResolution is 128 and actual resolution is 64 pixel per block,
     * this needs to be -1, because 128 << -1 = 64 (negative shifting shifts in other direction)
     */
    private int currentResolutionBitsLeftShifter = 0;

    /**
     * @return Width of graphics in level blocks
     */
    public int getWidth() {
        return blockWidth;
    }

    /**
     * @return Height of graphics in level blocks
     */
    public int getHeight() {
        return blockHeight;
    }


    // Remember all graphics for game
    static private ArrayList<Graphics> allGraphics = new ArrayList<Graphics>();

    final int precision = 10; // not more precise than 10ms -? 1/100s second
    private int[] timeList = null;

    /** Hierarchy here is: Hashmap of sizes mapped to to ArrayList of Images */
    private HashMap<Integer,ArrayList> ResolutionList = new HashMap<Integer, ArrayList>();
    // the reference step-list (each step references the correct image number) for animating this entity per category
    private HashMap<String,ArrayList> animationCategorySteps = new HashMap<String, ArrayList>();

    private ArrayList<IntPair> stepList = new ArrayList<IntPair>();
    // current, resolution specific Textures mapped to gameframe
    private Texture[] currentTextures = null;

    /**
     * Create a new Graphics
     * @param symbol // symbol in old Mcminos ascii representation
     * @param anchorX // center block point x (will be multiplied by virtualBlockResolution)
     * @param anchorY // center block point y (will be multiplied by virtualBlockResolution) - still viewed from top left, needs to be flipped
     * @param zIndex // for drawing order (layer)
     * @param moving // is this object static or does it move
     * @param blockWidth; // Width of graphics in blocks
     * @param blockHeight; // Height of graphics in blocks
     */
    Graphics(char symbol, int anchorX, int anchorY, int zIndex, boolean moving, int blockWidth, int blockHeight) {
        this.symbol = symbol;
        this.anchorX = anchorX << PlayWindow.virtualBlockResolutionExponent;
        this.anchorY = (blockHeight - anchorY - 1) << PlayWindow.virtualBlockResolutionExponent;
        this.zIndex = zIndex;
        this.moving = moving;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        totalAnimationFrames = 0;
        allGraphics.add(this);
    }

    long msToFrames( long ms) {
        return ms * Game.timeResolution / 1000;
    }

    // Is called at end of initialisation, when all graphics has been added
    void finishInit() {
        generateTimeList();
    }

    // category can include subcategory and is separated with a dot
    void addImage( String file, int resolution, int step ) {

        ArrayList textures;

        if( ResolutionList.containsKey(resolution)) {
            textures = ResolutionList.get( resolution );
        }
        else { // Resolution not in there
            textures = new ArrayList<Texture>();
            ResolutionList.put(resolution, textures);
        }

        Texture texture = new Texture( Gdx.files.internal( file ) );
        textures.add(texture);
        numberImagesLoaded += 1;
    }

    /**
     * @param step Reference nr for corresponding animation step
     * @param length length to display in ms
     */
    void addAnimationStep( int step, int length )
    {
        long frameLength = msToFrames(length);
        stepList.add( new IntPair(step, frameLength) );
        totalAnimationFrames += frameLength;
    }

    void generateTimeList( )
    {
        int size = (totalAnimationFrames + precision - 1) / precision;
        timeList = new int[size];
        int currentTime = 0;
        long nextAnimation = stepList.get(0).second;
        int currentAnimation = 0;
        int timeListIndex = 0;
        while( currentTime < totalAnimationFrames) {
            if( currentTime < nextAnimation ) {
                timeList[timeListIndex] = currentAnimation;
                timeListIndex ++;
                currentTime += precision;
            }
            else {
                currentAnimation ++;
                nextAnimation += stepList.get(currentAnimation).second;
            }
        }
    }

    int getAnimationIndex(long gameframe) {
        gameframe %= totalAnimationFrames;
        gameframe /= precision;
        return stepList.get(timeList[(int)gameframe]).first;
    }

    /**
     * Generic version for getting a texture for a time
     * @param resolution
     * @param gameframe
     * @return respective texture
     */
    Texture getTexture(int resolution, long gameframe) {
        ArrayList<Texture> textures = ResolutionList.get(resolution);
        return textures.get( getAnimationIndex(gameframe) );
    }

    /**
     * Set a specific resolution and extract corresponding tables to speed things up a bit
     */
    void setResolution( int resolution ) {
        currentTextures = new Texture[timeList.length]; // think if re-init necessary -> leak?
        if( ResolutionList.containsKey(resolution)) {
            currentResolution = resolution;
            currentResolutionBitsLeftShifter = Util.log2binary(currentResolution) - PlayWindow.virtualBlockResolutionExponent;

            for (int i = 0; i < timeList.length; i++) {
                currentTextures[i] = ((ArrayList<Texture>)ResolutionList.get(currentResolution))
                        .get(stepList.get(timeList[i]).first);
            }
        }
        // TODO: else exception?
    }

    Texture getTexture( long gameframe ){
        gameframe %= totalAnimationFrames;
        gameframe /= precision;
        return currentTextures[(int)gameframe];
    }

    /**
     * directly draw a Texture in given batch
     */
    public void draw( PlayWindow playwindow, SpriteBatch b, int x, int y) {
        b.draw(getTexture(playwindow.getGame().getGameFrame()), x, y);
    }

    public static int vPixelToScreen( int v, int vpixelpos, int levelPixelSize, int currentResolutionBitsLeftShifter) {
        int screen = Util.shiftLeftLogical(v - vpixelpos, currentResolutionBitsLeftShifter);
        screen = (screen + levelPixelSize) % levelPixelSize;
        return screen;
    }
    /**
     * Draw with offset to a batch in current resolution
     * Remember, level(0,0) is lower left corner due to libgdx' flipped windowVPixelYPos-axis
     *
     * @param vx0 virtualPixel x-coordinate (level block * virtualPixelResolution)
     * @param vy0 virtualPixel y-coordinate (level block * virtualPixelResolution)
     * @param animDelta offset to adapt animation
     */
    public void draw( PlayWindow playwindow, int vx0, int vy0, int animDelta ) {
        // let the installed scissor do the clipping, we just draw the respective image max 4 times
        ///////// first look at x
        int gamew = playwindow.levelWidthInPixels;
        int totalWidth = blockWidth << playwindow.resolutionExponent; // physical size of graphics
        int vlw = playwindow.getVPixelsLevelWidth(); // virtual levelwidth
        vx0 = (vx0 - anchorX + vlw) % vlw; // make sure it's not negative and apply anchor
        // get physical coordinates
        int x0 = vPixelToScreen(vx0, playwindow.windowVPixelXPos,gamew,currentResolutionBitsLeftShifter);

        /////////// do same for y
        int gameh = playwindow.levelHeightInPixels;
        int totalHeight = blockHeight << playwindow.resolutionExponent; // physical size of graphics
        int vlh = playwindow.getVPixelsLevelHeight(); // virtual levelwidth
        vy0 = (vy0 - anchorY + vlh) % vlh; // make sure it's not negative and apply anchor
        // get physical coordinates
        int y0 = vPixelToScreen(vy0, playwindow.windowVPixelYPos,gameh,currentResolutionBitsLeftShifter);

        // draw different parts to physical coordinates
        Texture t = getTexture(playwindow.getGame().getGameFrame() + animDelta );
        /*int maxww = playwindow.visibleWidthInPixels;
        int maxwh = playwindow.visibleHeightInPixels;*/
        // clipping should be done by scissors in playscreen
        // TODO: this seems slow optimize
        playwindow.batch.draw(t, x0, y0);
        playwindow.batch.draw(t, x0 - gamew, y0);
        playwindow.batch.draw(t, x0, y0 - gameh);
        playwindow.batch.draw(t, x0 - gamew, y0 - gameh);
/*        if( x0 < maxww && y0 < maxwh ) { // lower left corner in visible
            playwindow.batch.draw(t, x0, y0);
        }
        if( (x0 + totalWidth) % gamew < maxww && y0 < maxwh ) { // lower right corner visible
            playwindow.batch.draw(t, x0 - gamew, y0);
        }
        if( x0 < maxww && (x0 + totalWidth) % gamew < maxww && y0 < maxwh ) { // upper left corner visible
            playwindow.batch.draw(t, x0 - gamew, y0);
        }



        // Clipping correction for small screens, TODO: think about optimization
        if(x0 >= maxww && x0 > playwindow.levelWidthInPixels - totalWidth )
            x0 -= playwindow.levelWidthInPixels;
        if(y0 >= maxwh && y0 > playwindow.levelHeightInPixels - totalHeight )
            y0 -= playwindow.levelHeightInPixels;
        if(  (x0 < maxww) && (y0 < playwindow.visibleHeightInPixels) ) {
            playwindow.batch.draw(t, x0, y0, 0, totalHeight, x0w, y0h);
        }
        if( (x0 < maxww) && (y1 < maxwh)) {
                playwindow.batch.draw(t, x0, y1, 0, 0, x0w, y1h); }
            if( (vx1w > 0)
                    && (x1 < maxww) && (y1 < maxwh)) {
                playwindow.batch.draw(t, x1, y1, x0w, 0, x1w, y1h);
            }*/
    }

    /**
     * For the static part concerning all graphics
     */
    static void setResolutionAll( PlayWindow playwindow, int resolution ) {
        for (Graphics gfx : allGraphics) {
            gfx.setResolution( resolution );
        }
        playwindow.resize();
    }

    public int getzIndex() {
        return zIndex;
    }

    public int getAnimationFramesLength() {
        return totalAnimationFrames;
    }
}
