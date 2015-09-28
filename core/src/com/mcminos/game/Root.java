package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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
    private static long gameFrame = 0; // The game time - there is a getter for this
    public final static int virtualBlockResolution = 128; // How many virtual pixels is a block big (independent of actually used resolution), must be a power of 2
    public final static int baseSpeed = 2; // base speed of all units (kind of the slowest anybody usually moves) in blocks per second
    // derived constants
    public final static int timeResolutionExponent = Util.log2binary(timeResolution);
    public final static int virtualBlockResolutionExponent = Util.log2binary(virtualBlockResolution);
    // not needed (yet?) public final static int baseVPixelSpeedPerFrame = (int) Math.round(baseSpeed * virtualBlockResolution / timeResolution);
    public static final String UISKIN_DEFAULT = "uiskins/default/uiskin.json";

    public static SpriteBatch batch;
    public static BitmapFont defaultFont;
    public static Skin defaultSkin;
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
    private static Main main;
    static Semaphore updateLock = new Semaphore(1);
    static private Timer.Task timerTask = null;
    static private Random randomGenerator = new Random();

    static String currentLevelName = null;

    // Level/Game specific statics
    // TODO: consider making these non-static
    public static Level level;
    public static LevelObject mcminos;
    private static Mover mcmMover;
    static int chocolates; // number of chocolates carried by mcminos
    static int bombs; // number of bombs carried by mcminos
    static int dynamites; // number of dynamites carried by mcminos
    static int keys; // number of keys carried by mcminos
    static int umbrellas; // number of umbrellas carried by mcminos
    static int landmines; // number of umbrellas carried by mcminos
    static int lives; // number of lives left
    static int score; // current score

    public static FrameTimer frameTimer;
    private static int mcminosSpeed = baseSpeed;
    private static int ghostSpeed[] = {baseSpeed,baseSpeed,baseSpeed,baseSpeed};
    private static int mcminosSpeedFactor = 1;
    private static int ghostSpeedFactor = 1;
    public static int powerDuration = 0;
    public static int umbrellaDuration = 0;

    public static ArrayList<LevelObject> movables; // all moveables - i.e. mcminos
    private static LevelBlock lastBlock;
    private static boolean toolboxShown;
    private static boolean destinationSet; // was a destination set (and neds to be shown)
    public static LevelObject destination;

    public static HashMap<String,Sound> soundList = new HashMap<>();
    public static String[] soundNames = new String[]{"aaahhh",
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
            "rumble",
            "slowdown",
            "speedup",
            "splash",
            "tick",
            "tools",
            "trommeln",
            "wind",
            "zisch"};

    public static void setResolution(int resolution) {
        Root.resolution = resolution;
        resolutionExponent = Util.log2binary(resolution);
        Graphics.setResolutionAll();
    }

    private static Root ourInstance = new Root();

    public static Root getInstance() {
        return ourInstance;
    }

    public static boolean isToolboxShown() {
        return toolboxShown;
    }

    public static void setToolboxShown(boolean toolboxShown) {
        Root.toolboxShown = toolboxShown;
    }

    private Root() {
        batch = new SpriteBatch();

        defaultFont = new BitmapFont(Gdx.files.internal("fonts/liberation-sans-64.fnt"));
        defaultSkin = new Skin( Gdx.files.internal(UISKIN_DEFAULT) );
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
    }

    /**
     * Update the position of the currently seen viewable window
     */
    public static void updateWindowPosition() {
        if( ! toolboxShown) {
            windowVPixelXPos = computeWindowCoordinate(windowVPixelXPos, mcminos.getVX(), level.getScrollX(), getLevelWidth(), windowVPixelWidth);
            windowVPixelYPos = computeWindowCoordinate(windowVPixelYPos, mcminos.getVY(), level.getScrollY(), getLevelHeight(), windowVPixelHeight);
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
    private static int computeWindowCoordinate(int inputPos, int mcmPos, boolean scroll, int totalBlocks, int visibleVPixels) {
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

    public static void updateTime() {
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
            fullPixelWidth = getLevelWidth() << resolutionExponent;
            fullPixelHeight = getLevelHeight() << resolutionExponent;
        }
    }

    public static void resize() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Position this image so that it can be a background image which never has black bars. We are rather cutting some borders.
     * @param img
     */
    static void scaleBackground(Image img) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float iw = img.getWidth();
        float ih = img.getHeight();

        float scale = Math.max(w/iw, h / ih);
        img.setScale(scale);
        img.setPosition(w / 2 - iw * scale / 2, h / 2 - ih * scale / 2);
    }

    public void init() {
        gfx = Entities.getInstance();
        // Load after graphics have been loaded
        windowVPixelXPos = 0;
        windowVPixelYPos = 0; // TODO: this might have to be initialized from a saved state or just computed based on mcminos position
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        resolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        setResolution(resolution);
    }


    public static void loadLevel(String s) {
        reset();
        // Load a level
        level = new Level(s);
        // create destination-object
        lastBlock = mcminos.getLevelBlock();
        destination = new LevelObject(level,lastBlock.getX(),lastBlock.getY(),
                Entities.destination.getzIndex(), LevelObject.Types.Unspecified);
        resize();
        // init movers
        mcmMover = new Mover( mcminos, mcminosSpeed, true, moverMcminos);
        mcminosGfxNormal();

        // start the own timer (which triggers also the movemnet)
        startTimer();
    }

    public static void mcminosGfxNormal() {
        mcmMover.setGfx(Entities.mcminos_default_front, Entities.mcminos_default_up,
                Entities.mcminos_default_right, Entities.mcminos_default_down, Entities.mcminos_default_left);
    }

    public static void mcminosGfxPowered() {
        mcmMover.setGfx( Entities.mcminos_doped_front, Entities.mcminos_doped_up,
                Entities.mcminos_doped_right, Entities.mcminos_doped_down, Entities.mcminos_doped_left);
    }

    private static void reset() {
        level=null;
        mcminos = null;
        mcmMover=null;
        // TODO: make this reset dependent on level
        bombs=0; // number of bombs carried by mcminos
        dynamites=0; // number of dynamites carried by mcminos
        keys=0; // number of keys carried by mcminos
        umbrellas = 0; // number of umbrellas carried by mcminos
        lives = 3; // number of lives left
        landmines = 0; // number of landmines carried
        destinationSet = false;

        movables=new ArrayList<>(); // all moveables - mcminos
        lastBlock = null;
        toolboxShown = false;
        // Make sure global structure is empty,must happen before dest-creation
        LevelObject.disposeAll();
        score = 0; //TODO: recheck, when this has to be reset

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
        return level.get( x, y );
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
            if (destinationSet) { // destination is set
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
                if( dircount == 0 && xdelta ==0 && ydelta ==0) {
                    unsetDestination();
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
     * Start the moving thread which will manage all movement of objects in the game
     */
    public static void startTimer() {
        frameTimer = new FrameTimer();

        if( timerTask != null)
            timerTask.cancel(); // cancelold one
        timerTask = new Timer.Task() {
            @Override
            public void run() {
                nextGameFrame();
            }
        };
        Timer.schedule(timerTask, 0, 1 / (float) timeResolution);
    }

    public static long getGameFrame() {
        return gameFrame;
    }

    public static void pause() {
        timerTask.cancel();
    }

    /**
     * This is called
     */
    private static void nextGameFrame() { // to allow serialized iterations
        if( !toolboxShown) { // if game is not paused
            // do timers
            gameFrame++;
            frameTimer.update(gameFrame);
            // update durations and trigger events, if necessary
            if (powerDuration > 1) {
                powerDuration--;
            } else {
                if (powerDuration == 1) { // power just ran out
                    powerDuration = 0;
                    setPowerPillValues(1, 1, 0); // back to normal, TODO: check, if this has to be adapted to level specifics
                    mcminosGfxNormal();
                }
            }
            if(umbrellaDuration > 0) {
                umbrellaDuration --;
            } // no else necessary as umbrellapower is checked when necessary
            // move everybody
            try { // needs to be synchronized against drawing
                Root.updateLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // TODO: check if moving order can be reversed (mcminos first)

            // move everybody
            for (int i=movables.size()-1; i>=0; i--) { // works as synchronized
                LevelObject m = movables.get(i);
                m.move();
                if(m.checkCollisions())
                    movables.remove(i);
            }

            mcmMover.calculateDirection();
            mcmMover.move();
            checkMcMinosCollisions();
        }

        Root.updateLock.release();
    }

    /**
     * Check Mcminos'  collisions (mainly if mcminos found something and can collect it)
     * @return
     */
    private static void checkMcMinosCollisions() {
        // check if something can be collected (only when full on field)
        if(mcminos.fullOnBlock()) {
            LevelBlock currentBlock = getLevelBlockFromVPixel( mcminos.getVX(), mcminos.getVY() );
            if( currentBlock.hasPill() )
            {
                soundPlay("knurps");
                currentBlock.removePill();
                increaseScore(1);
            }
            // check, if mcminos actually moved or if it's the same field as last time
            if(currentBlock != lastBlock) {
                if(umbrellaDuration == 0) { // no umbrellapower currently
                    // check if last block had a hole -> make it bigger
                    if (lastBlock.hasHole()) {
                        // TODO check umbrella
                        // try to increase
                        lastBlock.getHole().increaseHole();
                    }
                    if (lastBlock.hasOneWay()) {
                        lastBlock.turnOneWay();
                    }
                    // check if here is max hole
                    if (currentBlock.hasHole() && currentBlock.getHole().holeIsMax()) {
                        // fall in
                        // TODO: intiate kill sequence
                        soundPlay("falling");
                    }
                }
                // check the things lying here
                for( LevelObject b:currentBlock.getCollectibles()) {
                    switch( b.getType() ) {
                        case Chocolate:
                            soundPlay("tools");
                            chocolates ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            increaseScore(10);
                            break;
                        case Bomb:
                            soundPlay("tools");
                            bombs ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case Dynamite:
                            soundPlay("tools");
                            dynamites ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMine:
                            soundPlay("tools");
                            landmines ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMineActive:
                            currentBlock.removeItem(b);
                            b.dispose();
                            new Explosion(currentBlock, LevelObject.Types.LandMine);
                            break;
                        case Key:
                            soundPlay("tools");
                            keys ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            increaseScore(10);
                            break;
                        case Umbrella:
                            soundPlay("tools");
                            umbrellas ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            increaseScore(10);
                            break;
                        case Live:
                            soundPlay("life");
                            lives ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            increaseScore(10);
                            break;
                        case Power1:
                            currentBlock.removeItem(b);
                            b.dispose();
                            setPowerPillValues(2, 1, 10);
                            // sound played in ppill method
                            mcminosGfxPowered(); // turn mcminos into nice graphics
                            break;
                        case Power2:
                            currentBlock.removeItem(b);
                            b.dispose();
                            setPowerPillValues(1, 2, 10);
                            mcminosGfxPowered(); // turn mcminos into nice graphics
                            break;
                        case Power3:
                            currentBlock.removeItem(b);
                            b.dispose();
                            setPowerPillValues(1, 1, 10);
                            mcminosGfxPowered(); // turn mcminos into nice graphics
                            break;
                        case SpeedUpField:
                            mcminosSetSpeedFactor(2);
                            soundPlay("speedup");
                            break;
                        case SpeedDownField:
                            mcminosSetSpeedFactor(1);
                            soundPlay("slowdown");
                            break;
                    }
                }


            }
            lastBlock = currentBlock;
        }
    }

    public static void increaseScore(int increment) {
        int old = score/5000;
        score += increment;
        if(score/5000 > old) { // just passed 5000
            // earn a live
            lives += 1;
            soundPlay("life");
        }
    }


    
    /**
     *  consume powerpill
     */
    static void setPowerPillValues(int mcmNewFactor, int gosNewFactor, int duration)
    {
        mcminosSetSpeedFactor(mcmNewFactor);
        for(int i=0; i<4; i++)
        {
            ghostSpeed[i] /= ghostSpeedFactor;
            ghostSpeed[i] *= gosNewFactor;
            // TODO: set ghost speed in ghostmover
        }
        ghostSpeedFactor = gosNewFactor;
        if(duration > 0) // something was actually consumed
        {
            powerDuration += duration << timeResolutionExponent;
            soundPlay("power");
            increaseScore(10);
        }
    }

    private static void mcminosSetSpeedFactor(int mcmNewFactor) {
        mcminosSpeed /= mcminosSpeedFactor;
        mcminosSpeed *= mcmNewFactor;
        mcminosSpeedFactor = mcmNewFactor;
        mcmMover.setCurrentSpeed(mcminosSpeed);
    }

/*
		case MEDICINE1-1:clearwall( x, y );
					snd_tool();
					inc_score( 10 );
					carry[CARRYANTIDOT]++;
					break;
		case CLOCKOBJ-1:clearwall( x, y );
					snd_tool();
					if(timeactiv) leveltime+=60;
					inc_score( 10 );
					break;
		case POISON1-1:clearwall( x, y );
					pacpoison();
					retwert = 0;
					break;
		case SKULL-1: retwert = 0; spec_action = 1;
					power = 0; kill_mcminos(); break;
		case SURPRISE-1:clearwall( x, y );
					choose_surprise( x, y );
					break;
		case LADDER-1: retwert = 0; pills_left=0; spec_action=1;
    inc_score(10); break;
    case TRUHE-1:clearwall( x, y );
    snd_tool();
    inc_score( 500 );
    break;
    case GELDSACK-1:clearwall( x, y );
    snd_tool();
    inc_score( 250 );
    break;
    case SPARSCHWEIN-1:clearwall( x, y );
    snd_tool();
    inc_score( 100 );
    break;
    case SPEEDUP-1: snd_speedup(); speedup = 1; break;
    case SLOWDOWN-1: snd_slowdown(); speedup = 0; break;
    case MIRROR-1:clearwall( x, y );
    inc_score( 10 );
    snd_mirror();
    mirrorflag = !mirrorflag;
    break;
    case WHISKEY-1:clearwall( x, y );
    snd_drunken();
    drunken += 16;
    inc_score( 5 );
    break;
    case KILLALL-1:clearwall( x, y );
    snd_killall();
    inc_score( goscount[0] * 10 );
    ghostkillflag = 1;
    spec_action = 1;
    break;
    case KILLALL2-1: snd_killall();
    inc_score( goscount[0] * 10 );
    ghostkillflag = 1;
    spec_action = 1;
    break;
    case SECRETLETTER-1:clearwall( x, y );
    //snd_letter();
    inc_score( 10 );
    found_letter = 1;
    spec_action = 1;
    break;
    case LOCH-1: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH+1: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH+2: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH+3: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        McMinos_hole();
        retwert = 0;
    }
    break;
    case WARP-1:spec_action = 1;
    stop_moving = 1;
    do_warp = 1;
    retwert = 0;
    break;
    case MINEDOWN-1: mine_expl( x, y ); break;
    case MINEUP-1:change_field( x, y, levfield(y, x).extra);
    snd_tool();
    inc_score( 10 );
    carry[CARRYMINE]++;
    break;
}



    void snd_killed( void )
{
	play_sound( GHOSTS, 3, 300 );
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

     Sound fr fallenden McMinos
    void snd_falling( void )
    {
        play_sound( FALLING, 2, 300 );
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


    public static void soundPlay(String s) {
        soundList.get(s).play(1.0f);
    }

    public void dispose() {
        batch.dispose();
        defaultSkin.dispose();
    }

    public static void setScreen(Screen scr) {
        main.setScreen(scr);
    }

    public static void setMain(Main m) {
        main = m;
    }

    public void setDestination(int x, int y) {
        destination.setGfx(Entities.destination);
        destination.moveTo(x, y);
        destinationSet = true;
    }

    private static void unsetDestination() {
        destination.setGfx(null);
        //destinationSet = false; needs to be still set
    }

    static int random(int interval) {
        return randomGenerator.nextInt(interval);
    }
}
