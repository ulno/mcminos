package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import net.ulno.libni.gdxReceiver.GdxReceiverMapping;
import net.ulno.libni.gdxReceiver.GdxReceiverMultiplexer;
import net.ulno.libni.gdxReceiver.GdxKeyboardMapping;
import net.ulno.libni.gdxReceiver.GdxMergedInput;
import net.ulno.libni.receiver.LibniMapping;
import net.ulno.libni.receiver.NetworkReceiver;
import net.ulno.libni.receiver.NewNetworkReceiverCallback;

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
    private static final String LIBNI_CONFIG_FILE = ".mcminos.libni";
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
    private GdxMergedInput libniMergedInput;

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
        this.libniMergedInput = new GdxMergedInput(
                new GdxKeyboardMapping()
                        .addButton(LibniMapping.BUTTON_UP, Input.Keys.W)
                        .addButton(LibniMapping.BUTTON_RIGHT, Input.Keys.D)
                        .addButton(LibniMapping.BUTTON_DOWN, Input.Keys.S)
                        .addButton(LibniMapping.BUTTON_LEFT, Input.Keys.A)
                        .addButton(LibniMapping.BUTTON_UP, Input.Keys.UP)
                        .addButton(LibniMapping.BUTTON_RIGHT, Input.Keys.RIGHT)
                        .addButton(LibniMapping.BUTTON_DOWN, Input.Keys.DOWN)
                        .addButton(LibniMapping.BUTTON_LEFT, Input.Keys.LEFT)
                        .addButton(LibniMapping.BUTTON_ESCAPE, Input.Keys.ESCAPE)
                        .addButton(LibniMapping.BUTTON_ESCAPE, Input.Keys.T)
                        .addButton(LibniMapping.BUTTON_ESCAPE, Input.Keys.M)
                        .addButton('p', Input.Keys.P)
                        .addButton(LibniMapping.BUTTON_FIRE, Input.Keys.SPACE)
                        .addButton(LibniMapping.BUTTON_FIRE, Input.Keys.ENTER)
                        .addButton(LibniMapping.BUTTON_FIRE, Input.Keys.Q)
                        .addButton(LibniMapping.BUTTON_FIRE, 23) //amazon fire remote select
                        .addButton(LibniMapping.BUTTON_ESCAPE, 82) // amazon fire menu
                        .addButton(LibniMapping.BUTTON_ESCAPE, 85) // amazon fire play/pause

                        // 89: // amazon fire wind back
                        // 90: // amazon fire wind forward
                ,

                new NewNetworkReceiverCallback() {

                    @Override
                    public void receiveController(NetworkReceiver controller) {
                        // configure mapping
                        // as McMinos is not multiplayer, we assign all controllers the same (default) mapping
                        // default mapping was already initialized, so nothing to do here
                    }
                },
                Gdx.files.external(LIBNI_CONFIG_FILE),
                new GdxReceiverMapping()
                        .addButton(LibniMapping.BUTTON_UP, GdxReceiverMultiplexer.POV_NORTH)
                        .addButton(LibniMapping.BUTTON_UP, GdxReceiverMultiplexer.POV_NORTHWEST)
                        .addButton(LibniMapping.BUTTON_UP, GdxReceiverMultiplexer.POV_NORTHEAST)
                        .addButtonFromAnalog(LibniMapping.BUTTON_UP,1,false)
                        .addButtonFromAnalog(LibniMapping.BUTTON_UP,3,false)
                        .addButtonFromAnalog(LibniMapping.BUTTON_UP,5,false)
                        .addButton(LibniMapping.BUTTON_RIGHT, GdxReceiverMultiplexer.POV_EAST)
                        .addButton(LibniMapping.BUTTON_RIGHT, GdxReceiverMultiplexer.POV_NORTHEAST)
                        .addButton(LibniMapping.BUTTON_RIGHT, GdxReceiverMultiplexer.POV_SOUTHEAST)
                        .addButtonFromAnalog(LibniMapping.BUTTON_RIGHT,0,true)
                        .addButtonFromAnalog(LibniMapping.BUTTON_RIGHT,2,true)
                        .addButtonFromAnalog(LibniMapping.BUTTON_RIGHT,4,true)
                        .addButton(LibniMapping.BUTTON_DOWN, GdxReceiverMultiplexer.POV_SOUTH)
                        .addButton(LibniMapping.BUTTON_DOWN, GdxReceiverMultiplexer.POV_SOUTHWEST)
                        .addButton(LibniMapping.BUTTON_DOWN, GdxReceiverMultiplexer.POV_SOUTHEAST)
                        .addButtonFromAnalog(LibniMapping.BUTTON_DOWN,1,true)
                        .addButtonFromAnalog(LibniMapping.BUTTON_DOWN,3,true)
                        .addButtonFromAnalog(LibniMapping.BUTTON_DOWN,5,true)
                        .addButton(LibniMapping.BUTTON_LEFT, GdxReceiverMultiplexer.POV_WEST)
                        .addButton(LibniMapping.BUTTON_LEFT, GdxReceiverMultiplexer.POV_NORTHWEST)
                        .addButton(LibniMapping.BUTTON_LEFT, GdxReceiverMultiplexer.POV_SOUTHWEST)
                        .addButtonFromAnalog(LibniMapping.BUTTON_LEFT,0,false)
                        .addButtonFromAnalog(LibniMapping.BUTTON_LEFT,2,false)
                        .addButtonFromAnalog(LibniMapping.BUTTON_LEFT,4,false)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON1)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON2)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON3)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON4)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON5)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON6)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON7)
                        .addButton(LibniMapping.BUTTON_FIRE, GdxReceiverMultiplexer.BUTTON8)
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

    public GdxMergedInput getLibniMergedInput() {
        return libniMergedInput;
    }
}
