package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.ulno.libni.receiver.libgdx.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ulno on 27.08.15.
 * <p/>
 * This is the Main class from where the game is controlled.
 */
public class Main extends com.badlogic.gdx.Game {
    // if the following file exists in teh hom edirectory or on the sd-card in android and there is a valid hostname in
    // here, then the mqtt-controller is active and reacts to messages sent on the NetworkLibniController/McMinos topic
    private static final String GAMENET_CONFIG_FILE = ".mcminos.libniMergedInput";
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
    private Statistics statistics;
    private Preferences preferences;
    private LibniMergedInput libniMergedInput;

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

        initLibniInput();

        this.setScreen(new Load(this));
    }

    private void initLibniInput() {
        this.libniMergedInput = new LibniMergedInput(
                new KeyboardMapping()
                        .addButton(LibniMergedInput.BUTTON_UP, Input.Keys.W)
                        .addButton(LibniMergedInput.BUTTON_RIGHT, Input.Keys.D)
                        .addButton(LibniMergedInput.BUTTON_DOWN, Input.Keys.S)
                        .addButton(LibniMergedInput.BUTTON_LEFT, Input.Keys.A)
                        .addButton(LibniMergedInput.BUTTON_UP, Input.Keys.UP)
                        .addButton(LibniMergedInput.BUTTON_RIGHT, Input.Keys.RIGHT)
                        .addButton(LibniMergedInput.BUTTON_DOWN, Input.Keys.DOWN)
                        .addButton(LibniMergedInput.BUTTON_LEFT, Input.Keys.LEFT)
                        .addButton(LibniMergedInput.BUTTON_ESCAPE, Input.Keys.ESCAPE)
                        .addButton(LibniMergedInput.BUTTON_ESCAPE, Input.Keys.T)
                        .addButton(LibniMergedInput.BUTTON_ESCAPE, Input.Keys.M)
                        .addButton('p', Input.Keys.P)
                        .addButton(LibniMergedInput.BUTTON_FIRE, Input.Keys.SPACE)
                        .addButton(LibniMergedInput.BUTTON_FIRE, Input.Keys.ENTER)
                        .addButton(LibniMergedInput.BUTTON_FIRE, 23) //amazon fire remote select
                        .addButton(LibniMergedInput.BUTTON_ESCAPE, 82) // amazon fire menu
                        .addButton(LibniMergedInput.BUTTON_ESCAPE, 85) // amazon fire play/pause

                        // 89: // amazon fire wind back
                        // 90: // amazon fire wind forward
                ,

                new NewControllerCallback() {

                    @Override
                    public void receiveController(NetworkController controller) {
                        // configure mapping
                        // as McMinos is not multiplayer, we assign all controllers the same (default) mapping
                        // default mapping was already initialized, so nothing to do here
                    }
                },
                Gdx.files.external(GAMENET_CONFIG_FILE),
                new GdxControllerMapping()
                        .addButton(LibniMergedInput.BUTTON_UP, GdxControllerMultiplexer.POV_NORTH)
                        .addButton(LibniMergedInput.BUTTON_UP, GdxControllerMultiplexer.POV_NORTHWEST)
                        .addButton(LibniMergedInput.BUTTON_UP, GdxControllerMultiplexer.POV_NORTHEAST)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_UP,0,true)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_UP,2,true)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_UP,4,true)
                        .addButton(LibniMergedInput.BUTTON_RIGHT, GdxControllerMultiplexer.POV_WEST)
                        .addButton(LibniMergedInput.BUTTON_RIGHT, GdxControllerMultiplexer.POV_NORTHWEST)
                        .addButton(LibniMergedInput.BUTTON_RIGHT, GdxControllerMultiplexer.POV_SOUTHWEST)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_RIGHT,1,true)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_RIGHT,3,true)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_RIGHT,5,true)
                        .addButton(LibniMergedInput.BUTTON_DOWN, GdxControllerMultiplexer.POV_SOUTH)
                        .addButton(LibniMergedInput.BUTTON_DOWN, GdxControllerMultiplexer.POV_SOUTHWEST)
                        .addButton(LibniMergedInput.BUTTON_DOWN, GdxControllerMultiplexer.POV_SOUTHEAST)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_DOWN,0,false)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_DOWN,2,false)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_DOWN,4,false)
                        .addButton(LibniMergedInput.BUTTON_LEFT, GdxControllerMultiplexer.POV_EAST)
                        .addButton(LibniMergedInput.BUTTON_LEFT, GdxControllerMultiplexer.POV_NORTHEAST)
                        .addButton(LibniMergedInput.BUTTON_LEFT, GdxControllerMultiplexer.POV_SOUTHEAST)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_LEFT,1,false)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_LEFT,3,false)
                        .addButtonFromAnalog(LibniMergedInput.BUTTON_LEFT,5,false)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON1)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON2)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON3)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON4)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON5)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON6)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON7)
                        .addButton(LibniMergedInput.BUTTON_FIRE, GdxControllerMultiplexer.BUTTON8)
        );
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
        //mainMenu.init();
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
        statistics = new Statistics(this,GAME_STATS_FILE);
    }

    public Statistics getStatistics() {
        return statistics;
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
        // this is also called by Gdx.app.exit(), but not on mac
    }

    public void preDispose() {
        libniMergedInput.dispose();
        audio.dispose();
        for (Skin s : levelSkinList.values()) {
            s.dispose();
        }
        for (BitmapFont f : levelFontList.values()) {
            f.dispose();
        }
        mainMenu.dispose();
    }

    public BitmapFont getLevelFont(int res) {
        BitmapFont font = levelFontList.get(res);
        font.setColor(1,1,1,1); // always reset to bright here, so if it needs to be changed, color has to be set after getting
        return font;
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
        if(currentLevel == null) { // if not given, find last level played
            currentLevel = statistics.getLastLevel();
        }
        mainMenu.init();
        mainMenu.activateLevel(currentLevel);
        mainMenu.resize();
        setScreen(mainMenu);
        //mainMenu.restoreInputProcessor();
    }

    public void levelEndCongrats( LevelConfig currentLevelConfig) {
        setScreen( new Congrats(this, currentLevelConfig) );
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void exit() {
        // dispose(); is called by Gdx.app.exit()
        preDispose(); // but not this
        Controllers.clearListeners();
        Gdx.app.exit();
    }

    public void fadeExit() {
        setScreen( new FadeExit(this) );
    }

    public LibniMergedInput getLibniMergedInput() {
        return libniMergedInput;
    }
}
