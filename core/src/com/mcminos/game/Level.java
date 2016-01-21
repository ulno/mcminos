package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by ulno on 17.08.15.
 *
 * The actual level with the ability to read in a new level
 */
public class Level implements KryoSerializable {
    public static final int maxDimension = 100; // maximum Level size in windowVPixelXPos and windowVPixelYPos
    private ArrayList<LevelObject> allLevelObjects = new ArrayList<>(); // sorted list of all levelobjects (for drawing at once)
    private Game game;
    private int pillsNumber = 0;
    private int rockmeNumber = 0;
    private LevelBlock[][] field; // on each level field are usually several objects, first is windowVPixelXPos, second windowVPixelYPos
    private ArrayList<LevelBlock> warpHoleBlocks = new ArrayList<>();
    private ArrayList<LevelBlock> castleList = new ArrayList<>();
    private ArrayList<LevelBlock> ghostStart[] = new ArrayList[4];
    private LevelBlock mcminosStart;
    private LevelConfig levelConfig;
    private boolean finished = false;
    private int levelCategory=-1;
    private int levelNr=-1;
    private Main main;
    private Random randomGenerator = new Random();

    // values set from levelconfig
    private int width;
    private int vPixelsWidth;
    private int height;
    private int vPixelsHeight;
    private boolean scrollX;
    private boolean scrollY;
    private int ghostPillDrop;



    /**
     * called when restored from kryo
     */
    Level() {
        this.game = null;
        // name will be set later
    }

    Level ( Main main, Game game, LevelConfig levelConfig ) {
        this.main = main;
        this.game = game;
        this.levelConfig = levelConfig;
        load( levelConfig, true );
        initMcMinosStart(game.getMcMinos());
        resetGhostsStart(game.getGhosts());
    }

    public void draw(PlayWindow playwindow, boolean drawBackgrounds) {
        int zIndex;

        int size = allLevelObjects.size();
        if(drawBackgrounds) {
            for (int i = 0; i < size; i++) {
                LevelObject lo = allLevelObjects.get(i);
                if (lo.getzIndex() >= LevelObject.maxzIndex) // TODO: check if necessary
                    break; // can be stopped, as null is infinity and therefore only null in the end
                lo.draw(playwindow);
            }
        } else {
            for (int i = 0; i < size; i++) {
                LevelObject lo = allLevelObjects.get(i);
                zIndex = lo.getzIndex();
                if (  zIndex >= LevelObject.maxzIndex)
                    break; // can be stopped, as null is infinity and therefore only null in the end
                if( zIndex >= 200 ) lo.draw(playwindow);
            }
        }
    }

    public void drawMini(PlayWindow playwindow, SpriteBatch batch) {
        int size = allLevelObjects.size();
        for (int i=0; i<size; i++) {
            LevelObject lo = allLevelObjects.get(i);
            lo.drawMini(playwindow, batch);
        }
    }

    private void disposeAllLevelObjects() {
        for( int i=allLevelObjects.size()-1; i>=0; i--) {
            LevelObject lo = allLevelObjects.get(i);
            lo.dispose(); // also removes
        }

    }

    public void dispose() {
        warpHoleBlocks.clear();
        disposeAllLevelObjects(); // this also removes mcminos from this list
        pillsNumber = 0;
        rockmeNumber = 0;
    }

    public void addToAllLevelObjects( LevelObject lo ) {
        // add to static list
        int index = Collections.binarySearch(allLevelObjects, lo); // make sure it's sorted
        if(index<0)
            index = -index - 1;

        allLevelObjects.add(index, lo);
    }

