package com.mcminos.game;

/**
 * Created by ulno on 14.08.15.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * represent on of the graphical
 * entities in the game like mcminos itself, ghosts, walls or other things in the game
 * The class allows to retrieve corresponding textures for specific game/level objects
 */
public class Graphics {
    static int numberImagesLoaded = 0; // for progress-bar
    private final int graphicsIndex;
    private char symbol;
    private int anchorX, anchorY; // already shifted to virtual resolution
    private int zIndex;
    private boolean moving;
    private int blockWidth, blockHeight;
    private int totalAnimationFrames; // total length in gameframes
    private int currentResolution = 0;
    private String name = null; // name of the graphics for index in HashMap

    /**
     * Shift how many bits to left to achieve the actual game resolution.
     * If virtualBlockResolution is 128 and actual resolution is 64 pixel per block,
     * this needs to be -1, because 128 << -1 = 64 (negative shifting shifts in other direction)
     */
    private int currentResolutionBitsLeftShifter = 0;


    // Remember all graphics for game
    private static ArrayList<Graphics> allGraphics = new ArrayList<Graphics>();
    private static HashMap<String,Graphics> allGraphicsByName = new HashMap<>();

    final int precision = 10; // not more precise than 10ms -? 1/100s second
    private int[] timeList = null;

    /** Hierarchy here is: Hashmap of sizes mapped to to ArrayList of Images */
    private HashMap<Integer,ArrayList> resolutionList = new HashMap<Integer, ArrayList>();
    // the reference step-list (each step references the correct image number) for animating this entity per category
    private HashMap<String,ArrayList> animationCategorySteps = new HashMap<String, ArrayList>();

