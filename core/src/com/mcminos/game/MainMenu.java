package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenu implements Screen {
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


    public MainMenu(final Main main) {
//        final MainMenu thisScreen = this; // TODO: check why we need this
        this.main = main;
        this.preferences = main.getPreferences();
        this.fullscreen = preferences.getFullScreen();
        this.audio = main.getAudio();
        this.statistics = main.getStatistics();
        batch = new SpriteBatch();
        levelsConfig = main.getLevelsConfig();
        bg = Entities.backgrounds_amoeboid_01.getTexture(128, 0); // can be fixed as bg is not so critical

        stage = new Stage(new ScreenViewport(), batch);

        fader = new Fader(main);

        // root table covering the screen
        rootTable = new Table();
        stage.addActor(rootTable);
        rootTable.setPosition(0, 0);

        // table for menu
        table = new Table();
        rootTable.add(table).top().center().fill().expand();

        rootTable.setBackground(new BackroundDrawer(bg));

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

    private void switchLevelCategory(int category, Label categoryLabel, Table twoColumns, ScrollPane folderSelector, SymbolButton[] categorySelectorButtons, ScrollPane levelSelector, int res) {
        categoryLabel.setText(levelsConfig.get(category).getName());
        categorySelectorButtons[activatedCategory].unselect();
        activatedCategory = category;
        categorySelectorButtons[activatedCategory].select();
        // selectedLevel = null; //deselect -- not necessary as it can stay selected
        twoColumns.clear();
        twoColumns.add(folderSelector).fillY().expandY().minWidth(res).prefWidth(res).padRight(res / 2);
        twoColumns.add(levelSelector).fill().expand();
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
        fader.fadeIn();
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
            switchLevelCategory(category, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector, resolution);
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
        Table topRow = new Table();

        SymbolButton settingsButton = new SymbolButton(res, Entities.menu_symbol_settings.getTexture(res, 0));
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(currentDialog == null) dialogPreferences();
                else dialogClose();
            }
        });
        topRow.add(settingsButton.getCell()).left().minHeight(res).padRight(res / 16);

        SymbolButton loadButton = new SymbolButton(res,Entities.menu_symbol_game_load.getTexture(res, 0));
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Play p = new Play(main, 1);
                if (p.getGame() != null) { // init was successful
                    main.setScreen(p); // TODO: allow more slots?
                }
            }
        });
        topRow.add(loadButton.getCell()).left().minHeight(res);

        Label title = new Label("McMinos", bigLevelSkin); // TODO: replace with mcminos logo graphics
        title.setAlignment(Align.center); // TODO: add left shift to position to compensate number of buttons
        topRow.add(title).prefHeight(res).fillX().expandX();

        SymbolButton infoButton = new SymbolButton(res,Entities.menu_button_info.getTexture(res, 0));
        infoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new Credits(main, selectedLevel));
            }
        });
        topRow.add(infoButton.getCell()).right().minHeight(res);

        SymbolButton quitButton = new SymbolButton(res,Entities.menu_button_exit_variant.getTexture(res, 0));
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                main.exit();
            }
        });
        topRow.add(quitButton.getCell()).right().minHeight(res);


        Label categoryLabel = new Label(levelsConfig.get(activatedCategory).getName(), levelSkin);

        // the scrollpanes with the respective levels for each category
        ScrollPane[] levelSelector = new ScrollPane[levelsConfig.size()];
        Cell<Group> lastCell = null;
        for (int c = 0; c < levelsConfig.size(); c++) {
            LevelCategory cat = levelsConfig.get(c);

            Table levelSelectorTable = new Table();
            levelSelector[c] = new ScrollPane(levelSelectorTable);
            levelSelector[c].setForceScroll(false, true);

            for (int i = 0; i < cat.size(); i++) {
                LevelConfig lc = cat.get(i);
                Group levelRowGroup = new Group();
                levelRowGroup.setHeight(res + res / 8);
                Table levelRow = new Table(bigLevelSkin);
                levelRow.setPosition(0, res / 2 + res / 8); // TODO: figure out why this shift is necessary -> bug in libgdx?
                levelRowGroup.addActor(levelRow);
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
                t =  new Label((i + 1) + ". " + lc.getTitle(language), levelSkin);
                if (selectedLevel != null && c == activatedCategory && i == selectedLevel.getNr()) {
                    t =  new Label((i + 1) + ". >" + lc.getTitle(language), levelSkin);
                    Group indicator = new Group();
                    indicator.setSize(res, res);
                    Image indicatorImage = new Image(Entities.mcminos_default_left.getTextureDirectStep(res, 7));
                    indicator.setSize(res, res);
                    indicator.addActor(indicatorImage);
                    levelRow.add(t).prefHeight(res).top().left();
                    levelRow.add(indicator).top().left().fillX().expandX();
                    // check why we can't set a background? - doesn't matter showing the symbol looks nice
                } else {
                    t =  new Label((i + 1) + ". " + lc.getTitle(language), levelSkin);
                    levelRow.add(t).prefHeight(res).top().left().fillX().expandX();
                }

                lastCell = levelSelectorTable.add(levelRowGroup).prefHeight(res).top().left().padBottom(res / 16).fillX().expandX();
                levelSelectorTable.row();
                if(statistics.activated(lc)) {
                    levelRow.addListener(new LevelClickListener(levelsConfig.get(c).get(i)));
                } else {
                    // t.setColor(0.5f,0.5f,0.5f,1.0f); does not work as this is a colored font
                    levelRowGroup.setColor(1,1,1,0.6f); // sets only transparency
                }
                // mark last active level
            }
            lastCell.expandY().fillY();
        }

        // add the buttons for the different level categories to the toolbar
        Table categorySelectorTable = new Table();
        ScrollPane categorySelector = new ScrollPane(categorySelectorTable);
        SymbolButton categorySelectorButtons[] = new SymbolButton[levelsConfig.size()];
        Cell<Group> lastCategory = null;
        for (int i = 0; i < levelsConfig.size(); i++) { // loop through categories
            SymbolButton b = new SymbolButton(res, levelsConfig.get(i).getTexture(res));
            CategoryClickListener listener = new CategoryClickListener(i, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector[i], res);
            b.addListener(listener);
            categorySelectorButtons[i] = b;

            lastCategory = categorySelectorTable.add(b.getCell()).top().left().prefSize(res).padBottom(res / 16);
            categorySelectorTable.row();
        }
        lastCategory.fillY().expandY();

