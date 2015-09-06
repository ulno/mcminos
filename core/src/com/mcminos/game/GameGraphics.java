package com.mcminos.game;

/**
 * Created by ulno on 14.08.15.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * represent on of the graphical
 * entities in the game like mcminos itself, ghosts, walls or other things in the game
 * The class allows to retrieve corresponding textures for specific game/level objects
 */
public class GameGraphics {
    private char symbol;
    private int anchorX, anchorY; // already shifted to virtual resolution
    private int zIndex;
    private boolean moving;
    private int blockWidth, blockHeight;
    private int totalAnimationLength;
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
    static private ArrayList<GameGraphics> allGraphics = new ArrayList<GameGraphics>();

    final int precision = 10; // not more precise than 10ms -? 1/100s second
    private int[] timeList = null;

    /** Hierarchy here is: Hashmap of sizes mapped to to ArrayList of Images */
    private HashMap<Integer,ArrayList> ResolutionList = new HashMap<Integer, ArrayList>();
    // the reference step-list (each step references the correct image number) for animating this entity per category
    private HashMap<String,ArrayList> animationCategorySteps = new HashMap<String, ArrayList>();

    private ArrayList<IntPair> stepList = new ArrayList<IntPair>();
    // current, resolution specific Textures mapped to gametime
    private Texture[] currentTextures = null;

    /**
     * Create a new GameGraphics
     * @param symbol // symbol in old Mcminos ascii representation
     * @param anchorX // center block point x (will be multiplied by virtualBlockResolution)
     * @param anchorY // center block point y (will be multiplied by virtualBlockResolution)
     * @param zIndex // for drawing order (layer)
     * @param moving // is this object static or does it move
     * @param blockWidth; // Width of graphics in blocks
     * @param blockHeight; // Height of graphics in blocks
     */
    GameGraphics(char symbol, int anchorX, int anchorY, int zIndex, boolean moving, int blockWidth, int blockHeight) {
        this.symbol = symbol;
        this.anchorX = anchorX << Game.virtualBlockResolutionExponent;
        this.anchorY = anchorY << Game.virtualBlockResolutionExponent;
        this.zIndex = zIndex;
        this.moving = moving;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        totalAnimationLength = 0;
        allGraphics.add(this);
    }

    // Is called at end of initialisation, when all graphics has been added
    void finishInit() {
        generateTimeList();
    }

