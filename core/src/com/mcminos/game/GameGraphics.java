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
    private int anchorX, anchorY;
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
     * @param vPixelX virtualPixel x-coordinate (level block * virtualPixelResolution)
     * @param vPixelY virtualPixel y-coordinate (level block * virtualPixelResolution)
     */
    void draw( int vPixelX, int vPixelY) {
        int gamew = Game.fullPixelWidth;
        int gameh = Game.fullPixelHeight;
        int pixelx = Util.shiftLeftLogical(vPixelX + anchorX - Game.windowVPixelXPos, currentResolutionBitsLeftShifter); // TODO: Think, do we have to properly round here?
        if( Game.getScrollX() )
            pixelx = (pixelx + gamew + currentResolution - 1) % gamew - currentResolution + 1;
        int pixely = Util.shiftLeftLogical(vPixelY + anchorY - Game.windowVPixelYPos, currentResolutionBitsLeftShifter);
        if( Game.getScrollY() )
            pixely = (pixely + gameh + currentResolution - 1) % gameh - currentResolution + 1;
        // only draw if visible (some part of the rectangle is in visible area)
        if( (pixelx + currentResolution > 0) && (pixely + currentResolution > 0)
            && (pixelx < Game.windowPixelWidth) && (pixely < Game.windowPixelHeight) ) {
            Game.batch.draw(getTexture(Game.gameTime), pixelx, pixely);
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