    /**
     * does not know game as called in kryo -> take care of randomly animated objects
     *
     * @param lc
     * @param doUpdateBlocks
     */
    public void load(LevelConfig lc, boolean doUpdateBlocks) {
        this.levelConfig = lc;
        this.game = game;

        // reset start positions
        for(int i=0; i<4; i++) {
            ghostStart[i] = new ArrayList<>();
        }
        // get local cache values from levelConfig
        width = lc.getWidth();
        height = lc.getHeight();
        scrollX = lc.getScrollX();
        scrollY = lc.getScrollY();
        ghostPillDrop = lc.getGhostPillDrop();

        // initialize level field
        field = new LevelBlock[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                field[x][y] = new LevelBlock(this, x, y);

        String lines[] = lc.getLevelData().split("\n");

        for (int i = 0; i < lines.length; i++) {
            parseLevelLine(i, lines[i]);
        }

        Graphics bggfx = lc.getBackground();

        if(doUpdateBlocks) {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++) {
                    LevelBlock f = field[x][y];
                    f.updateWall();
                    f.updateCastle(game);
                    f.updateDoor();
                    // create background
                    if (x % bggfx.getWidth() == 0 && y % bggfx.getHeight() == 0) {
                        LevelObject lo = new LevelObject(this, x, y, Entities.backgrounds_pavement_01.getzIndex(), LevelObject.Types.Background);
                        lo.setGfx(bggfx);
                    }
                    // create wrap-around fields
                    if (scrollX && x == width - 1) {
                        LevelObject lo = new LevelObject(this, x, y, Entities.extras_wrap_around_horizontal.getzIndex(), LevelObject.Types.Background);
                        lo.setGfx(Entities.extras_wrap_around_horizontal);
                    }
                    if (scrollY && y == height - 1) {
                        LevelObject lo = new LevelObject(this, x, y, Entities.extras_wrap_around_vertical.getzIndex(), LevelObject.Types.Background);
                        lo.setGfx(Entities.extras_wrap_around_vertical);
                    }
                }
        }
        // update some related variables
        vPixelsWidth = width << PlayWindow.virtualBlockResolutionExponent;
        vPixelsHeight = height << PlayWindow.virtualBlockResolutionExponent;
    }

    /**
     * Create elements mentioned in this line
     *
     * @param levelline
     * @param line
     */
    private void parseLevelLine(int levelline, String line) {
        int destinationLine = height - levelline - 1; // Flip position for inverted windowVPixelYPos in libgdx
        if(destinationLine < 0 || destinationLine >= height) {
            Gdx.app.log("parseLevelLine","levelinput out of dimension, ignoring. " + levelConfig.getId() + " y: " + destinationLine );
            return;
        }
        int linepos = 0;
        for(char c : line.toCharArray()) {
            if(linepos>width) {
                Gdx.app.log("parseLevelLine","levelinput out of dimension, ignoring. " + levelConfig.getId() + " x: " + linepos );
                break;
            }
            LevelBlock lb = field[linepos][destinationLine];
            switch(c) {
                case 'P':
                    //lb.makeMcMinos(mcminos);
                    mcminosStart = lb;
                    break;
                case 'X':
                    lb.makeWall();
                    break;
                case 'Z':
                    lb.makeIndestructableWall();
                    break;
                case 'U':
//                    lb.makeInvisibleWall();
                    lb.makeWall(); // testing to not have invisible walls, seems to work: TODO: re-discuss
                    break;
                case '.':
                    lb.makePill();
                    break;
                case '*':
                    lb.makePowerPill1();
                    break;
                case '(':
                    lb.makePowerPill2();
                    break;
                case ')':
                    lb.makePowerPill3();
                    break;
                case 'C':
                    lb.makeCastle(game);
                    break;
                case 'G':
                    //lb.makeGhost(0,ghosts);
                    ghostStart[0].add(lb);
                    break;
                case 'g':
                    //lb.makeGhost(1,ghosts);
                    ghostStart[1].add(lb);
                    break;
                case 'H':
                    //lb.makeGhost(2,ghosts);
                    ghostStart[2].add(lb);
                    break;
                case 'h':
                    //lb.makeGhost(3,ghosts);
                    ghostStart[3].add(lb);
                    break;
                case 'L':
                    lb.makeLive();
                    break;
                case 'c':
                    lb.makeLetter();
                    break;
                case 'S':
                    lb.makeSkull();
                    break;
                case 'T':
                    lb.makeSkullField();
                    break;
                case 'b':
                    lb.makeBomb();
                    break;
                case 'd':
                    lb.makeDynamite();
                    break;
                case '_':
                    lb.makeLandMine();
                    break;
                case ',':
                    lb.makeLandMineActivated();
                    break;
                case 'k':
                    lb.makeKey();
                    break;
                case 'u':
                    lb.makeUmbrella();
                    break;
                case 'r':
                    lb.makeRock();
                    break;
                case 'O':
                    lb.makeRockMe();
                    break;
                case '0':
                    lb.makeRockMe();
                    lb.makeRock();
                    //handled now correctly decreaseRockmes(); // is already at destination
                    break;
                case '6':
                    lb.makeHole(0);
                    break;
                case '7':
                    lb.makeHole(1);
                    break;
                case '8':
                    lb.makeHole(2);
                    break;
                case '9':
                    lb.makeHole(3);
                    break;
                case 'o':
                    lb.makeHole(4);
                    break;
                case 'D':
                    lb.makeDoorClosed();
                    break;
                case '|':
                    lb.makeDoorOpened();
                    break;
                case 'F':
                    lb.makeSpeedUpField();
                    break;
                case 'f':
                    lb.makeSpeedDownField();
                    break;
                case 'W':
                    lb.makeWarpHole();
                    break;
                case 'a':
                    lb.makeKillAllPill();
                    break;
                case 'A':
                    lb.makeKillAllField();
                    break;
                case '^':
                    lb.makeOneWay(0);
                    break;
                case '>':
                    lb.makeOneWay(1);
                    break;
                case 'v':
                    lb.makeOneWay(2);
                    break;
                case '<':
                    lb.makeOneWay(3);
                    break;
                case 'ä':
                    lb.makeOneWay(4);
                    break;
                case 'ö':
                    lb.makeOneWay(5);
                    break;
                case 'ü':
                    lb.makeOneWay(6);
                    break;
                case 'ß':
                    lb.makeOneWay(7);
                    break;
                case '+':
                    lb.makeChocolate();
//                multipliers: MCSPEED *= 2; GHSPEEDs *= 1
//                (does it's job for: 10 s)
                    break;
                case 'x':
                    lb.makeExit();
                    break;
                case '1':
                    lb.makeBonus1();
                    break;
                case '2':
                    lb.makeBonus2();
                    break;
                case '3':
                    lb.makeBonus3();
                    break;
                case '?':
                    lb.makeSurprise();
                    break;
                case 'w':
                    lb.makeWhisky();
                    break;
                case 'M':
                    lb.makeMirror();
                    break;
                case 'p':
                    lb.makePoison();
                    break;
                case 'm':
                    lb.makeMedicine();
                    break;


                /*
Missing:

;   $ = clock, Level time (if level time is limited:) + 60 sec.

                 */

            }
            linepos ++;
            if (linepos >= width) break;
        }
    }