    private ArrayList<IntPair> stepList = new ArrayList<IntPair>();
    // current, resolution specific Textures mapped to gameframe
    private TextureRegion[] currentTextures = null;

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
        graphicsIndex = allGraphics.size()-1;
    }

    long msToFrames( long ms) {
        return ms * Game.timeResolution / 1000;
    }

    // Is called at end of initialisation, when all graphics has been added
    void finishInit() {
        generateTimeList();
    }

    // category can include subcategory and is separated with a dot
    void addImage( TextureAtlas atlas, String file, int resolution, int step ) {

        ArrayList textures;

        if( resolutionList.containsKey(resolution)) {
            textures = resolutionList.get( resolution );
        }
        else { // Resolution not in there
            textures = new ArrayList<Texture>();
            resolutionList.put(resolution, textures);
        }

        TextureRegion ar = atlas.findRegion(resolution + "/" + file);

//        Texture texture = new Texture( Gdx.files.internal( file ) );
        textures.add(ar);
        numberImagesLoaded += 1;

        if(name == null) { // not yet given
            // compute basename in removing animation number
            name = file.split("_[0-9]*$")[0];
            allGraphicsByName.put(name, this);
        }
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

    public TextureRegion getTextureDirectStep(int res, int gfxStep) {
        ArrayList<TextureRegion> textures = resolutionList.get(res);
        return textures.get( gfxStep );
    }

    /**
     * Generic version for getting a texture for a time
     * @param resolution
     * @param gameframe
     * @return respective texture
     */
    TextureRegion getTexture(int resolution, long gameframe) {
        ArrayList<TextureRegion> textures = resolutionList.get(resolution);
        return textures.get( getAnimationIndex(gameframe) );
    }

    /**
     * Set a specific resolution and extract corresponding tables to speed things up a bit
     */
    void setResolution( int resolution ) {
        currentTextures = new TextureRegion[timeList.length]; // think if re-init necessary -> leak?
        if( resolutionList.containsKey(resolution)) {
            currentResolution = resolution;
            currentResolutionBitsLeftShifter = Util.log2binary(currentResolution) - PlayWindow.virtualBlockResolutionExponent;

            for (int i = 0; i < timeList.length; i++) {
                currentTextures[i] = ((ArrayList<TextureRegion>) resolutionList.get(currentResolution))
                        .get(stepList.get(timeList[i]).first);
            }
        }
        // TODO: else exception?
    }

    TextureRegion getTexture( long gameframe ){
        gameframe %= totalAnimationFrames;
        gameframe /= precision;
        return currentTextures[(int)gameframe];
    }

    TextureRegion getTextureMini( int miniResolution ) {
        ArrayList textures;

        if( resolutionList.containsKey(miniResolution)) {
            textures = resolutionList.get( miniResolution );
        } else {
            return null;
        }
        if( textures.size() > 0)
            return (TextureRegion) textures.get(0);
        return null;
    }

        /**
         * directly draw a Texture in given batch
         */
    public void draw( PlayWindow playwindow, SpriteBatch b, int x, int y) {
        b.draw(getTexture(playwindow.getGame().getAnimationFrame()), x, y);
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
    public void draw( PlayWindow playwindow, Level level, int vx0, int vy0, int animDelta ) {
        // let the installed scissor do the clipping, we just draw the respective image max 4 times
        ///////// first look at x
        int gamew = playwindow.levelWidthInPixels;
        int totalWidth = blockWidth << playwindow.resolutionExponent; // physical size in pixels of graphics
        int vlw = level.getVPixelsWidth(); // virtual levelwidth
        vx0 = (vx0 - anchorX + vlw) % vlw; // make sure it's not negative and apply anchor
        // get physical coordinates
        int x0 = vPixelToScreen(vx0, playwindow.windowVPixelXPos,gamew,currentResolutionBitsLeftShifter);
        /////////// do same for y
        int gameh = playwindow.levelHeightInPixels;
        int totalHeight = blockHeight << playwindow.resolutionExponent; // physical size in pixels of graphics
        int vlh = level.getVPixelsHeight(); // virtual levelwidth
        vy0 = (vy0 - anchorY + vlh) % vlh; // make sure it's not negative and apply anchor
        // get physical coordinates
        int y0 = vPixelToScreen(vy0, playwindow.windowVPixelYPos,gameh,currentResolutionBitsLeftShifter);

        // draw different parts to physical coordinates
        TextureRegion t = getTexture(playwindow.getGame().getAnimationFrame() + animDelta );
        // clipping is done by scissors in playscreen
        // TODO: this seems slow, optimize in figuring out what is (at least partly) visible
        int maxww = playwindow.visibleWidthInPixels;
        int x1 = (gamew + x0 + totalWidth - 1) % gamew;
        int maxwh = playwindow.visibleHeightInPixels;
        int y1 = (gameh + y0 + totalHeight - 1) % gameh;
        boolean xcl = x0 < maxww; // left corner in visible area
        boolean xcr = x1 < maxww; // right corner
        boolean ycb = y0 < maxwh; // bottom corner
        boolean yct = y1 < maxwh; // top corner

        // TODO: check if just drawing all four isn't faster
        if(xcl && ycb) playwindow.batch.draw(t, x0, y0); // left bottom visible
        if(xcr && ycb) playwindow.batch.draw(t, x0 - gamew, y0); // right bottom
        if(xcl && yct) playwindow.batch.draw(t, x0, y0 - gameh); // left top
        if(xcr && yct) playwindow.batch.draw(t, x0 - gamew, y0 - gameh); // right top
    }

    public static int virtualToMiniX( PlayWindow playwindow, Level level, int vx, int anchorX ) {
//        return Gdx.graphics.getWidth() - ((level.getVPixelsWidth() - vx + anchorX ) >> playwindow.virtual2MiniExponent) - playwindow.virtual2MiniResolution;
        return playwindow.getMiniX() + ((vx - anchorX ) >> playwindow.virtual2MiniExponent) + playwindow.virtual2MiniResolution;
    }

    public static int virtualToMiniY( PlayWindow playwindow, Level level, int vy, int anchorY ) {
//        return Gdx.graphics.getHeight() - ((level.getVPixelsHeight() - vy + anchorY ) >> playwindow.virtual2MiniExponent) - playwindow.virtual2MiniResolution;
        return playwindow.getMiniY() + ((vy - anchorY ) >> playwindow.virtual2MiniExponent) + playwindow.virtual2MiniResolution;
    }

    public void drawMini( PlayWindow playwindow, Level level, SpriteBatch minibatch, int vx, int vy, int animDelta ) {
        if(zIndex>=200) { // only draw non-backgrounds
            // compute for upper right corner
            int x = virtualToMiniX(playwindow, level, vx, anchorX);
            int y = virtualToMiniY(playwindow, level, vy, anchorY);
            TextureRegion t = getTextureMini(playwindow.virtual2MiniResolution);
            if (t != null)
                minibatch.draw(t, x, y);
        }
    }

        /**
         * For the static part concerning all graphics
         */
    static void setResolutionAll( PlayWindow playwindow, int resolution, int toolboxWidth ) {
        for (Graphics gfx : allGraphics) {
            gfx.setResolution( resolution );
        }
        playwindow.resize(toolboxWidth);
    }

    public int getzIndex() {
        return zIndex;
    }

    public int getAnimationFramesLength() {
        return totalAnimationFrames;
    }

/*    public int getAllGraphicsIndex() {
        return graphicsIndex;
    }

    public static Graphics getByIndex(int index ) {
        if(index>=0 && index < allGraphics.size())
            return allGraphics.get(index);
        else
            return null;
    } */

    public String getName() {
        return name;
    }

    public static Graphics getByName(String name) {
        if(name.length() == 0) return null;
        return allGraphicsByName.get(name);
    }

}
