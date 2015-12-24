package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
//    private Label versionStringActor;

    private boolean fullscreen = Game.preferencesHandle.getBoolean("fs");
    private LevelConfig activatedLevel = null; // nothing selected in the beginning
    private int activatedCategory = 0; // first is selected by default
    private String language = "en";
    private BitmapFont levelFont;
    private HashMap<String, Texture> textureCache = new HashMap<>();


    public MainMenu(final Main main) {
//        final MainMenu thisScreen = this; // TODO: check why we need this
        this.main = main;
        this.audio = main.getAudio();
        this.statistics = main.getUserStats();
        batch = new SpriteBatch();
        levelsConfig = main.getLevelsConfig();
        bg = Entities.backgrounds_amoeboid_01.getTexture(128, 0); // can be fixed as bg is not so critical

        stage = new Stage(new ScreenViewport(), batch);

        // root table covering the screen
        rootTable = new Table();
        stage.addActor(rootTable);
        rootTable.setPosition(0, 0);

        // table for menu
        table = new Table();
        rootTable.add(table).top().center().fill().expand();

        rootTable.setBackground(new BackroundDrawer(bg));

// happens in resize        rebuildMenu();

        // eventually continue a paused/suspended game
        if (Game.getSaveFileHandle(0).exists()) {
            resumeRequested = true;
        }
    }

    private void switchLevelCategory(int category, Label categoryLabel, Table twoColumns, ScrollPane folderSelector, Button[] categorySelectorButtons, ScrollPane levelSelector, int res) {
        categoryLabel.setText(levelsConfig.get(category).getName());
        categorySelectorButtons[activatedCategory].setColor(1,1,1,1);
        activatedCategory = category;
        categorySelectorButtons[activatedCategory].setColor(0.5f,0,0,1);
        activatedLevel = null; //deselect
        twoColumns.clear();
        twoColumns.add(folderSelector).fillY().expandY().minWidth(res).prefWidth(res).padRight(res / 2);
        twoColumns.add(levelSelector).fill().expand();
    }

    private void selectLevel(LevelConfig level) {
        activatedLevel = level;
        main.setScreen(new Play(main, level, 0, 3));
    }

    public void activateLevel(LevelConfig currentLevel) {
        this.activatedLevel = currentLevel;
        if (activatedLevel != null) {
            activatedCategory = activatedLevel.getCategoryNr();
        }
    }

    public void init() {
        audio.musicFixed(0);
    }

    // inner class for menu
    class CategoryClickListener extends ClickListener {

        private final Table twoColumns;
        private final ScrollPane categorySelector;
        private final ScrollPane levelSelector;
        private final int resolution;
        private final Button[] categorySelectorButtons;
        private final Label categoryLabel;
        public int category;

        public CategoryClickListener(int c, Label categoryLabel, Table twoColumns, ScrollPane categorySelector, Button[] buttons, ScrollPane levelSelector, int res) {
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
        int res = main.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        textSkin = main.getMenuSkin(res / 2);
        levelSkin = main.getLevelSkin(res / 2);
        bigLevelSkin = main.getLevelSkin(res);

        // initialize
        table.clear();
        Table twoColumns = new Table();

        // first build only elements, layout later
        Table topRow = new Table();

        Image settingsButton = new Image(Entities.menu_button_settings.getTexture(res, 0));
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: call/activate preferences menu
            }
        });
        topRow.add(settingsButton).left().minHeight(res).padRight(res / 16);

        Image loadButton = new Image(Entities.menu_button_game_load.getTexture(res, 0));
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Play p = new Play(main, 1);
                if (p.getGame() != null) { // init was successful
                    main.setScreen(p); // TODO: allow more slots?
                }
            }
        });
        topRow.add(loadButton).left().minHeight(res);

        Label title = new Label("McMinos", bigLevelSkin); // TODO: replace with old white graphics
        title.setAlignment(Align.center); // TODO: add left shift to position to compensate number of buttons
        topRow.add(title).prefHeight(res).fillX().expandX();

        Image infoButton = new Image(Entities.menu_button_info.getTexture(res, 0));
        infoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: show credits
            }
        });
        topRow.add(infoButton).right().minHeight(res);

        Image quitButton = new Image(Entities.menu_button_exit_variant.getTexture(res, 0));
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        topRow.add(quitButton).right().minHeight(res);


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
                Label t = new Label((i + 1) + ". " + lc.getTitle(language), levelSkin);
                levelRow.add(thumbnail).prefHeight(res).top().left().padRight(res / 4);
                levelRow.add(t).prefHeight(res).top().left().fillX().expandX();
                lastCell = levelSelectorTable.add(levelRowGroup).prefHeight(res).top().left().padBottom(res / 16).fillX().expandX();
                levelSelectorTable.row();
                if(statistics.activated(lc)) {
                    levelRow.addListener(new LevelClickListener(levelsConfig.get(c).get(i)));
                } else {
                    // t.setColor(0.5f,0.5f,0.5f,1.0f); does not work as this is a colored font
                    levelRowGroup.setColor(0.5f,0.5f,0.5f,0.7f); // sets only transparency
                }
                if (activatedLevel != null) {
                    if (i == activatedLevel.getNr()) {
                        //levelRow.setBackground(new NinePatchDrawable(bigMenuSkin.getPatch("default-rect"))); // TODO: check for memory leak here
                        levelRow.background("default-rect");
                        levelRowGroup.setColor(0, 1, 0, 0.7f); // sets only transparency
                        //TODO: check why we can't set a background?
                    }
                }
            }
            if (lastCell != null) {
                lastCell.expandY().fillY();
            }
        }

        // add the buttons for the different level categories to the toolbar
        Table categorySelectorTable = new Table();
        ScrollPane categorySelector = new ScrollPane(categorySelectorTable);
        Button categorySelectorButtons[] = new Button[levelsConfig.size()];
        Cell<Group> lastCategory = null;
        for (int i = 0; i < levelsConfig.size(); i++) { // loop through categories
            Group g = new Group();
            g.setHeight(res);
            Button b = new Button(bigMenuSkin);
            categorySelectorButtons[i] = b;
            TextureRegion t = levelsConfig.get(i).getTexture(res);
            if (t != null) {
                Image img = new Image(t);
                img.setSize(res, res);
                b.add(img);
            }
            b.setSize(res, res);
            g.addActor(b);
            b.addListener(new CategoryClickListener(i, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector[i], res));
            lastCategory = categorySelectorTable.add(g).top().left().prefSize(res).padBottom(res / 16);
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
        table.add(categoryLabel).minHeight(res).prefHeight(res).padBottom(res / 16).left().row();
        switchLevelCategory(activatedCategory, categoryLabel, twoColumns, categorySelector, categorySelectorButtons, levelSelector[activatedCategory], res);
        table.add(twoColumns).fill().expand();

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
            stage.act(delta);
            stage.draw();
            batch.begin();
            levelFont.draw(batch, main.getVersionString(), 0, main.getSymbolResolution() / 2, Gdx.graphics.getWidth(), 0, false);
            batch.end();

        }
    }

    public void resize() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        int res = main.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        bigLevelSkin = main.getLevelSkin(res);
        levelSkin = main.getLevelSkin(res / 2);
        textSkin = main.getMenuSkin(res / 2);

        rootTable.setSize(width, height);
        table.setBounds(0, 0, width, height);
        stage.getViewport().update(width, height, true);
        levelFont = main.getLevelFont(main.getSymbolResolution() / 2);
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
        batch.dispose();
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
            Gdx.graphics.setDisplayMode(1280, 900, false);
        }
        Game.preferencesHandle.putBoolean("fs", fullscreen);
        Game.preferencesHandle.flush();
    }
}
