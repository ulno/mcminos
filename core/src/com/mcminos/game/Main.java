package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ulno on 27.08.15.
 * <p/>
 * This is the Main class from where the game is controlled.
 */
public class Main extends com.badlogic.gdx.Game {
    private Audio audio;
    public static final String TEXT_FILE = "text";
    public static final String DEFAULT_UISKIN = "uiskins/default/uiskin.json";
    public static final String DEFAULT_ATLAS = "uiskin.atlas";
    public static final String LEVEL_FONT = "level";
    public static final String MENU_FONT = "menu";
    public static final String GAME_STATS_FILE = "user-stats";
    private HashMap<Integer, BitmapFont> levelFontList = new HashMap<>();
    private HashMap<Integer, BitmapFont> menuFontList = new HashMap<>();
    private HashMap<Integer, Skin> levelSkinList = new HashMap<>();
    private HashMap<Integer, Skin> menuSkinList = new HashMap<>();
    private int symbolResolution;
    // obsolet with new menu private ArrayList<String> levelNamesList;
    public final static String versionStringFile = "VERSIONSTRING";
    private String versionString;
    private LevelsConfig levelsConfig;
    private MainMenu mainMenu;
    private InputProcessor defaultInputProcessor;
    private Statistics userStats;
    private Preferences preferences;

    public LevelsConfig getLevelsConfig() {
        return levelsConfig;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG); // TODO: set to info again
        Gdx.graphics.setVSync(true); // try some magic on the desktop TODO: check if this has any effect
        audio = new Audio();
        preferences = new Preferences(this);
        //loadSkinAndFont(8);
        //loadSkinAndFont(16);
        loadSkinAndFont(32);
        //loadSkinAndFont(64);
        //loadSkinAndFont(128);
        defaultInputProcessor = Gdx.input.getInputProcessor();

        try {
            versionString = new BufferedReader(Gdx.files.internal(versionStringFile).reader()).readLine();
        } catch (IOException e) {
            Gdx.app.log("Main","Trouble reading version string",e);
            versionString = "undefined";
        }

        this.setScreen(new Load(this));
    }

    /**
     * callback from Load to save levelsconfig here
     * @param lc
     */
    public void initLevelsConfig(LevelsConfig lc) {
        this.levelsConfig = lc;
    }

    public void initMainMenu( MainMenu mainMenu ) {
        this.mainMenu = mainMenu;
        mainMenu.init();
    }


    /* obsolet with new menu
    private void readLevelList() {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Gdx.files.internal("levels/list").read()), 2048);
        String line;
        levelNamesList = new ArrayList<>();

        try {
            while ((line = br.readLine()) != null) {
                line = line.trim(); // remove whitespace
                levelNamesList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    } */

    public void loadSkinAndFont(int res) {
        String fontName = "fonts/" + LEVEL_FONT + "-" + res + ".fnt";
        BitmapFont levelFont = new BitmapFont(Gdx.files.internal(fontName));
        levelFontList.put(res, levelFont);
        fontName = "fonts/" + MENU_FONT + "-" + res + ".fnt";
        BitmapFont menuFont = new BitmapFont(Gdx.files.internal(fontName));
        menuFontList.put(res, menuFont);
        levelSkinList.put(res, createSkinWithFont(levelFont));
        menuSkinList.put(res, createSkinWithFont(menuFont));
    }

    public void loadUserStats() {
        userStats = new Statistics(this,GAME_STATS_FILE);
    }

    public Statistics getUserStats() {
        return userStats;
    }

    private Skin createSkinWithFont(BitmapFont font) {
        Skin skin = new Skin();
        skin.add("default-font", font, BitmapFont.class);
        FileHandle fileHandle = Gdx.files.internal(DEFAULT_UISKIN);
        FileHandle atlasFile = fileHandle.sibling(DEFAULT_ATLAS);
        if (atlasFile.exists()) {
            skin.addRegions(new TextureAtlas(atlasFile));
        }
        skin.load(fileHandle);
        //BitmapFont skinFont = skin.getFont("default-font");
        //skinFont.getData().setScale(2.0f);
        return skin;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose() {
        audio.dispose();
        for (Skin s : levelSkinList.values()) {
            s.dispose();
        }
        for (BitmapFont f : levelFontList.values()) {
            f.dispose();
        }
        mainMenu.dispose();
        Gdx.app.exit();
    }

    public BitmapFont getLevelFont(int res) {
        return levelFontList.get(res);
    }

    public BitmapFont getMenuFont(int res) {
        return levelFontList.get(res);
    }

    public Skin getLevelSkin(int res) {
        return levelSkinList.get(res);
    }

    public Skin getMenuSkin(int res) {
        return menuSkinList.get(res);
    }

    public Audio getAudio() {
        return audio;
    }

    /* obsolet with new menu
    public ArrayList<String> getLevelNames() {
        return levelNamesList;
    }
*/

    /* obsolet with new menu
    public String getNextLevel(String currentLevel) {
        int index = levelNamesList.indexOf(currentLevel);
        if (index < 0) return null;
        if (index == levelNamesList.size() - 1) { // Last level
            // TODO: trigger something special
            return levelNamesList.get(0); // start over for now
        }
        return levelNamesList.get(index + 1);
    }
*/

    public String getVersionString() {
        return versionString;
    }

    public void activateMainMenu( LevelConfig currentLevel ) {
        mainMenu.init();
        mainMenu.activateLevel(currentLevel);
        mainMenu.resize();
        setScreen(mainMenu);
        mainMenu.restoreInputProcessor();
    }

    public void levelEndCongrats( LevelConfig currentLevelConfig) {
        setScreen( new Congrats(this, currentLevelConfig) );
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void exit() {
        dispose();
        Gdx.app.exit();
    }
}
