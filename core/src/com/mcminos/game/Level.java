package com.mcminos.game;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ulno on 17.08.15.
 *
 * The actual level with the ability to read in a new level
 */
public class Level {
    public static final int maxDimension = 100; // maximum Level size in windowVPixelXPos and windowVPixelYPos
    private ArrayList<LevelObject> allLevelObjects = new ArrayList<>(); // sorted list of all levelobjects (for drawing at once)
    private Game game;
    private int pillsNumber = 0;
    private int rockmeNumber = 0;
    private LevelBlock[][] field; // on each level field are usually several objects, first is windowVPixelXPos, second windowVPixelYPos
    private String author = "Main";
    private int number = 199;
    private int showNumber = 199;
    private String accessCode = "";
    private int width = 20;
    private int vPixelsWidth = 0;
    private int height = 20;
    private int vPixelsHeight = 0;
    private int VisibleWidth = 20;
    private int visibleHeight = 20;
    private boolean scrollX = false;
    private boolean scrollY = false;
    private String background = "default";
    private int time = 0;
    private int restart = 0;
    private boolean mirror = false;
    public int mcminosSpeed = 1;
    public int[] ghostMax = {0,0,0,0};
    public int[] ghostTime = {0, 0, 0, 0};
    public int[] ghostSpeed = {0, 0, 0, 0};
    public int[] ghostAgility = {0, 0, 0, 0};
    public int[] ghostPillMax  = {0, 0, 0, 0};
    public int[] ghostPillFreq  = {0, 0, 0, 0};
    public int[] ghostTranswall  = {0, 0, 0, 0};
    private int livesMin = 0, livesMax = 999;
    private int keysMin = 0, keysMax = 999;
    private int dynamitesMin = 0, dynamitesMax = 999;
    private int minesMin = 0, minesMax = 999;
    private int chocolatesMin = 0, chocolatesMax = 999;
    private int medicinesMin = 0, medicinesMax = 999;
    private int umbrellasMin = 0, umbrellasMax = 999;
    private ArrayList<LevelBlock> warpHoleBlocks = new ArrayList<>();
    private ArrayList<LevelObject> castleList = new ArrayList<>();
    private ArrayList<LevelBlock> ghostStart[] = new ArrayList[4];
    private LevelBlock mcminosStart;
    private String levelName;


    Level ( Game game, String filename ) {
        this.game = game;
        load( filename );
    }

    public void draw(PlayWindow playwindow) {
        for (LevelObject lo : allLevelObjects) {
            if( lo.getzIndex() >= LevelObject.maxzIndex)
                break; // can be stopped, as null is infinity and therefore only null in the end
            lo.draw(playwindow);
        }
    }

    public void dispose() {
        warpHoleBlocks.clear();
        allLevelObjects.clear();
    }

    public void addToAllLevelObjects( LevelObject lo ) {
        // add to static list
        int index = Collections.binarySearch(allLevelObjects, lo); // make sure it's sorted
        if(index<0)
            index = -index - 1;

        allLevelObjects.add(index, lo);
    }