    /**
     * Find level block at the given x,y position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock get( int x, int y ) {
        if(scrollX) x = (x+(width<<2)) % width;
        if(scrollY) y = (y+(height<<2)) % height;
        if(x>=0 && x<width && y>=0 && y<height)
            return field[x][y];
        else
            return null;
    }

    public LevelBlock getLevelBlockFromVPixel( int vPixelX, int vPixelY) {
        int x = vPixelX  >> PlayWindow.virtualBlockResolutionExponent;
        int y = vPixelY  >> PlayWindow.virtualBlockResolutionExponent;
        x = (  x + width ) % width;
        y = (  y + height ) % height;
        return get( x, y );
    }

    public LevelBlock getLevelBlockFromVPixelRounded( int vPixelX, int vPixelY) {
        return getLevelBlockFromVPixel( vPixelX + (PlayWindow.virtualBlockResolution >> 1),
                vPixelY + (PlayWindow.virtualBlockResolution >> 1) );
    }


    /**
     * Find level block below of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getDown( int x, int y ) {
        y--;
        if(y<0) {
            if (scrollY) y += height;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getDown2(int x, int y) {
        return field[x][(y-2+height)%height];
    }

    /**
     * Find level block right of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getRight( int x, int y, boolean sx) {
        x++;
        if(x>=width) {
            if (sx) x -= width;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getRight( int x, int y ) {
        return getRight( x, y, scrollX);
    }
    public LevelBlock getRight2(int x, int y) {
        return field[(x+2)%width][y];
    }

    /**
     * Find level block on top of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getUp( int x, int y ) {
        y++;
        if(y>=height) {
            if (scrollY) y -= height;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getUp2(int x, int y) {
        return field[x][(y+2)%height];
    }

    /**
     * Find level block left of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getLeft( int x, int y) {
        x--;
        if(x<0) {
            if (scrollX) x += width;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getLeft2(int x, int y) {
        return field[(x-2+width)%width][y];
    }

    public int getVPixelsWidth() {
        return vPixelsWidth;
    }

    public int getVPixelsHeight() {
        return vPixelsHeight;
    }

    public int getVisibleWidth() {
        return levelConfig.getVisibleWidth();
    }

    public int getVisibleHeight() {
        return levelConfig.getVisibleHeight();
    }

    public boolean getScrollX() {
        return scrollX;
    }

    public boolean getScrollY() {
        return scrollY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void increasePills() {
        pillsNumber ++;
    }

    public void decreasePills() {
        pillsNumber --;
        // do we need to trigger something when we reach 0 - no, we check this manually
    }

    public void increaseRockmes() {
        rockmeNumber ++;
    }

    public void decreaseRockmes() {
        rockmeNumber --;
        // do we need to trigger something when we reach 0 - no, we check this manually
    }


    public int getRockmesNumber() {
        return rockmeNumber;
    }

    public int getPillsNumber() {
        return pillsNumber;
    }

    public void addWarpHole(LevelBlock warpHoleBlock) {
        warpHoleBlocks.add(warpHoleBlock);
    }

    public LevelBlock getFreeWarpHole( LevelBlock origin ) {
        LevelBlock returnBlock = null;

        for( int i=0; i<5; i++ ) { // Try 5 times random
            LevelBlock testBlock = warpHoleBlocks.get(random(warpHoleBlocks.size()));
            if( !testBlock.hasRock() && testBlock != origin) {
                returnBlock = testBlock;
                break;
            }
        }
        // if random was not successfull, try linear
        if( returnBlock == null) {
            for( int i=0; i<warpHoleBlocks.size(); i++ ) { // Try 5 times random
                LevelBlock testBlock = warpHoleBlocks.get(i);
                if( !testBlock.hasRock() && testBlock != origin) {
                    returnBlock = testBlock;
                    break;
                }
            }
        }
        if( returnBlock == null) { // only origin is possible
            return origin;
        }
        return returnBlock;
    }

    public void addCastle(LevelObject castle) {
        castleList.add(castle.getLevelBlock());
    }

    public LevelBlock getRandomCastleBlock() {
        if(castleList.size() == 0) {
            return null;
        }
        return castleList.get(random(castleList.size()));
    }

    public Game getGame() {
        return game;
    }

    public void killRestart(boolean completeRestart) {
        // reset all things necessary in case of a death
        /* restart:      0       ; (0,1,2,4,8,16 256, 257, 258, 260,
                    ; 264, 272)
                    ; Mode of restarting a level after death
                    ; of McMinos: (Default = 0)
                    ; 0 = ghosts + McMinos start in their
                    ; original positions.
                    ; 1 = Level is completely restarted.
                    ; 2 = ghosts restart from castle(s).
                    ; 4 = McMinos restarts from the position
                    ; he died, ghosts restart from their
                    ; original positions.
                    ; 8 = Bonus level
                    ; 16= McMinos and all ghosts
                    ;     restart from where they were
                    ;     in the moment of McMinos' death.
                    ; 256 = Last level and RSTRT = 0
                    ; 257 = Last level and RSTRT = 1
                    ; 258 = Last level and RSTRT = 2
                    ; 260 = Last level and RSTRT = 4
                    ; 264 = Last level and RSTRT = 8
                    ; 272 = Last level and RSTRT = 16 !!! */
        game.stopMovement();
        game.disposeEventManagerTasks();
        int restart = levelConfig.getRestart();
        if(completeRestart || (restart & 1) > 0) { // complete restart requested
            // done in reset game.getGhosts().dispose(); // remove ghosts
            // discard mcminos
            //game.getMcMinos().dispose();
            // empty movers
            //game.clearMovers();
            game.reset();
            // reload
            reload();
            resetGhostsStart(game.getGhosts());
        } else { // "normal" restart
            game.getMcMinos().reset();
            // restore graphics of mcminos
            // in mcminos.reset: game.getMcMinos().gfxSelect();
            if((restart & 16) == 0) {
                game.getGhosts().dispose(); // remove ghosts
            }
            if ((restart & 2) == 0) {
                resetGhostsStart(game.getGhosts());
            }
            if ((restart & 4) == 0) { // if not 4, teleport back
                game.getMcMinos().teleportToBlock(mcminosStart);
            }
            McMinos mcminos = game.getMcMinos();
            mcminos.teleportToBlock(mcminos.getLevelBlock());
            mcminos.initMover();
        }
        game.startMovement();
        game.getPlayScreen().activateToolbox();
    }

    private void resetGhostsStart(Ghosts ghosts) {
        ghosts.dispose();
        for (LevelBlock b : ghostStart[0]) b.makeGhost(0,ghosts);
        for (LevelBlock b : ghostStart[1]) b.makeGhost(1,ghosts);
        for (LevelBlock b : ghostStart[2]) b.makeGhost(2,ghosts);
        for (LevelBlock b : ghostStart[3]) b.makeGhost(3,ghosts);
    }

    private void initMcMinosStart(McMinos mcminos) {
        mcminosStart.makeMcMinos(mcminos);
    }

    private void reload() {
        game.reload();
    }

    public LevelConfig getLevelConfig() {
        return levelConfig;
    }

    public void removeFromAllLevelObjects(LevelObject lo) {
/*        int index = Collections.binarySearch(allLevelObjects, lo);
        if(index>=0)
            allLevelObjects.remove(index);*/ // TODO: optimize, this is very slow (the bigger the level)
        allLevelObjects.remove(lo);
    }

    /**
     * level is finished, either by win or death
     */
    public void finish() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getGhostPillDrop() {
        return ghostPillDrop;
    }

    public void decreaseGhostPillDrop() {
        if(ghostPillDrop > 0)
            ghostPillDrop --;
    }

    public int getGhostPillFreq() {
        return levelConfig.getGhostPillFreq();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, levelConfig.getCategoryNr());
        kryo.writeObject(output, levelConfig.getNr());
        kryo.writeObject(output, ghostPillDrop );
        kryo.writeObject(output, allLevelObjects);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        levelCategory = kryo.readObject(input,Integer.class);
        levelNr = kryo.readObject(input,Integer.class);
        Main main = (Main) kryo.getContext().get("main");
        levelConfig = main.getLevelsConfig().get(levelCategory).get(levelNr);
        int gpdTmp = kryo.readObject(input,Integer.class);
        load(levelConfig, false);
        for( int i=allLevelObjects.size()-1; i>=0; i--) { // reset manually as these are loaded now - TODO: consider skipping backgrounds
            allLevelObjects.get(i).dispose();
        }
        allLevelObjects = kryo.readObject(input, ArrayList.class);
        // cleanup, remove animation-event-objects
        for( int i = allLevelObjects.size()-1; i>=0; i-- ) {
            LevelObject lo = allLevelObjects.get(i);
            switch(lo.getType()) {
                case BombFused:
                case DynamiteFused:
                case BombExplosion:
                case McMinosDying:
                case McMinosFalling:
                case McMinosWinning:
                    allLevelObjects.remove(i);
                    break;
            }
        }
        ghostPillDrop = gpdTmp;
    }

    public void initAfterKryoLoad(Main main,Game game) {
        this.main = main;
        this.game = game;
        levelConfig = main.getLevelsConfig().get(levelCategory).get(levelNr);
        for( int i=allLevelObjects.size()-1; i>=0; i--) {
            allLevelObjects.get(i).initAfterKryoLoad(game);
        }
    }

    public LevelObject getFirstLevelObjectFromList(LevelObject.Types type) {
        for( int i = allLevelObjects.size()-1; i>=0; i-- ) {
            LevelObject lo = allLevelObjects.get(i);
            if(lo.getType() == type ) return lo;
        }
        return null;
    }

    public int getGhostTranswall(int ghostnr) {
        return levelConfig.getGhostTranswall(ghostnr);
    }

    public int getGhostMax(int ghostnr) {
        return levelConfig.getGhostMax(ghostnr);
    }

    public int getGhostTime(int ghostnr) {
        return levelConfig.getGhostTime(ghostnr);
    }

    public int getGhostAgility(int ghostNr) {
        return levelConfig.getGhostAgility(ghostNr);
    }

/*    public void exchangeLevelObjectInList(LevelObject loOld, LevelObject loNew) {
        allLevelObjects.remove(loOld);
        allLevelObjects.add(loNew);
    }*/

    public int random(int interval) {
        return randomGenerator.nextInt(interval);
    }
}
