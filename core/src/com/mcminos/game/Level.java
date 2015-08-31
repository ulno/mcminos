package com.mcminos.game;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by ulno on 17.08.15.
 *
 * The actual level with the ability to read in a new level
 */
public class Level {
    public static final int maxDimension = 100; // maximum Level size in windowVPixelXPos and windowVPixelYPos
    private LevelBlock[][] field; // on each level field are usally several objects, first is windowVPixelXPos, second windowVPixelYPos
    private String author = "McMinos";
    private int number = 199;
    private int showNumber = 199;
    private String accessCode = "";
    private int width = 20;
    private int vPixelsWidth = 0;
    private int height = 20;
    private int vPixelsHeight = 0;
    private int visibleWidth = 20;
    private int visibleHeight = 20;
    private boolean scrollX = false;
    private boolean scrollY = false;
    private String background = "default";
    private int time = 0;
    private int restart = 0;
    private boolean mirror = false;
    private int mcminosSpeed = 1;
    private int[] ghost = {0,0,0,0};
    private int[] ghostTime = {0, 0, 0, 0};
    private int[] ghostSpeed = {0, 0, 0, 0};
    private int[] ghostAgility = {0, 0, 0, 0};
    private int[] ghostPillMax  = {0, 0, 0, 0};
    private int[] ghostPillFreq  = {0, 0, 0, 0};
    private int[] ghostTranswall  = {0, 0, 0, 0};
    private int livesMin = 0, livesMax = 999;
    private int keysMin = 0, keysMax = 999;
    private int dynamitesMin = 0, dynamitesMax = 999;
    private int minesMin = 0, minesMax = 999;
    private int chocolatesMin = 0, chocolatesMax = 999;
    private int medicinesMin = 0, medicinesMax = 999;
    private int umbrellasMin = 0, umbrellasMax = 999;


    Level ( String filename ) {
        load( filename);
    }

    private void load(String filename) {
        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Gdx.files.internal(filename).read()), 2048);

        String line = null;
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
                        switch(strList[0]) {
                            case "LEVEL": readLevel=true; break;
                            case "AUTHOR": author = strList[1]; break;
                            case "NUMBR": number = Integer.parseInt(strList[1]); break;
                            case "SHOWNR": showNumber = Integer.parseInt(strList[1]); break;
                            case "ACCCD": accessCode = strList[1]; break;
                            case "LWID": width = Integer.parseInt(strList[1]); break;
                            case "LHI": height = Integer.parseInt(strList[1]); break;
                            case "VWID": visibleWidth = Integer.parseInt(strList[1]); break;
                            case "VHI": visibleHeight = Integer.parseInt(strList[1]); break;
                            case "SCROLLX": scrollX = "1".equals(strList[1]); break;
                            case "SCROLLY": scrollY = "1".equals(strList[1]); break;
                            case "BACK": background = strList[1]; break;
                            case "LTIME": time = Integer.parseInt(strList[1]); break;
                            case "RSTRT": restart = Integer.parseInt(strList[1]); break;
                            case "MIRROR": mirror = "1".equals(strList[1]); break;
                            case "MCSPEED": mcminosSpeed = Integer.parseInt(strList[1]); break;
                            case "GHOST1": ghost[0] = Integer.parseInt(strList[1]); break;
                            case "GRTIME1": ghostTime[0] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED1": ghostSpeed[0] = Integer.parseInt(strList[1]); break;
                            case "AGIL1": ghostAgility[0] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX1": ghostPillMax[0] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ1": ghostPillFreq[0] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL1": ghostTranswall[0] = Integer.parseInt(strList[1]); break;
                            case "GHOST2": ghost[1] = Integer.parseInt(strList[1]); break;
                            case "GRTIME2": ghostTime[1] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED2": ghostSpeed[1] = Integer.parseInt(strList[1]); break;
                            case "AGIL2": ghostAgility[1] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX2": ghostPillMax[1] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ2": ghostPillFreq[1] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL2": ghostTranswall[1] = Integer.parseInt(strList[1]); break;
                            case "GHOST3": ghost[2] = Integer.parseInt(strList[1]); break;
                            case "GRTIME3": ghostTime[2] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED3": ghostSpeed[2] = Integer.parseInt(strList[1]); break;
                            case "AGIL3": ghostAgility[2] = Integer.parseInt(strList[1]); break;
                            case "PILLMAX3": ghostPillMax[2] = Integer.parseInt(strList[1]); break;
                            case "PILLFREQ3": ghostPillFreq[2] = Integer.parseInt(strList[1]); break;
                            case "TRANSWALL3": ghostTranswall[2] = Integer.parseInt(strList[1]); break;
                            case "GHOST4": ghost[3] = Integer.parseInt(strList[1]); break;
                            case "GRTIME4": ghostTime[3] = Integer.parseInt(strList[1]); break;
                            case "GHSPEED4": ghostSpeed[3] = Integer.parseInt(strList[1]); break;
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
        GameGraphics bggfx = null;
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
                bggfx = Entities.backgrounds_blue_balls;
                break;
        }
        for( int x=0; x<width; x++)
            for( int y=0; y<height; y++)
            {
                LevelBlock f = field[x][y];
                f.updateWall();
                f.updateCastle();
                // background
                if(x % bggfx.getWidth() == 0 && y % bggfx.getHeight() == 0) {
                    LevelObject lo = new LevelObject(x, y, Entities.backgrounds_blue_balls.getzIndex());
                    lo.setGfx(bggfx);
                }
            }
        // update soem related variables
        vPixelsWidth = width << Game.virtualBlockResolutionExponent;
        vPixelsHeight = height << Game.virtualBlockResolutionExponent;
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
                    break;
                case 'X':
                    lb.makeWall();
                    break;
                case 'Z':
                    lb.makeIndestructableWall();
                    break;
                case '.':
                    lb.makePill();
                    // TODO: count the pills
                    break;
                case '*':
                    lb.makePowerPill1();
                    break;
                case 'C':
                    lb.makeCastle();
                    break;
                case 'G':
                    lb.makeGhost1();
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
                case 'b':
                    lb.makeBomb();
                    break;
                case 'r':
                    lb.makeRock();
                    break;

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
    public LevelBlock getDown( int x, int y ) {
        y--;
        if(y<0) {
            if (scrollY) y += height;
            else return null;
        }
        return field[x][y];
    }

    /**
     * Find level block right of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getRight( int x, int y) {
        x++;
        if(x>=width) {
            if (scrollX) x -= width;
            else return null;
        }
        return field[x][y];
    }

    /**
     * Find level block on top of the given windowVPixelXPos,windowVPixelYPos position.
     * @param x
     * @param y
     * @return return levelblock or null if there is no one.
     */
    public LevelBlock getUp( int x, int y) {
        y++;
        if(y>=height) {
            if (scrollY) y -= height;
            else return null;
        }
        return field[x][y];
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


    public int getVPixelsWidth() {
        return vPixelsWidth;
    }

    public int getVPixelsHeight() {
        return vPixelsHeight;
    }

    public int getVisibleWidth() {
        return visibleWidth;
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
}
