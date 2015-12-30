package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelConfig {
    public static final int ghostTypesCount = 4;
    private int symbolStep;
    private Graphics symbol;
    private String id;
    private HashMap<String, String> title = new HashMap<>();
    private HashMap<String, String> body = new HashMap<>();
    private LevelCategory category;
    private int nr = -1;
    private LevelsConfig levelsConfig;

    private String author = "Main";
    private int number = 199;
    private int showNumber = 199;
    private String accessCode = "";
    private int width = 20;
    private int height = 20;
    private int visibleWidth = 100;
    private int visibleHeight = 100;
    private boolean scrollX = false;
    private boolean scrollY = false;
    private Graphics background;
    private int time = 0;
    private int restart = 0;
    private boolean mirror = false;
    public int mcminosSpeed = 1;
    public int[] ghostMax = {0, 0, 0, 0};
    public int[] ghostTime = {0, 0, 0, 0};
    public int[] ghostSpeed = {0, 0, 0, 0};
    public int[] ghostAgility = {0, 0, 0, 0};
    public int ghostPillDrop = 0;
    public int ghostPillFreq = 0;
    public int[] ghostTranswall = {0, 0, 0, 0};
/*    private int livesMin = 0, livesMax = 999;
    private int keysMin = 0, keysMax = 999;
    private int dynamitesMin = 0, dynamitesMax = 999;
    private int minesMin = 0, minesMax = 999;
    private int chocolatesMin = 0, chocolatesMax = 999;
    private int medicinesMin = 0, medicinesMax = 999;
    private int umbrellasMin = 0, umbrellasMax = 999;*/
    private String levelData;


    public LevelConfig(LevelsConfig lc, String levelFileName) {
        String backgroundString = "default";

        levelsConfig = lc;
        String[] split = levelFileName.split("/");
        String categoryId = "";
        if (split.length > 1) {
            categoryId = split[0];
            id = split[1];
        } else {
            id = split[0];
        }
        LevelCategory levelCategory = levelsConfig.getCategory(categoryId);
        if (levelCategory == null) { // doesn't exist yet
            category = new LevelCategory(lc, categoryId); // adds itself to levelconfig
        } else {
            category = levelCategory;
        }
        nr = category.size(); // add correct nr
        category.add(this); // add this level to the category

        // load data from level filename
        BufferedReader br = new BufferedReader(Gdx.files.internal("levels/" + levelFileName + ".asx").reader());

        // set defaults for title an dbody
        title.put("en", id);
        body.put("en", "Play and enjoy level " + id + "!");

        KeyValue kv;
        while ((kv = new KeyValue(br)).key != null) {
            // try minmax-split
            int min = 0, max = 0;

            String[] minmax = kv.value.split("\\s*,\\s*");
            if (minmax.length > 1) {
                try {
                    min = Integer.parseInt(minmax[0]);
                    max = Integer.parseInt(minmax[1]);
                } catch (NumberFormatException nfe) {
                }
            }

            // apply minmax on existing mcminos - we don't do this anymore, levels are always started with 0
            switch (kv.key) {
                case "AUTHOR":
                    author = kv.value;
                    break;
                                /* case "NUMBR":
                                    number = Integer.parseInt(kv.value);
                                    break;
                                case "SHOWNR":
                                    showNumber = Integer.parseInt(kv.value);
                                    break;
                                case "ACCCD":
                                    accessCode = kv.value;
                                    break; */
                case "LWID":
                    width = Integer.parseInt(kv.value);
                    break;
                case "LHI":
                    height = Integer.parseInt(kv.value);
                    break;
                case "VWID":
                    visibleWidth = Integer.parseInt(kv.value);
                    break;
                case "VHI":
                    visibleHeight = Integer.parseInt(kv.value);
                    break;
                case "SCROLLX":
                    // for adapting this game to a mobile platform allowing infinite levels only makes sense, when scrolling is enabled in the respective direction
                    scrollX = "1".equals(kv.value);
                    break;
                case "SCROLLY":
                    scrollY = "1".equals(kv.value);
                    break;
                case "BACK":
                    backgroundString = kv.value;
                    break;
                case "LTIME":
                    time = Integer.parseInt(kv.value);
                    break;
                case "RSTRT":
                    restart = Integer.parseInt(kv.value);
                    break;
                case "MIRROR":
                    mirror = "1".equals(kv.value);
                    break;
                case "MCSPEED":
                    mcminosSpeed = Integer.parseInt(kv.value);
                    break;
                case "GHOST1":
                    ghostMax[0] = Integer.parseInt(kv.value);
                    break;
                case "GRTIME1":
                    ghostTime[0] = Integer.parseInt(kv.value);
                    break;
                case "GHSPEED1":
                    ghostSpeed[0] = Integer.parseInt(kv.value);
                    break;
                case "AGIL1":
                    ghostAgility[0] = Integer.parseInt(kv.value);
                    break;
                case "TRANSWALL1":
                    ghostTranswall[0] = Integer.parseInt(kv.value);
                    break;
                case "GHOST2":
                    ghostMax[1] = Integer.parseInt(kv.value);
                    break;
                case "GRTIME2":
                    ghostTime[1] = Integer.parseInt(kv.value);
                    break;
                case "GHSPEED2":
                    ghostSpeed[1] = Integer.parseInt(kv.value);
                    break;
                case "AGIL2":
                    ghostAgility[1] = Integer.parseInt(kv.value);
                    break;
                case "PILLMAX2":
                    ghostPillDrop = Integer.parseInt(kv.value);
                    break;
                case "PILLFREQ2":
                    ghostPillFreq = Integer.parseInt(kv.value);
                    break;
                case "TRANSWALL2":
                    ghostTranswall[1] = Integer.parseInt(kv.value);
                    break;
                case "GHOST3":
                    ghostMax[2] = Integer.parseInt(kv.value);
                    break;
                case "GRTIME3":
                    ghostTime[2] = Integer.parseInt(kv.value);
                    break;
                case "GHSPEED3":
                    ghostSpeed[2] = Integer.parseInt(kv.value);
                    break;
                case "AGIL3":
                    ghostAgility[2] = Integer.parseInt(kv.value);
                    break;
                case "TRANSWALL3":
                    ghostTranswall[2] = Integer.parseInt(kv.value);
                    break;
                case "GHOST4":
                    ghostMax[3] = Integer.parseInt(kv.value);
                    break;
                case "GRTIME4":
                    ghostTime[3] = Integer.parseInt(kv.value);
                    break;
                case "GHSPEED4":
                    ghostSpeed[3] = Integer.parseInt(kv.value);
                    break;
                case "AGIL4":
                    ghostAgility[3] = Integer.parseInt(kv.value);
                    break;
                case "TRANSWALL4":
                    ghostTranswall[3] = Integer.parseInt(kv.value);
                    break;
                                /*case "LIVE":
                                    livesMin = min;
                                    livesMax = max;
                                    break;
                                case "KEYS":
                                    keysMin = min;
                                    keysMax = max;
                                    break;
                                case "DYNA":
                                    dynamitesMin = min;
                                    dynamitesMax = max;
                                    break;
                                case "MINE":
                                    minesMin = min;
                                    minesMax = max;
                                    break;
                                case "CHOC":
                                    chocolatesMin = min;
                                    chocolatesMax = max;
                                    break;
                                case "MEDC":
                                    medicinesMin = min;
                                    medicinesMax = max;
                                    break;
                                case "UMBR":
                                    umbrellasMin = min;
                                    umbrellasMax = max;
                                    break; */
                case "LEVEL":
                    levelData = kv.value;
                    break;
                case "SYMBOL":
                    if(kv.value.contains(",")) {
                        String keysplit[] = kv.value.split(",");
                        symbol = Graphics.getByName(keysplit[0].trim());
                        symbolStep = Integer.parseInt(keysplit[1].trim());
                    } else {
                        symbol = Graphics.getByName(kv.value);
                        symbolStep = 0;
                    }
                    break;
                case "TITLE":
                    title.put("en", kv.value);
                    break;
                case "BODY":
                    body.put("en", kv.value);
                    break;
                default:
                    if (kv.key.startsWith("TITLE-")) {
                        String lang = kv.key.substring(6);
                        title.put(lang, kv.value);
                    } else if (kv.key.startsWith("BODY-")) {
                        String lang = kv.key.substring(5);
                        body.put(lang, kv.value);
                    }
            } // end switch
        } //end while
        try {
            br.close();
        } catch (IOException e) {
        }

        // select background
        switch (backgroundString) {
            case "0":
            case "black":
                background = Entities.backgrounds_black;
                break;
            case "1":
            case "pavement-01":
                background = Entities.backgrounds_pavement_01;
                break;
            case "amoeboid-01":
                background = Entities.backgrounds_amoeboid_01;
                break;
            case "2":
            case "gravel-01":
                background = Entities.backgrounds_gravel_01;
                break;
            case "3":
            case "meadow-flowers":
                background = Entities.backgrounds_meadow_flowers;
                break;
            case "4":
            case "sand-01":
                background = Entities.backgrounds_sand_01_sand;
                break;
            case "5":
            case "soil-01":
                background = Entities.backgrounds_soil_01;
                break;
            default:
                background = Entities.backgrounds_pavement_01;
                break;
        }
    }


    public String getTitle(String lang) {
        if (hasTitle(lang)) {
            return title.get(lang);
        } else {
            if(!hasTitle("en")) return null;
            return title.get("en");
        }
    }

    public boolean hasTitle(String lang) {
        return title.containsKey(lang);
    }

    public String getBody(String lang) {
        if (hasBody(lang)) {
            return body.get(lang);
        } else {
            if(!hasBody("en")) return null;
            return body.get("en");
        }
    }

    public boolean hasBody(String lang) {
        return body.containsKey(lang);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return category.getId() + "/" + getId();
    }

    public int getCategoryNr() {
        return category.getNr();
    }

    public int getNr() {
        return nr;
    }

    public LevelConfig getNextLevel() {
        return category.get(nr + 1);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getLevelData() {
        return levelData;
    }

    public Graphics getBackground() {
        return background;
    }

    public boolean getScrollX() {
        return scrollX;
    }

    public boolean getScrollY() {
        return scrollY;
    }

    public int getRestart() {
        return restart;
    }

    public boolean isMirror() {
        return mirror;
    }

    public String getAuthor() {
        return author;
    }

    public int getVisibleWidth() {
        return visibleWidth;
    }

    public int getVisibleHeight() {
        return visibleHeight;
    }

    public int getGhostPillDrop() {
        return ghostPillDrop;
    }

    public int getGhostPillFreq() {
        return ghostPillFreq;
    }

    public int getGhostTranswall(int ghostnr) {
        return ghostTranswall[ghostnr];
    }

    public int getGhostMax(int ghostnr) {
        return ghostMax[ghostnr];
    }

    public int getGhostTime(int ghostnr) {
        return ghostTime[ghostnr];
    }

    public int getGhostAgility(int ghostNr) {
        return ghostAgility[ghostNr];
    }

    public TextureRegion getSymbol(int res) {
        if(symbol != null) {
            return symbol.getTextureDirectStep(res, symbolStep);
        }
        return null;
    }

    public LevelCategory getCategory() {
        return category;
    }
}