    /**
     *
     * @param levelName
     */
    public void load(String levelName) {
        // save name
        this.levelName = levelName;

        // construct filename
        String filename = "levels/" + levelName + ".asx";

        // reset start positions
        for(int i=0; i<4; i++) {
            ghostStart[i] = new ArrayList<>();
        }

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Gdx.files.internal(filename).read()), 2048);

        String line;
        try {
            boolean readLevel = false; // set to true, when level keyword was found
            int levelline = 0; // current nr of line read
            while ((line = br.readLine()) != null) {
                if(! readLevel) {
                    line = line.split(";")[0]; // cut off comments
                    line = line.trim(); // remove whitespace
                    if( line == "" || ! line.startsWith(";") )
                    {
                        int min=0, max=0;
                        String[] strList = line.split("\\s*:\\s*");
                        if(strList.length > 1) { // if two params,  try min,max split
                            String[] minmax = strList[1].split("\\s*,\\s*");
                            if (minmax.length > 1) {
                                min = Integer.parseInt(minmax[0]);
                                max = Integer.parseInt(minmax[1]);
                            }
                        }
                        // TODO: apply minmax on existing mcminos
                        switch(strList[0]) {
                            case "LEVEL": readLevel=true; break;
                            case "AUTHOR": author = strList[1]; break;
                            case "NUMBR": number = Integer.parseInt(strList[1]); break;
                            case "SHOWNR": showNumber = Integer.parseInt(strList[1]); break;
                            case "ACCCD": accessCode = strList[1]; break;
                            case "LWID": width = Integer.parseInt(strList[1]); break;
                            case "LHI": height = Integer.parseInt(strList[1]); break;
                            case "VWID": VisibleWidth = Integer.parseInt(strList[1]); break;
                            case "VHI": visibleHeight = Integer.parseInt(strList[1]); break;
                            case "SCROLLX": scrollX = "1".equals(strList[1]); break;
                            case "SCROLLY": scrollY = "1".equals(strList[1]); break;
                            case "BACK": background = strList[1]; break;
                            case "LTIME": time = Integer.parseInt(strList[1]); break;
                            case "RSTRT": restart = Integer.parseInt(strList[1]); break;
                            case "MIRROR": mirror = "1".equals(strList[1]); break;
                            case "MCSPEED": mcminosSpeed = Integer.parseInt(strList[1]); break;
                            case "GHOST1": ghostMax[0] = Integer.parseInt(strList[1]); break;
                            case "GRTIME1": ghostTime[0] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED1": ghostSpeed[0] = Integer.parseInt(strList[1]) * Game.baseSpeed; break;
                            case "AGIL1": ghostAgility[0] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX1": ghostPillMax[0] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ1": ghostPillFreq[0] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL1": ghostTranswall[0] = Integer.parseInt(strList[1]); break;
                            case "GHOST2": ghostMax[1] = Integer.parseInt(strList[1]); break;
                            case "GRTIME2": ghostTime[1] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED2": ghostSpeed[1] = Integer.parseInt(strList[1]) * Game.baseSpeed; break;
                            case "AGIL2": ghostAgility[1] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX2": ghostPillMax[1] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ2": ghostPillFreq[1] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL2": ghostTranswall[1] = Integer.parseInt(strList[1]); break;
                            case "GHOST3": ghostMax[2] = Integer.parseInt(strList[1]); break;
                            case "GRTIME3": ghostTime[2] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED3": ghostSpeed[2] = Integer.parseInt(strList[1]) * Game.baseSpeed; break;
                            case "AGIL3": ghostAgility[2] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX3": ghostPillMax[2] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ3": ghostPillFreq[2] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL3": ghostTranswall[2] = Integer.parseInt(strList[1]); break;
                            case "GHOST4": ghostMax[3] = Integer.parseInt(strList[1]); break;
                            case "GRTIME4": ghostTime[3] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED4": ghostSpeed[3] = Integer.parseInt(strList[1]) * Game.baseSpeed; break;
                            case "AGIL4": ghostAgility[3] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX4": ghostPillMax[3] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ4": ghostPillFreq[3] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL4": ghostTranswall[3] = Integer.parseInt(strList[1]); break;
                            case "LIVE": livesMin = min; livesMax = max; break;
                            case "KEYS": keysMin = min; keysMax = max; break;
                            case "DYNA": dynamitesMin = min; dynamitesMax = max; break;
                            case "MINE": minesMin = min; minesMax = max; break;
                            case "CHOC": chocolatesMin = min; chocolatesMax = max; break;
                            case "MEDC": medicinesMin = min; medicinesMax = max; break;
                            case "UMBR": umbrellasMin = min; umbrellasMax = max; break;
                        }
                    }
                }
                else { // just read level-data
                    if( levelline == 0 ) // only first time
                    {
                        // initialize level field
                        field = new LevelBlock[width][height];
                        for (int x = 0; x < width; x++)
                            for (int y = 0; y < height; y++)
                                field[x][y] = new LevelBlock(this, x, y);
                    }
                    if( levelline < height )
                    {
                        parseLevelLine( levelline, line );
                    }
                    levelline ++;
                }

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // select background
        Graphics bggfx = null;
        switch(background) {
            case "1":
            case "pavement-01":
                bggfx = Entities.backgrounds_pavement_01;
                break;
            case "2":
            case "pavement-02":
                bggfx = Entities.backgrounds_pavement_02;
                break;
            case "3":
            case "pavement-03":
                bggfx = Entities.backgrounds_pavement_03;
                break;
            case "amoeboid-01":
                bggfx = Entities.backgrounds_amoeboid_01;
                break;
            case "autumn-leaves-02":
                bggfx = Entities.backgrounds_autumn_leaves_02;
                break;
            case "lawn-autumn-leaves":
                bggfx = Entities.backgrounds_lawn_autumn_leaves;
                break;
            case "blue-balls":
                bggfx = Entities.backgrounds_blue_balls;
                break;
            case "dry-grass":
                bggfx = Entities.backgrounds_dry_grass;
                break;
            case "flecktarn":
                bggfx = Entities.backgrounds_flecktarn;
                break;
            case "flowers-03":
                bggfx = Entities.backgrounds_flowers_03;
                break;
            case "flowers-xl-03":
                bggfx = Entities.backgrounds_flowers_xl_03;
                break;
            case "gravel-01":
                bggfx = Entities.backgrounds_gravel_01;
                break;
            case "gravel-02":
                bggfx = Entities.backgrounds_gravel_02;
                break;
            case "hexagon-01":
                bggfx = Entities.backgrounds_hexagon_01_hexagon;
                break;
            case "hexagon-03":
                bggfx = Entities.backgrounds_hexagon_03;
                break;
            case "lawn-02":
                bggfx = Entities.backgrounds_lawn_02;
                break;
            case "meadow-flowers":
                bggfx = Entities.backgrounds_meadow_flowers;
                break;
            case "sand-01":
                bggfx = Entities.backgrounds_sand_01_sand;
                break;
            case "universe-01":
                bggfx = Entities.backgrounds_universe_01;
                break;
            default:
                bggfx = Entities.backgrounds_pavement_01;
                break;
        }
        for( int x=0; x<width; x++)
            for( int y=0; y<height; y++)
            {
                LevelBlock f = field[x][y];
                f.updateWall();
                f.updateCastle();
                f.updateDoor();
                // background
                if(x % bggfx.getWidth() == 0 && y % bggfx.getHeight() == 0) {
                    LevelObject lo = new LevelObject(this, x, y, Entities.backgrounds_blue_balls.getzIndex(),LevelObject.Types.Background);
                    lo.setGfx(bggfx);
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
        int linepos = 0;
        for(char c : line.toCharArray()) {
            LevelBlock lb = field[linepos][destinationLine];
            switch(c) {
                case 'P':
                    lb.makeMcMinos();
                    mcminosStart = lb;
                    break;
                case 'X':
                    lb.makeWall();
                    break;
                case 'Z':
                    lb.makeIndestructableWall();
                    break;
                case 'U':
                    lb.makeInvisibleWall();
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
                    lb.makeCastle();
                    break;
                case 'G':
                    lb.makeGhost1();
                    ghostStart[0].add(lb);
                    break;
                case 'g':
                    lb.makeGhost2();
                    ghostStart[1].add(lb);
                    break;
                case 'H':
                    lb.makeGhost3();
                    ghostStart[2].add(lb);
                    break;
                case 'h':
                    lb.makeGhost4();
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
                    decreaseRockmes(); // is already at destination
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
        return field[x][y];
    }

    /**
     * Find level block below of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getDown( int x, int y, boolean sy ) {
        y--;
        if(y<0) {
            if (sy) y += height;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getDown( int x, int y ) {
        return getDown( x, y, scrollY);
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
    public LevelBlock getUp( int x, int y, boolean sy) {
        y++;
        if(y>=height) {
            if (sy) y -= height;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getUp( int x, int y ) {
        return getUp( x, y, scrollY);
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
    public LevelBlock getLeft( int x, int y, boolean sx) {
        x--;
        if(x<0) {
            if (sx) x += width;
            else return null;
        }
        return field[x][y];
    }
    public LevelBlock getLeft( int x, int y ) {
        return getLeft( x, y, scrollX);
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
        return VisibleWidth;
    }

    public int getVisibleHeight() {
        return visibleHeight;
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
        // TODO: do we need to trigger something when we reach 0?
    }

    public void increaseRockmes() {
        rockmeNumber ++;
    }

    public void decreaseRockmes() {
        rockmeNumber --;
        // TODO: do we need to trigger something when we reach 0?
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
            LevelBlock testBlock = warpHoleBlocks.get(game.random(warpHoleBlocks.size()));
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
        castleList.add(castle);
    }

    public LevelBlock getRandomCastleBlock() {
        if(castleList.size() == 0) {
            return null;
        }
        return castleList.get(game.random(castleList.size())).getLevelBlock();
    }

    public Game getGame() {
        return game;
    }

    public void killRestart() {
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
        game.disableMovement();
        game.getMcMinos().setPowerPillValues(1,1,0);
        game.disposeFrameTimer();
        if((restart & 1) > 0) { // complete restart requested
            game.getGhosts().dispose(); // remove ghosts
            // discard mcminos
            //game.getMcMinos().dispose();
            // empty movers
            //game.clearMovers();
            game.reset();
            // reload
            reload();
        } else { // "normal" restart
            // restore graphics of mcminos
            game.getMcMinos().gfxNormal();
            if((restart & 16) == 0) {
                game.getGhosts().dispose(); // remove ghosts
            }
            if ((restart & 2) == 0) {
                for (LevelBlock b : ghostStart[0]) b.makeGhost1();
                for (LevelBlock b : ghostStart[1]) b.makeGhost2();
                for (LevelBlock b : ghostStart[2]) b.makeGhost3();
                for (LevelBlock b : ghostStart[3]) b.makeGhost4();
            }
            if ((restart & 4) == 0) game.getMcMinos().teleportToBlock(mcminosStart);
        }
    }

    private void reload() {
        game.reload();
    }

    public String getLevelName() {
        return levelName;
    }

    public void removeFromAllLevelObjects(LevelObject levelObject) {
        allLevelObjects.remove(levelObject);
    }
}
