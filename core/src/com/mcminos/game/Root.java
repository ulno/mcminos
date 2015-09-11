package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the class having all static game content which needs to be accessed by all other modules.
 *
 */
public class Root {
    // constants
    public final static int timeResolution = 128; // How often per second movements are updated?
    public final static int virtualBlockResolution = 128; // How many virtual pixels is a block big (independent of actually used resolution), must be a power of 2
    public final static double baseSpeed = 2.0; // in blocks per second
    // derived constants
    public final static int timeResolutionExponent = Util.log2binary(timeResolution);
    public final static int virtualBlockResolutionExponent = Util.log2binary(virtualBlockResolution);
    // not needed (yet?) public final static int baseVPixelSpeedPerFrame = (int) Math.round(baseSpeed * virtualBlockResolution / timeResolution);

    public static SpriteBatch batch;
    public static Stage stage;
    public static BitmapFont defaultFont;
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
    private static Mover mcmMover;
    public static HashSet<LevelObject> movables=new HashSet<>(); // all moveables - mcminos
    static Semaphore updateLock = new Semaphore(1);
    private static LevelBlock lastBlock = null;
    static int bombs=0; // number of bombs carried by mcminos
    static int dynamites=0; // number of dynamites carried by mcminos
    static int keys=0; // number of keys carried by mcminos
    static int umbrellas = 0; // number of umbrellas carried by mcminos
    static int lives = 3; // number of lives left
    public static HashMap<String,Sound> soundList = new HashMap<>();

    public static void setResolution(int resolution) {
        Root.resolution = resolution;
        resolutionExponent = Util.log2binary(resolution);
        Graphics.setResolutionAll();
    }

    private static Root ourInstance = new Root();
    public static long gameFrame = 0;

    public static Root getInstance() {
        return ourInstance;
    }

    private Root() {
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public void updateWindowPosition() {

        windowVPixelXPos = computeWindowCoordinate(windowVPixelXPos, mcminos.getVX(), level.getScrollX(), getLevelWidth(), windowVPixelWidth);
        windowVPixelYPos = computeWindowCoordinate(windowVPixelYPos, mcminos.getVY(), level.getScrollY(), getLevelHeight(), windowVPixelHeight);

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
                windowPixelWidth = level.getVisibleWidth() << resolutionExponent;
            }
            if (windowVPixelHeight > level.getVisibleHeight() << virtualBlockResolutionExponent) {
                windowVPixelHeight = level.getVisibleHeight() << virtualBlockResolutionExponent;
                windowPixelHeight = level.getVisibleHeight() << resolutionExponent;
            }
            Root.fullPixelWidth = Root.getLevelWidth() << resolutionExponent;
            Root.fullPixelHeight = Root.getLevelHeight() << resolutionExponent;
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
        stage = new Stage(new ScreenViewport());
        defaultFont = new BitmapFont(Gdx.files.internal("fonts/liberation-sans-64.fnt"));
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        resolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        Root.setResolution(resolution);
        // create destination-object
        destination = new LevelObject(0,0,Entities.destination.getzIndex(), LevelObject.Types.Unspecified);
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
        int w = Root.getLevelWidth();
        int h = Root.getLevelHeight();
        int roundx = (vPixelX + (virtualBlockResolution >> 1)) >> virtualBlockResolutionExponent;
        int roundy = (vPixelY + (virtualBlockResolution >> 1)) >> virtualBlockResolutionExponent;
        //if( level.getScrollX() )
        roundx = (  roundx + w ) % w;
        //else
        //    roundx = Math.max(0,Math.min(w,roundx));
        //if( level.getScrollY() )
        roundy = ( roundy  + h ) % h;
        //else
        //    roundy = Math.max(0,Math.min(h,roundy));
        return level.get( roundx, roundy );
    }

