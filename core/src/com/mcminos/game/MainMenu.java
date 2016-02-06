package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenu implements Screen, InputProcessor, ControllerListener, MqttControllerListener {
    public final static String WEBSITE="http://mcminos.com";
    private final LevelsConfig levelsConfig;
    private final Statistics statistics;
    private final Audio audio;
    private final Preferences preferences;
    private boolean resumeRequested = false; // only resume, if resume-file detected
    private Skin bigMenuSkin;
    private Skin levelSkin;
    private Skin textSkin;
    private Skin bigLevelSkin;
    private final Stage stage;
    private final Table table;
    private final TextureRegion bg;
    private final SpriteBatch batch;
    private final Main main;
    private final Table rootTable;
    private final Fader fader;
//    private Label versionStringActor;

    private boolean fullscreen;
    private LevelConfig selectedLevel = null; // nothing selected in the beginning
    private int activatedCategory = 0; // first is selected by default
    private String language;
    private BitmapFont levelFont;
    private HashMap<String, Texture> textureCache = new HashMap<>();
    private Table currentDialog = null;
    private HotSpot hotSpotRoot;
    private HotSpot hotSpotSelected;
    private long frames = 0;
    private long lastFrames=0;

    Vector2 coords = new Vector2();
    private HotSpot hotSpotPreferencesRoot;
    private int keyDirections;
    private MqttController mqttController;


    public MainMenu(final Main main) {
//        final MainMenu thisScreen = this; // TODO: check why we need this

        this.main = main;
        this.preferences = main.getPreferences();
        this.fullscreen = preferences.getFullScreen();
        this.audio = main.getAudio();
        this.statistics = main.getStatistics();
        batch = new SpriteBatch();
        levelsConfig = main.getLevelsConfig();
        bg = Entities.backgrounds_amoeboid_01.getTexture(Preferences.MAXRES, 0); // can be fixed as bg is not so critical

        stage = new Stage(new ScreenViewport(), batch);

        // in init Gdx.input.setInputProcessor(new InputMultiplexer(stage,this));

        fader = new Fader(main,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        // root table covering the screen
        rootTable = new Table();
        stage.addActor(rootTable);
        rootTable.setPosition(0, 0);

        // table for menu
        table = new Table();
        rootTable.add(table).top().center().fill().expand();

        rootTable.background(new BackroundDrawer(bg));

        hotSpotRoot = new HotSpot(null,null,-1);
        hotSpotSelected = hotSpotRoot;

// happens in resize        rebuildMenu();

/*        selectedLevel = statistics.getLastLevel();  // make sure, we can scroll to start
        activatedCategory = selectedLevel.getCategoryNr();
        later set in activate level through main
*/
        // eventually continue a paused/suspended game
        if (Game.getSaveFileHandle(0).exists()) {
            resumeRequested = true;
        }
    }

    private void switchLevelCategory(int category) {
//        private void switchLevelCategory(int category, Label categoryLabel, Table twoColumns, ScrollPane folderSelector, SymbolButton[] categorySelectorButtons, ScrollPane levelSelector, int res) {
//        categoryLabel.setText(levelsConfig.get(category).getName());
//        categorySelectorButtons[activatedCategory].unselect();
        activatedCategory = category;
//        categorySelectorButtons[activatedCategory].select();
//        // selectedLevel = null; //deselect -- not necessary as it can stay selected

        rebuildMenu();
    }

    private void selectLevel(LevelConfig level) {
        selectedLevel = level;
        main.setScreen(new Play(main, level, 0, 3));
    }


    public void activateLevel(LevelConfig currentLevel) {
        this.selectedLevel = currentLevel;
        if (selectedLevel != null) {
            activatedCategory = selectedLevel.getCategoryNr();
        }
    }

    public void init() {
        audio.musicFixed(0);
        fader.fadeOutIn();
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
        Controllers.clearListeners();
        Controllers.addListener(this);
        keyDirections = 0;
        mqttController = main.getMqttController();
        mqttController.setListener(this);
    }

    @Override
    public void connected(Controller controller) {
    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        updateDirections();
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        updateDirections();
        activateSelection();
        return true;
//        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        updateDirections();
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        updateDirections();
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }

    @Override
    public void mqttDown(char button) {
        updateDirections();
    }

    @Override
    public void mqttUp(char button) {
        updateDirections();
        if(button == ' ') { // our convention for fire
            activateSelection();
        }
    }

    @Override
    public void mqttAnalog(byte analogNr, int value) {

    }


    // inner class for menu
    class CategoryClickListener extends ClickListener {

        private final Table twoColumns;
        private final ScrollPane categorySelector;
        private final ScrollPane levelSelector;
        private final int resolution;
        private final SymbolButton[] categorySelectorButtons;
        private final Label categoryLabel;
        public int category;

        public CategoryClickListener(int c, Label categoryLabel, Table twoColumns, ScrollPane categorySelector, SymbolButton[] buttons, ScrollPane levelSelector, int res) {
            category = c;
            this.categoryLabel = categoryLabel;
            this.twoColumns = twoColumns;
            this.categorySelector = categorySelector;
            this.categorySelectorButtons = buttons;
            this.levelSelector = levelSelector;
            this.resolution = res;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            hotSpotSelected = hotSpotRoot;
            switchLevelCategory(category);
//            switchLevelCategory(category, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector, resolution);
            //super.clicked(event,x,y);
        }
    }

    // inner class for menu
    class LevelClickListener extends ClickListener {

        public LevelConfig level;

        LevelClickListener(LevelConfig lc) {
            this.level = lc;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            selectLevel(level);
        }
    }

    private void rebuildMenu() {
        language = preferences.getLanguage();
        int res = preferences.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        textSkin = main.getMenuSkin(res / 2);
        levelSkin = main.getLevelSkin(res / 2);
        bigLevelSkin = main.getLevelSkin(res);

        // initialize
        table.clear();
        Table twoColumns = new Table();

        // first build only elements, layout later
        Table topRowTable = new Table();
        ScrollPane topRowPane = new ScrollPane(topRowTable);
        topRowPane.isScrollingDisabledY();

        SymbolButton settingsButton = new SymbolButton(res, Entities.menu_symbol_settings.getTexture(res, 0));
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                preferences();
            }
        });
        topRowTable.add(settingsButton.getCell()).left().minHeight(res).padRight(res / 16);

        HotSpot hotSpotSettings = hotSpotRoot.getCreateRight(settingsButton.getCell(), topRowPane, 1);
        HotSpot hs = hotSpotSettings;
        hotSpotRoot.setUp(hs);
        hotSpotRoot.setDown(hs);
        hotSpotRoot.setLeft(hs);

        SymbolButton loadButton = new SymbolButton(res,Entities.menu_symbol_game_load.getTexture(res, 0));
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                save();
            }
        });
        topRowTable.add(loadButton.getCell()).left().minHeight(res).expandX().fillX();
        hs = hs.getCreateRight(loadButton.getCell(), topRowPane, 2);

        SymbolButton www1Button = new SymbolButton(res,Entities.menu_symbol_www.getTexture(res, 0));
        www1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                Gdx.net.openURI(WEBSITE);
            }
        });
        topRowTable.add(www1Button.getCell()).left().minHeight(res).padLeft(res / 4);
        hs = hs.getCreateRight(www1Button.getCell(), topRowPane, 3);

        Image title = new Image( Entities.logo.getTexture(res,0) );
        topRowTable.add(title).prefHeight(res);
        title.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                Gdx.net.openURI(WEBSITE);
            }
        });


        SymbolButton www2Button = new SymbolButton(res,Entities.menu_symbol_www.getTexture(res, 0));
        www2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                Gdx.net.openURI(WEBSITE);
            }
        });
        topRowTable.add(www2Button.getCell()).left().minHeight(res).padRight(res/4).expandX().fillX();
        hs = hs.getCreateRight(www2Button.getCell(), topRowPane, 3);

        SymbolButton infoButton = new SymbolButton(res,Entities.menu_button_info.getTexture(res, 0));
        infoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                credits();
            }
        });
        topRowTable.add(infoButton.getCell()).right().minHeight(res);
        hs = hs.getCreateRight(infoButton.getCell(), topRowPane, 4);


        SymbolButton quitButton = new SymbolButton(res,Entities.menu_button_exit_variant.getTexture(res, 0));
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                leave();
            }
        });
        topRowTable.add(quitButton.getCell()).right().minHeight(res);
        hs.getCreateRight(quitButton.getCell(), topRowPane, 5);

        Label categoryLabel = new Label(levelsConfig.get(activatedCategory).getName(), levelSkin);

        // the scrollpanes with the respective levels for each category
        Cell<Table> lastCell = null;
        LevelCategory cat = levelsConfig.get(activatedCategory);

        Table levelSelectorTable = new Table();
        ScrollPane levelSelector = new ScrollPane(levelSelectorTable);
        //levelSelector[c].setForceScroll(false, true);

        HotSpot levelsHotSpotRoot = null;
        for (int i = 0; i < cat.size(); i++) {
            LevelConfig lc = cat.get(i);
            //Table levelRow = new Table();
            //levelRow.setHeight(res + res / 8);
            Table levelRow = new Table(bigLevelSkin);
            HotSpot levelHS  = new HotSpot(levelRow,levelSelector,1000+i);
            if(levelsHotSpotRoot == null) levelsHotSpotRoot = levelHS;
            else {
                hs.setDown(levelHS);
                levelHS.setUp(hs);
            }
            hs = levelHS;
            //levelRow.setPosition(0, res / 2 + res / 8); // figure out why this shift is necessary -> bug in libgdx? -- not anymore
            //levelRow.addActor(levelRow);
            Group thumbnail = new Group();
            thumbnail.setSize(res, res);
            TextureRegion texture = lc.getSymbol(res);
            if (texture != null) {
                Image snapshot = new Image(texture);
                snapshot.setSize(res, res);
                thumbnail.addActor(snapshot);
            }
            levelRow.add(thumbnail).prefHeight(res).top().left().padRight(res / 4);

            Label t;
            t = new Label((i + 1) + ". " + lc.getTitle(language), levelSkin);
            if (selectedLevel != null && cat.getNr() == selectedLevel.getCategory().getNr() && i == selectedLevel.getNr()) {
                t = new Label((i + 1) + ". >" + lc.getTitle(language), levelSkin);
                Group indicator = new Group();
                indicator.setSize(res, res);
                Image indicatorImage = new Image(Entities.mcminos_default_left.getTextureDirectStep(res, 7));
                indicator.setSize(res, res);
                indicator.addActor(indicatorImage);
                levelRow.add(t).prefHeight(res).top().left();
                levelRow.add(indicator).top().left().fillX().expandX();
                // check why we can't set a background? - doesn't matter showing the symbol looks nice
            } else {
                t = new Label((i + 1) + ". " + lc.getTitle(language), levelSkin);
                levelRow.add(t).prefHeight(res).top().left().fillX().expandX();
            }

            lastCell = levelSelectorTable.add(levelRow).prefHeight(res).top().left().padBottom(res / 16).fillX().expandX();
            levelSelectorTable.row();
            if (statistics.activated(lc)) {
                levelRow.addListener(new LevelClickListener(cat.get(i)));
            } else {
                // t.setColor(0.5f,0.5f,0.5f,1.0f); does not work as this is a colored font
                levelRow.setColor(1, 1, 1, 0.6f); // sets only transparency
            }
            // mark last active level
        }
        //lastCell.expandY().fillY().align(Align.topLeft);
        levelsHotSpotRoot.setUp(hotSpotSettings.getRight());
        for(hs = hotSpotSettings.getRight(); hs !=null; hs=hs.getRight()) {
            hs.setDown(levelsHotSpotRoot);
        }
        for(hs = levelsHotSpotRoot; hs!=null; hs=hs.getDown()) {
            hs.setLeft(hotSpotSettings.getDown());
        }

        // add the buttons for the different level categories to the toolbar
        Table categorySelectorTable = new Table();
        ScrollPane categorySelector = new ScrollPane(categorySelectorTable);
        SymbolButton categorySelectorButtons[] = new SymbolButton[levelsConfig.size()];
        Cell<Group> lastCategory = null;
        hs = hotSpotSettings;
        for (int i = 0; i < levelsConfig.size(); i++) { // loop through categories
            SymbolButton b = new SymbolButton(res, levelsConfig.get(i).getTexture(res));
            hs = hs.getCreateDown(b.getCell(), categorySelector, 100 + i);
            hs.setRight(levelsHotSpotRoot);
            CategoryClickListener listener = new CategoryClickListener(i, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector, res);
            b.addListener(listener);
            categorySelectorButtons[i] = b;

            lastCategory = categorySelectorTable.add(b.getCell()).top().left().prefSize(res).padBottom(res / 16);
            categorySelectorTable.row();
        }
        lastCategory.fillY().expandY();
        categorySelectorButtons[activatedCategory].select();