    // category can include subcategory and is separeted with a dot
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
    }

    /**
     * @param step Reference nr for corresponding animation step
     * @param length length to display in ms
     */
    void addAnimationStep( int step, int length )
    {
        stepList.add( new IntPair(step, length) );
        totalAnimationLength += length;
    }

    void generateTimeList( )
    {
        int size = (totalAnimationLength + precision - 1) / precision;
        timeList = new int[size];
        int currentTime = 0;
        int nextAnimation = stepList.get(0).second;
        int currentAnimation = 0;
        int timeListIndex = 0;
        while( currentTime < totalAnimationLength ) {
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

    int getAnimationIndex(long gametime) {
        gametime %= totalAnimationLength;
        gametime /= precision;
        return stepList.get(timeList[(int)gametime]).first;
    }

    /**
     * Generic version for getting a texture for a time
     * @param resolution
     * @param gametime
     * @return respective texture
     */
    Texture getTexture(int resolution, long gametime) {
        ArrayList<Texture> textures = ResolutionList.get(resolution);
        return textures.get( getAnimationIndex(gametime) );
    }

    /**
     * Set a specific resolution and extract corresponding tables to speed things up a bit
     */
    void setResolution( ) {
        currentTextures = new Texture[timeList.length]; // think if re-init necessary -> leak?
        if( ResolutionList.containsKey(Game.resolution)) {
            currentResolution = Game.resolution;
            currentResolutionBitsLeftShifter = Util.log2binary(currentResolution) - Game.virtualBlockResolutionExponent;

            for (int i = 0; i < timeList.length; i++) {
                currentTextures[i] = ((ArrayList<Texture>)ResolutionList.get(currentResolution))
                        .get(stepList.get(timeList[i]).first);
            }
        }
        // TODO: else exception?
    }

    Texture getTexture( long gametime ){
        gametime %= totalAnimationLength;
        gametime /= precision;
        return currentTextures[(int)gametime];
    }

    /**
     * Draw with offset to a batch in current resolution
     * Remember, level(0,0) is lower left corner due to libgdx' flipped windowVPixelYPos-axis
     *
     * @param vx0 virtualPixel x-coordinate (level block * virtualPixelResolution)
     * @param vy0 virtualPixel y-coordinate (level block * virtualPixelResolution)
     */
    void draw( int vx0, int vy0) {
        // As we can be in a corner, clipping needs to be respected
        // Therefore compute the four pieces which could be wrapped around
        // compute them first completely in virtual coordinates
        // there are basically two important x coordinates (left and middle, x0 and x1)
        // and two important y coordinates (down and middle, y0 and y1)
        // lower left corner is given as vx0 and vy0
        // if x1 and y1 used they will be usually 0, the split will be decided via value!=0 in vx1w or vy1h
        // there is a problem when window is too small and the drawing shoudl start left or under the current windowcorner

        int gamew = Game.fullPixelWidth;
        int gameh = Game.fullPixelHeight;

        // first look at x
        int vTotalWidth = blockWidth << Game.virtualBlockResolutionExponent; // virtual size of graphics
        int totalWidth = blockWidth << Game.resolutionExponent; // physical size of graphics
        int vx0w=vTotalWidth, vx1w=0;
        int vlw = Game.getVPixelsLevelWidth(); // virtual levelwidth
        vx0 = (vx0 - anchorX + vlw) % vlw; // make sure it's not negative and apply anchor
        int vx1 = (vlw - anchorX) % vlw;
        if( vx0 + vTotalWidth > vlw ) { // if it's outside the level bounds
            // vx1 = 0; will always be 0
            vx0w = vlw - vx0;
            vx1w = vTotalWidth - vx0w;
        }
        // get physical coordinates
        int x0 = Util.shiftLeftLogical(vx0 - Game.windowVPixelXPos, currentResolutionBitsLeftShifter);
        int x1 = Util.shiftLeftLogical(vx1 - Game.windowVPixelXPos, currentResolutionBitsLeftShifter);
        // always wrap around if( Game.getScrollX() ) {
        //x0 = (x0 + gamew + totalWidth - 1) % gamew - totalWidth + 1;
        //x1 = (x1 + gamew + totalWidth - 1) % gamew - totalWidth + 1;
        x0 = (x0 + gamew ) % gamew;
        x1 = (x1 + gamew ) % gamew;
        //}
        // get physical widths
        int x0w = Util.shiftLeftLogical(vx0w, currentResolutionBitsLeftShifter);
        int x1w = Util.shiftLeftLogical(vx1w, currentResolutionBitsLeftShifter);

        // do same for y
        int vTotalHeight = blockHeight << Game.virtualBlockResolutionExponent; // virtual size of graphics
        int totalHeight = blockHeight << Game.resolutionExponent; // physical size of graphics
        int vy0h = vTotalHeight, vy1h=0;
        int vlh = Game.getVPixelsLevelHeight(); // virtual levelwidth
        vy0 = (vy0 - anchorY + vlh) % vlh; // make sure it's not negative and apply anchor
        int vy1 = (vlh - anchorY) % vlh;
        if( vy0 + vTotalHeight > vlh ) { // if it's outside the level bounds
            // vy1 = 0; will always be 0
            vy0h = vlh - vy0;
            vy1h = vTotalHeight - vy0h;
        }
        // get physical coordinates
        int y0 = Util.shiftLeftLogical(vy0  - Game.windowVPixelYPos, currentResolutionBitsLeftShifter);
        int y1 = Util.shiftLeftLogical(vy1  - Game.windowVPixelYPos, currentResolutionBitsLeftShifter);
        // if( Game.getScrollY() ) { // allways wrap around
        //y0 = (y0 + gameh + totalHeight -1) % gameh  - totalHeight + 1;
        //y1 = (y1 + gameh + totalHeight -1) % gameh  - totalHeight + 1;
        y0 = (y0 + gameh) % gameh;
        y1 = (y1 + gameh) % gameh;
        // }
        // get physical heights
        int y0h = Util.shiftLeftLogical(vy0h, currentResolutionBitsLeftShifter);
        int y1h = Util.shiftLeftLogical(vy1h, currentResolutionBitsLeftShifter);

        // draw different parts to physical coordinates
        // only draw if visible (some part of the rectangle is in visible area)
        Texture t = getTexture(Game.gameTime);
        int maxww = Game.windowPixelWidth;
        int maxwh = Game.windowPixelHeight;
        // Clipping correction for small screens, TODO: think about optimization
        if(x0 >= maxww && x0 > Game.fullPixelWidth - totalWidth )
            x0 -= Game.fullPixelWidth;
        if(x1 >= maxww && x1 > Game.fullPixelWidth - totalWidth )
            x1 -= Game.fullPixelWidth;
        if(y0 >= maxwh && y0 > Game.fullPixelHeight - totalHeight )
            y0 -= Game.fullPixelHeight;
        if(y1 >= maxwh && y1 > Game.fullPixelHeight - totalHeight )
            y1 -= Game.fullPixelHeight;
        if(  (x0 < maxww) && (y0 < Game.windowPixelHeight) ) {
            //Game.batch.draw(getTexture(Game.gameTime), x0, y0);
            Game.batch.draw(t, x0, y0, 0, totalHeight - y0h, x0w, y0h);
        }
        if( (vx1w > 0)
                && (x1 < maxww) && (y0 < maxwh)) {
            Game.batch.draw(t, x1, y0, x0w, totalHeight - y0h, x1w, y0h);
        }
        if(vy1h > 0) {
            if( (x0 < maxww) && (y1 < maxwh)) {
                Game.batch.draw(t, x0, y1, 0, 0, x0w, y1h); }
            if( (vx1w > 0)
                    && (x1 < maxww) && (y1 < maxwh)) {
                Game.batch.draw(t, x1, y1, x0w, 0, x1w, y1h);
            }
        }

    }

    /**
     * For the static part concerning all graphics
     */
    static void setResolutionAll( ) {
        for (GameGraphics gfx : allGraphics) {
            gfx.setResolution();
        }
        Game.resize();
    }

    public int getzIndex() {
        return zIndex;
    }
}