    public static MoverDirectionChooser moverMcminos = new MoverDirectionChooser() {
        @Override
        public Mover.directions[] chooseDirection(LevelObject lo) {
            // mcminos
            if (destination.hasGfx()) { // destination is set
                // check screen distance
                int x = mcminos.getVX();
                int xdelta = x - destination.getVX(); // delta to center of destination (two centers substract)
                int xdiff = Math.abs(xdelta);
                if (xdiff <= virtualBlockResolution >> 1 || xdiff >= getVPixelsLevelWidth() - (virtualBlockResolution >> 1))
                    xdelta = 0;
                else {
                    //also allow this in non-scrolled levels
                    //if (getScrollX() && xdiff >= getVPixelsLevelWidth() >> 1)
                    if (xdiff >= getVPixelsLevelWidth() >> 1)
                        xdelta = (int) Math.signum(xdelta);
                    else
                        xdelta = -(int) Math.signum(xdelta);
                }
                int y = mcminos.getVY();
                int ydelta = y - destination.getVY(); // delta to center of destination (two centers substract)
                int ydiff = Math.abs(ydelta);
                if (ydiff <= virtualBlockResolution >> 1 || ydiff >= getVPixelsLevelHeight() - (virtualBlockResolution >> 1))
                    ydelta = 0;
                else {
                    // also in non-scroll levels
                    //if( getScrollY() && ydiff >= getVPixelsLevelHeight() >> 1 )
                    if (ydiff >= getVPixelsLevelHeight() >> 1)
                        ydelta = (int) Math.signum(ydelta);
                    else
                        ydelta = -(int) Math.signum(ydelta);
                }

                Mover.directions tryDirections[] = {Mover.directions.STOP, Mover.directions.STOP};
                int dircount = 0;
                if (ydelta > 0) tryDirections[dircount++] = Mover.directions.UP;
                if (ydelta < 0) tryDirections[dircount++] = Mover.directions.DOWN;
                if (xdelta > 0) tryDirections[dircount++] = Mover.directions.RIGHT;
                if (xdelta < 0) tryDirections[dircount++] = Mover.directions.LEFT;
                if (dircount > 1 && xdiff > ydiff) {
                    Mover.directions tmp = tryDirections[0];
                    tryDirections[0] = tryDirections[1];
                    tryDirections[1] = tmp;
                }
                return tryDirections;
            }
            return new Mover.directions[]{};
        }
    };

    public static MoverDirectionChooser moverRock = new MoverDirectionChooser() {
        @Override
        public Mover.directions[] chooseDirection(LevelObject lo) {
            return new Mover.directions[]{Mover.directions.STOP};
        }
    };

