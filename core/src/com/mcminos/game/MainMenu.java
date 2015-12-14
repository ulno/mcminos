package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenu implements Screen {
    private final LevelsConfig levelsConfig;
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

    private ArrayList<Texture> categoryButtonImages = new ArrayList();

    private boolean fullscreen = Game.preferencesHandle.getBoolean("fs");
    private int levelCategory;
    private LevelConfig activatedLevel = null; // nothing selected in the beginning
    private String language = "en";
    private BitmapFont levelFont;
    private HashMap<String, Texture> textureCache = new HashMap<>();


    public MainMenu(final Main main, LevelConfig levelPreselect) {
        final MainMenu thisScreen = this; // TODO: check why we need this
        this.main = main;
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

        rootTable.setBackground(new Drawable() {
            @Override
            public void draw(Batch batch, float x0f, float y0f, float widthf, float heightf) {
                int x0 = (int) x0f;
                int y0 = (int) y0f;
                int width = (int) widthf;
                int height = (int) heightf;

                for (int x = x0; x < x0 + width; x += bg.getRegionWidth())
                    for (int y = y0; y < 0 + height; y += bg.getRegionHeight())
                        batch.draw(bg, x, y);
            }

            @Override
            public float getLeftWidth() {
                return 0;
            }

            @Override
            public void setLeftWidth(float leftWidth) {

            }

            @Override
            public float getRightWidth() {
                return 0;
            }

            @Override
            public void setRightWidth(float rightWidth) {

            }

            @Override
            public float getTopHeight() {
                return 0;
            }

            @Override
            public void setTopHeight(float topHeight) {

            }

            @Override
            public float getBottomHeight() {
                return 0;
            }

            @Override
            public void setBottomHeight(float bottomHeight) {

            }

            @Override
            public float getMinWidth() {
                return 0;
            }

            @Override
            public void setMinWidth(float minWidth) {

            }

            @Override
            public float getMinHeight() {
                return 0;
            }

            @Override
            public void setMinHeight(float minHeight) {

            }
        });
        // read images for the category buttons
        for (int i = 0; i < levelsConfig.size(); i++) {
            String path = levelsConfig.get(i).getPath();
            FileHandle fh = Gdx.files.internal("levels/" + path + "/gfx.png");
            if (fh.exists()) {
                categoryButtonImages.add(new Texture(fh));
            } else {
                categoryButtonImages.add(null);
            }
        }

        initMenu();

        // eventually continue a paused/suspended game
        if (Game.getSaveFileHandle(0).exists()) {
            resumeRequested = true;
        }
    }

    private void switchLevelCategory(int catecory) {
        levelCategory = catecory;
//        selectLevel(-1); //deselect level
    }

    private void selectLevel(int level) {
        LevelCategory levelCategory = levelsConfig.get(this.levelCategory);
        LevelConfig lc = levelCategory.get(level);
        activatedLevel = lc;
        this.dispose();
        main.setScreen(new Play(main, lc, 0, 3));
    }

    // inner class for menu
    class CategoryClickListener extends ClickListener {

        public int category;

        CategoryClickListener(int c) {
            category = c;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            switchLevelCategory(category);
            initMenu();
        }
    }

    // inner class for menu
    class LevelClickListener extends ClickListener {

        public int level;

        LevelClickListener(int level) {
            this.level = level;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            selectLevel(level);
        }
    }

    private void initMenu() {
        int res = main.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        textSkin = main.getMenuSkin(res / 2);
        levelSkin = main.getLevelSkin(res / 2);
        bigLevelSkin = main.getLevelSkin(res);

        table.clear();

        Table topRow = new Table();
        table.add(topRow).prefHeight(res).minHeight(res).top().fillX().expandX().padBottom(res / 8).row();

        Image settingsButton = new Image(Entities.menu_button_settings.getTexture(res,0));
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: call/activate preferences menu
            }
        });
        topRow.add(settingsButton).left().minHeight(res).padRight(res/16);

        Image loadButton = new Image(Entities.menu_button_game_load.getTexture(res,0));
        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                main.setScreen(new Play(main,1)); // TODO: allow more slots?
            }
        });
        topRow.add(loadButton).left().minHeight(res);

        Label title = new Label("McMinos", bigLevelSkin); // TODO: replace with old white graphics
        title.setAlignment(Align.center); // TODO: add left shift to position to compensate number of buttons
        topRow.add(title).prefHeight(res).fillX().expandX();

        Image quitButton = new Image(Entities.menu_button_stop.getTexture(res,0));
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        topRow.add(quitButton).right().minHeight(res);


        Label l = new Label(levelsConfig.get(levelCategory).getName(), levelSkin);
        table.add(l).prefHeight(res + 4).padBottom(res / 16).left().row();

        Table folderSelectorTable = new Table();

        // add the buttons for the different level categories to the toolbar
        Cell<Group> lastCategory = null;
        for (int i = 0; i < levelsConfig.size(); i++) {
            Group g = new Group();
            g.setHeight(res);
            Button b = new Button(bigMenuSkin);
            if (i == levelCategory) {
                b.setColor(1.0f, 0, 0, 1.0f);
            }
            if (categoryButtonImages.get(i) != null) {
                Image img = new Image(categoryButtonImages.get(i));
                img.setSize(res, res);
                b.add(img);
            }
            b.setSize(res, res);
            g.addActor(b);
            b.addListener(new CategoryClickListener(i));
            lastCategory = folderSelectorTable.add(g).top().left().prefSize(res).padBottom(res / 16);
            folderSelectorTable.row();
        }
        lastCategory.fillY().expandY();
        ScrollPane folderSelector = new ScrollPane(folderSelectorTable);

        Table twoColumns = new Table();


        table.add(twoColumns).fill().expand().row();

        Table levelSelectorTable = new Table();
        ScrollPane levelSelector = new ScrollPane(levelSelectorTable);
        levelSelector.setForceScroll(false, true);

        LevelCategory categoryLevels = levelsConfig.get(levelCategory);
        Cell<Group> lastCell = null;
        for (int i = 0; i < categoryLevels.size(); i++) {
            LevelConfig lc = categoryLevels.get(i);
            Group levelRowGroup = new Group();
            levelRowGroup.setHeight(res+res/8);
            Table levelRow = new Table();
            levelRow.setPosition(0,res/2+res/8); // TODO: figure out why this shift is necessary -> bug in libgdx?
            levelRowGroup.addActor(levelRow);
            Group thumbnail = new Group();
            thumbnail.setSize(res, res);
            String levelThumbNailName = "levels/" + levelsConfig.get(levelCategory).getPath() + "/" + lc.getId() + ".png";
            Texture texture = null;
            if (textureCache.containsKey(levelThumbNailName)) {
                texture = textureCache.get(levelThumbNailName);
            } else {
                FileHandle fh = Gdx.files.internal(levelThumbNailName);
                if (fh.exists()) {
                    texture = new Texture(fh);
                    textureCache.put(levelThumbNailName, texture);
                }
            }
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
            levelRow.addListener(new LevelClickListener(i));
        }
        if (lastCell != null) {
            lastCell.expandY().fillY();
        }
        twoColumns.add(folderSelector).fillY().expandY().minWidth(res).prefWidth(res).padRight(res / 2);
        twoColumns.add(levelSelector).fill().expand();


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
        resize();
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
                dispose();
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