/*        if(versionStringActor != null) {
            versionStringActor.remove();
        }
        versionStringActor = new Label(main.getVersionString(), levelSkin);
        versionStringActor.setPosition(0,0, Align.bottomRight);
//        versionStringActor.setColor(1,1,1,0.5f);
        rootTable.addActor(versionStringActor); */

// called from there        resize();

        // Layout
        table.align(Align.topLeft);
        table.add(topRowPane).minHeight(res).prefHeight(res).top().fillX().expandX().row();
        table.add(categoryLabel).minHeight(res / 2).prefHeight(res).padLeft(res + res / 2).left().row();
        //switchLevelCategory(activatedCategory, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector, res);
        twoColumns.clear();
        twoColumns.add(categorySelector).fillY().expandY().minWidth(res).prefWidth(res).padRight(res / 2);
        twoColumns.add(levelSelector).fill().expand();
        table.add(twoColumns).top().left().fillX().expandX();

        //table.pack(); // fit table into root-table to update coordinates
        table.layout(); // force layout to have coordinates and dimensions in pane
        // scroll pane to activated level
        if(selectedLevel != null && cat.getNr() == selectedLevel.getCategory().getNr()) {
            ScrollPane pane = levelSelector;
            pane.setScrollY((res + res / 8) * (selectedLevel.getNr()-1));
            if (lastCell != null) {
                lastCell.expandY().fillY();
            }
        }
        categorySelector.setScrollY((res + res / 8) * (activatedCategory-2));
    }

    private void leave() {
        //dispose(); // done from main
        main.fadeExit();
    }

    private void credits() {
        main.setScreen(new Credits(main, selectedLevel));
    }

    private void preferences() {
        if (currentDialog == null) dialogPreferences();
        else dialogClose();
    }

    private void save() {
        Play p = new Play(main, 1);
        if (p.getGame() != null) { // init was successful
            main.setScreen(p); // TODO: allow more slots?
        }
    }

    /**
     * the activated hotspot is executed (after space, return, or fire)
     */
    private void activateSelection() {
        int hint = hotSpotSelected.getActivateHint();
        if(hint>=1000) { // a level was selected, needs to be started
            hint -= 1000;
            hotSpotSelected = hotSpotRoot;
            selectLevel(levelsConfig.get(activatedCategory).get(hint));
        } else if (hint >= 100) { // a category was selected
            hint -= 100;
            switchLevelCategory(hint);
        } else {
            switch(hint) {
                case 1:
                    preferences();
                    if(currentDialog!=null) { // if menu was opened (should usually happen)
                        hotSpotSelected = hotSpotPreferencesRoot.getRight();
                    }
                    break;
                case 2:
                    save();
                    break;
                case 3:
                    Gdx.net.openURI(WEBSITE);
                    break;
                case 4:
                    credits();
                    break;
                case 5:
                    leave();
                    break;
                case 10:
                    toggleSound();
                    break;
                case 11:
                    toggleMusic();
                    break;
                case 12:
                    increaseSymbolSize();
                    hotSpotSelected = hotSpotPreferencesRoot.getRight().getRight().getRight();
                    break;
                case 13:
                    decreaseSymbolSize();
                    hotSpotSelected = hotSpotPreferencesRoot.getRight().getRight().getRight().getRight();
                    break;
                case 14:
                    changeLanguage();
                    hotSpotSelected = hotSpotPreferencesRoot.getRight().getRight().getRight().getRight().getRight();
                    break;
                case 15:
                    dialogClose();
                    hotSpotSelected = hotSpotRoot.getRight();
                    break;
            }
        }
    }

    private void moveCursor(int direction) {
        if(frames-lastFrames>30) {
            HotSpot newHS = hotSpotSelected;
            switch (direction) {
                case Mover.UP:
                    newHS = hotSpotSelected.getUp();
                    break;
                case Mover.RIGHT:
                    newHS = hotSpotSelected.getRight();
                    break;
                case Mover.DOWN:
                    newHS = hotSpotSelected.getDown();
                    break;
                case Mover.LEFT:
                    newHS = hotSpotSelected.getLeft();
                    break;
            }
            if (newHS != null) {
                hotSpotSelected = newHS;
                // scroll underlying scrollpane
                ScrollPane pane = hotSpotSelected.getScrollPane();
                if (pane != null) {
                    Actor a = hotSpotSelected.getActor();
                    pane.setScrollX(a.getX() - pane.getScrollWidth() / 2);
                    pane.setScrollY(pane.getMaxY() + pane.getScrollHeight() / 2 - a.getY());
                }
            }
            lastFrames = frames;
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (resumeRequested) {
            Play p = new Play(main, 0);
            if (p.getGame() != null) { //load successfull
                main.setScreen(p); // resume
            } else { //if not just continue with this screen
                resumeRequested = false;
            }
        } else {
            frames += 2;
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            if(!fader.isActive()) {
                stage.act(delta);
                evaluateDirections();
            }
            stage.draw();
//            batch.begin();
//            levelFont.draw(batch, main.getVersionString(), 0, preferences.getSymbolResolution() / 2, Gdx.graphics.getWidth(), 0, false);
//            batch.end();
            Actor a = hotSpotSelected.getActor();
            if(a != null) {
                int res = preferences.getSymbolResolution();
                batch.setColor(1, 1, 1, 1); // restore batch color
//                Vector2 coords = new Vector2(a.getX(),a.getY());
                coords.x = 0;
                coords.y = 0;
                a.localToStageCoordinates(/*in/out*/coords);
                //stage.stageToScreenCoordinates(/*in/out*/coords);
                batch.begin();
                batch.draw(Entities.destination.getTexture(res, frames), coords.x, coords.y);
                batch.end();
            }

            fader.render();
        }
    }

    public void resize() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        int res = preferences.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        bigLevelSkin = main.getLevelSkin(res);
        levelSkin = main.getLevelSkin(res / 2);
        textSkin = main.getMenuSkin(res / 2);

        rootTable.setSize(width, height);
        table.setBounds(0, 0, width, height);
        stage.getViewport().update(width, height, true);

        levelFont = main.getLevelFont(preferences.getSymbolResolution() / 2);
        rebuildMenu();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        mqttController.clearListener();
        for (Texture t : textureCache.values())
            t.dispose();
        stage.dispose();
//        batch.dispose(); in stage
        fader.dispose();
    }

    private void toggleFullscreen() {
        if (!fullscreen) {
            fullscreen = true;
            //DesktopLauncher.run(false);

/*            com.badlogic.gdx.Graphics.DisplayMode m = null;
            for(com.badlogic.gdx.Graphics.DisplayMode mode: Gdx.graphics.getDisplayModes()) {
                Gdx.app.log("mode",mode.toString());
                if(m == null) {
                    m = mode;
                } else {
                    if(m.width < mode.width) {
                        m = mode;
                    }
                }
            }
            Gdx.graphics.setDisplayMode(m); */
            //Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
//            Gdx.graphics.setDisplayMode(1920, 1080, true);
//            Gdx.graphics.setDisplayMode(1280, 720, true);
//            Gdx.graphics.setVSync(true);
            //TODO: setting here to fs does not work
        } else {
            fullscreen = false;
            Gdx.graphics.setWindowedMode(1280,900);
        }
        preferences.setFullScreen(fullscreen);
    }

    private SymbolButton soundButton;
    private SymbolButton musicButton;

    private void dialogPreferences() {
        int res = preferences.getSymbolResolution();
        int padSize = res / 16;
        Skin menuSkin = main.getMenuSkin(res);
        Table thisDialog = new Table();
        thisDialog.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
        thisDialog.setColor(new Color(1, 1, 1, 0.9f)); // little transparent
        thisDialog.setSize(Math.min(Gdx.graphics.getWidth(), 6*res + 9 * padSize),
                Math.min(Gdx.graphics.getHeight(), 1*res + 4*padSize) );
        thisDialog.setPosition(0, Gdx.graphics.getHeight() - thisDialog.getHeight() - res);

        // Basic layout
        Table rowGamePrefsTable = new Table(menuSkin);
        rowGamePrefsTable.setHeight(res);
        ScrollPane rowGamePrefs = new ScrollPane(rowGamePrefsTable);
        hotSpotPreferencesRoot = new HotSpot(null,null,-1);

        thisDialog.add(rowGamePrefs).expandX().fillX().pad(padSize).top().minHeight(res).row();

        ///// Fill game prefs row
        soundButton = new SymbolButton(res,
                audio.getSound() ?
                Entities.menu_symbol_sound_on.getTexture(res, 0)
                : Entities.menu_symbol_sound_off.getTexture(res, 0));
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                toggleSound();
            }
        });
        rowGamePrefsTable.add(soundButton.getCell()).prefSize(res, res).padRight(padSize);
        HotSpot hs = hotSpotPreferencesRoot.getCreateRight(soundButton.getCell(),rowGamePrefs,10);
        hotSpotPreferencesRoot.setUp(hs);
        hotSpotPreferencesRoot.setDown(hs);
        hotSpotPreferencesRoot.setLeft(hs);

        musicButton = new SymbolButton(res,
                audio.getMusic() ?
                Entities.menu_symbol_music_on.getTexture(res, 0)
                : Entities.menu_symbol_music_off.getTexture(res, 0));
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                toggleMusic();
            }
        });
        rowGamePrefsTable.add(musicButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(musicButton.getCell(),rowGamePrefs,11);

        SymbolButton symbolPlusButton = new SymbolButton(res, Entities.menu_symbol_toolbar_zoom_in.getTexture(res, 0));
        symbolPlusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                increaseSymbolSize();
            }
        });
        rowGamePrefsTable.add(symbolPlusButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(symbolPlusButton.getCell(),rowGamePrefs,12);

        SymbolButton symbolMinusButton = new SymbolButton(res,
                Entities.menu_symbol_toolbar_zoom_out.getTexture(res, 0));
        symbolMinusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                decreaseSymbolSize();
            }
        });
        rowGamePrefsTable.add(symbolMinusButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(symbolMinusButton.getCell(),rowGamePrefs,13);

        SymbolButton langButton = new SymbolButton(res,preferences.languageGfx().getTexture(res,0));
        langButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                changeLanguage();
            }
        });
        rowGamePrefsTable.add(langButton.getCell()).prefSize(res, res).padRight(padSize);
        hs = hs.getCreateRight(langButton.getCell(),rowGamePrefs,14);

        SymbolButton closeButton = new SymbolButton(res, Entities.toolbox_abort.getTexture(res, 0));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hotSpotSelected = hotSpotRoot;
                dialogClose();
            }
        });
        rowGamePrefsTable.add(closeButton.getCell()).prefSize(res, res).padRight(padSize);
        hs.getCreateRight(closeButton.getCell(),rowGamePrefs,15);

        stage.addActor(thisDialog);
        currentDialog = thisDialog;
    }

    private void changeLanguage() {
        dialogClose();
        preferences.nextLanguage();
        rebuildMenu(); // TODO: jump to current level
        dialogPreferences();
    }

    private void decreaseSymbolSize() {
        dialogClose();
        decreaseSymbolResolution();
        dialogPreferences();
    }

    private void increaseSymbolSize() {
        dialogClose();
        increaseSymbolResolution();
        dialogPreferences();
    }

    private void toggleMusic() {
        int res = preferences.getSymbolResolution();
        preferences.toggleMusic();
        musicButton.setSymbol(res, preferences.getMusic() ?
                Entities.menu_symbol_music_on.getTexture(res, 0)
                : Entities.menu_symbol_music_off.getTexture(res, 0));
    }

    private void toggleSound() {
        int res = preferences.getSymbolResolution();
        preferences.toggleSound();
        soundButton.setSymbol(res,
                preferences.getSound() ?
                        Entities.menu_symbol_sound_on.getTexture(res, 0)
                        : Entities.menu_symbol_sound_off.getTexture(res, 0));
    }

    private void dialogClose() {
        if(currentDialog != null) {
            currentDialog.remove();
            currentDialog = null;
        }
    }

    public void setSymbolResolution(int symbolResolution) {
        preferences.setSymbolResolution(symbolResolution);
        resize();
    }

    public void increaseSymbolResolution() {
        setSymbolResolution(preferences.getSymbolResolution()*2);
    }

    public void decreaseSymbolResolution() {
        setSymbolResolution(preferences.getSymbolResolution() / 2);
    }

    public void updateDirections() {
        keyDirections = Util.getKeyDirections(mqttController);
    }

    public void evaluateDirections() {
        if(keyDirections>0) {
            if((keyDirections&Mover.UP)>0)
                moveCursor(Mover.UP);
            else if((keyDirections&Mover.RIGHT)>0)
                moveCursor(Mover.RIGHT);
            else if((keyDirections&Mover.DOWN)>0)
                moveCursor(Mover.DOWN);
            else if((keyDirections&Mover.LEFT)>0)
                moveCursor(Mover.LEFT);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        updateDirections();
        //return super.keyDown(event, keycode);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        switch (character) {
            case 'F': // only capital:
                toggleFullscreen();
                return true;
            case '9':
                ScreenshotFactory.saveScreenshot();
                return true;
            case ' ':
            case 13:
                activateSelection();
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode) {
            case 23: //amazon fire remote select
                activateSelection();
                return true;
            case 82: // amazon fire menu
                if(currentDialog==null) {
                    dialogPreferences();
                    hotSpotSelected = hotSpotPreferencesRoot.getRight();
                }
                else {
                    dialogClose();
                    hotSpotSelected = hotSpotRoot.getRight();
                }
                return true;
            case 85: // amazon fire play/pause
                activateSelection();
                return true;
            case 89: // amazon fire wind back
                break;
            case 90: // amazon fire wind forward
                break;
            default:
                updateDirections();
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}