    /**
     * Start the moving thread which wil manage all movement of objects in the game
     */
    public void startMover() {
        mcmMover = new Mover( mcminos, 1.0, true, moverMcminos, Entities.mcminos_default_front, Entities.mcminos_default_up,
                Entities.mcminos_default_right, Entities.mcminos_default_down, Entities.mcminos_default_left);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                gameFrame++;

                doMovement();

            }
        }
                , 0, 1 / (float) timeResolution);
    }

    /**
     * This is called
     */
    private void doMovement() {
        // move everybody
        try { // needs to be synchronized against drawing
            Root.updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // move everybody
        for( LevelObject m : movables ) {
            m.move();
        }

        mcmMover.calculateDirection();
        mcmMover.move();

        checkCollisions();

        Root.updateLock.release();
    }

    /**
     *     Check collisions (mainly if mcminos found something and can collect it)
     * @return
     */
    private void checkCollisions() {
        // check if something can be collected (only when full on field)
        if((mcminos.getVX() % virtualBlockResolution  == 0) && (mcminos.getVY() % virtualBlockResolution == 0)) {
            LevelBlock currentBlock = getLevelBlockFromVPixel( mcminos.getVX(), mcminos.getVY() );
            if( currentBlock.hasPill() )
            {
                soundPlay("knurps");
                currentBlock.removePill();
            }
            // check, if mcminos actually moved or if it's the same field as last time
            if(currentBlock != lastBlock) {
                for( LevelObject b:currentBlock.getCollectibles()) {
                    switch( b.getType() ) {
                        case Bomb:
                            soundPlay("tools");
                            bombs ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            break;
                        case Dynamite:
                            soundPlay("tools");
                            dynamites ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            break;
                        case Key:
                            soundPlay("tools");
                            keys ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            break;
                        case Umbrella:
                            soundPlay("tools");
                            umbrellas ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            break;
                        case Live:
                            soundPlay("life");
                            lives ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            break;
                    }
                }
            }
            lastBlock = currentBlock;
        }
    }
    
    /*
    void snd_killed( void )
{
	play_sound( GHOSTS, 3, 300 );
}

 Sound fr Powerpill 
    void snd_power( void )
    {
        play_sound( POWER, 2, 300 );
    }

     Sound fr Tool 
    void snd_tool( void )
    {
        play_sound( TOOLS, 1, 30 );
    }

     Sound fr neues Leben 
    void snd_life( void )
    {
        play_sound( NEWLIFE, 2, 50 );
    }

     Sound fr nchsten Level 
    void snd_levelend( void )
    {
        play_sound(APPLAUS, 255, 800 );
    }

     Sound fr neues Leben 
    void snd_newlife( void )
    {
        play_sound( NEWLIFE, 2, 50 );
    }

     Sound fr Vergiftung 
    void snd_pacpoison( void )
    {
        play_sound( POISON, 2, 300 );
    }

     Sound fr Gegengift 
    void snd_antidot( void )
    {
        play_sound( TOOLS, 1, 30 );
    }

     Sound fr Explosion 
    void snd_explosion( void )
    {
        play_sound( EXPLOSION, 4, 300 );
    }

     Sound fr Spiegel 
    void snd_mirror( void )
    {
        play_sound( FADE, 2, 200 );
    }

     Sound fr Speedup 
    void snd_speedup( void )
    {
        play_sound( SPEEDUP, 2, 300 );
    }

     Sound fr Slowdown 
    void snd_slowdown( void )
    {
        play_sound( SLOWDOWN, 2, 200 );
    }

     Sound fr drunken 
    void snd_drunken( void )
    {
        play_sound( ETHANOLE, 2, 200 );
    }

     Sound fr killall 
    void snd_killall( void )
    {
        play_sound( KILLALL, 2, 100 );
    }

     Sound fr letter 
    void snd_letter( void )
    {
        play_sound( TOOLS, 1, 30 );
    }

     Sound fr Lochvergrerung 
    void snd_hole( void )
    {
        play_sound( HOLEGROW, 2, 20 );
    }

     Sound fr fallenden Stein bzw. aufbrechenden Boden 
    void snd_rockfall( void )
    {
        play_sound( SPLASH, 3, 200 );
    }

     Sound fr warpin 
    void snd_warpin( void )
    {
        play_sound( BLUB, 2, 200 );
    }

     Sound fr warpout 
    void snd_warpout( void )
    {
        play_sound( BULB, 2, 200 );
    }

     Sound fr Tre ffnen 
    void snd_opendoor( void )
    {
        play_sound( QUIETSCH, 2, 200 );
    }

     Sound fr Tre schlieen 
    void snd_closedoor( void )
    {
        play_sound( RUMS, 2, 200 );
    }

     Sound fr Geist erschlagen 
    void snd_beat( void )
    {
        play_sound( GOTYOU, 2, 100 );
    }

     Sound fr Stein schieben 
    void snd_moverock( void )
    {
        play_sound( SND_MOVEROCK, 2, 120 );
    }

     Sound fr fallenden Main
    void snd_falling( void )
    {
        play_sound( FALLING, 2, 300 );
    }

     Sound fr brennende Zndschnur 
    void snd_zisch( void )
    {
        play_sound( ZISCH, 2, 400 );
    }

     Uhrticken 
    void snd_tick( void )
    {
        play_sound( TICK, 0, 0 );
    }

     Beep 
    void snd_beep( void )
    {
        play_sound( BEEP, 2, 30 );}
    */
    
    

    public static void loadSounds() {
        String[] soundNames=new String[]{"aaahhh",
                "applaus",
                "beep",
                "blub",
                "bulb",
                "error",
                "ethanole",
                "explosio",
                "fade2",
                "fade3",
                "fade",
                "falling",
                "ghosts",
                "gotyou",
                "hihat",
                "holegrow",
                "killall",
                "knurps",
                "life",
                "moverock",
                "orchestr",
                "panflute",
                "poison",
                "power2",
                "power",
                "quietsch",
                "rums",
                "slowdown",
                "speedup",
                "splash",
                "tick",
                "tools",
                "trommeln",
                "zisch"};
        for( String s:soundNames ) {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/" + s + ".wav"));
            soundList.put(s, sound);
        }
    }

    public static void soundPlay(String s) {
        soundList.get(s).play(1.0f);
    }
}