/*        if(versionStringActor != null) {
            versionStringActor.remove();
        }
        versionStringActor = new Label(main.getVersionString(), levelSkin);
        versionStringActor.setPosition(0,0, Align.bottomRight);
//        versionStringActor.setColor(1,1,1,0.5f);
        rootTable.addActor(versionStringActor); */

        stage.addListener(new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                switch (character) {
                    case 'F': // only capital:
                        toggleFullscreen();
                        break;
                }
                return false;
            }

        });
        Gdx.input.setInputProcessor(stage);
// called from there        resize();

        // Layout
        table.add(topRow).prefHeight(res).minHeight(res).top().fillX().expandX().padBottom(res / 8).row();
        table.add(categoryLabel).minHeight(res).prefHeight(res).padBottom(res / 16).padLeft(res+res/2).left().row();
        switchLevelCategory(activatedCategory, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector[activatedCategory], res);
        table.add(twoColumns).fill().expand();

        //table.pack(); // fit table into root-table to update coordinates
        table.layout(); // force layout to have coordinates and dimensions in pane
        // scroll pane to activated level
        if(selectedLevel != null) {
            ScrollPane pane = levelSelector[selectedLevel.getCategory().getNr()];
            pane.setScrollY((res + res / 8) * selectedLevel.getNr());
            if (lastCell != null) {
                lastCell.expandY().fillY();
            }
        }

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (resumeRequested) {
            Play p = new Play(main, 0);
            if (p.getGame() != null) { //load successfull
                main.setScreen(p); // resume
            } else { //if not just continue with this screen
                resumeRequested = false;
            }
        } else {
            if(!fader.isActive()) stage.act(delta);
            stage.draw();
            batch.begin();
            levelFont.draw(batch, main.getVersionString(), 0, preferences.getSymbolResolution() / 2, Gdx.graphics.getWidth(), 0, false);
            batch.end();
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

    public void restoreInputProcessor() {
        Gdx.input.setInputProcessor(stage); // restore inputprocessor

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
        for (Texture t : textureCache.values())
            t.dispose();
        stage.dispose();
//        batch.dispose(); akready disposed? TODO: check
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
        thisDialog.setPosition( 0, Gdx.graphics.getHeight() - thisDialog.getHeight() - res );

        // Basic layout
        Table rowGamePrefsTable = new Table(menuSkin);
        rowGamePrefsTable.setHeight(res);
        ScrollPane rowGamePrefs = new ScrollPane(rowGamePrefsTable);
        
        thisDialog.add(rowGamePrefs).expandX().fillX().pad(padSize).top().minHeight(res).row();

        ///// Fill game prefs row
        soundButton = new SymbolButton(res,
                audio.getSound() ?
                Entities.menu_symbol_sound_on.getTexture(res, 0)
                : Entities.menu_symbol_sound_off.getTexture(res, 0));
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int res = preferences.getSymbolResolution();
                preferences.toggleSound();
                soundButton.setSymbol(res,
                        preferences.getSound() ?
                        Entities.menu_symbol_sound_on.getTexture(res, 0)
                        : Entities.menu_symbol_sound_off.getTexture(res, 0));
            }
        });
        rowGamePrefsTable.add(soundButton.getCell()).prefSize(res, res).padRight(padSize);

        musicButton = new SymbolButton(res,
                audio.getMusic() ?
                Entities.menu_symbol_music_on.getTexture(res, 0)
                : Entities.menu_symbol_music_off.getTexture(res, 0));
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int res = preferences.getSymbolResolution();
                preferences.toggleMusic();
                musicButton.setSymbol(res,preferences.getMusic() ?
                        Entities.menu_symbol_music_on.getTexture(res, 0)
                        : Entities.menu_symbol_music_off.getTexture(res, 0));
            }
        });
        rowGamePrefsTable.add(musicButton.getCell()).prefSize(res, res).padRight(padSize);

        SymbolButton symbolPlusButton = new SymbolButton(res, Entities.menu_symbol_toolbar_zoom_in.getTexture(res, 0));
        symbolPlusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogClose();
                increaseSymbolResolution();
                dialogPreferences();
            }
        });
        rowGamePrefsTable.add(symbolPlusButton.getCell()).prefSize(res, res).padRight(padSize);

        SymbolButton symbolMinusButton = new SymbolButton(res,
                Entities.menu_symbol_toolbar_zoom_out.getTexture(res, 0));
        symbolMinusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogClose();
                decreaseSymbolResolution();
                dialogPreferences();
            }
        });
        rowGamePrefsTable.add(symbolMinusButton.getCell()).prefSize(res, res).padRight(padSize);

        SymbolButton langButton = new SymbolButton(res,preferences.languageGfx().getTexture(res,0));
        langButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogClose();
                preferences.nextLanguage();
                rebuildMenu(); // TODO: jump to current level
                dialogPreferences();
            }
        });
        rowGamePrefsTable.add(langButton.getCell()).prefSize(res, res).padRight(padSize);


        SymbolButton closeButton = new SymbolButton(res, Entities.toolbox_abort.getTexture(res, 0));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogClose();
            }
        });
        rowGamePrefsTable.add(closeButton.getCell()).prefSize(res, res).padRight(padSize);

        stage.addActor(thisDialog);
        currentDialog = thisDialog;
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
        setSymbolResolution(preferences.getSymbolResolution()/2);
    }

}
