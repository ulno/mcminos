package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 20.12.15.
 */
public class Congrats implements Screen {
    private final Main main;
    private final LevelCategory category;
    private final LevelConfig levelConfig;
    private final Stage stage;
    private final Statistics statistics;
    private final SpriteBatch batch;
    private final Audio audio;
    private final Preferences preferences;
    private final Fader fader;
    private boolean finished = false;

    public Congrats(Main main, LevelConfig currentLevelConfig) {
        this.main = main;
        this.audio = main.getAudio();
        this.preferences = main.getPreferences();
        this.category = currentLevelConfig.getCategory();
        this.levelConfig = currentLevelConfig;
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage); // set inputprocessor
        statistics = main.getStatistics();
        fader = new Fader(main,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        fader.fadeOutInMusicFixed(1);

        rebuild();
    }

    public void rebuild() {
        stage.clear(); // start clear

        TextureRegion bg = Entities.backgrounds_amoeboid_01.getTexture(128, 0); // can be fixed as bg is not so critical

        // root table covering the screen
        Table rootTable = new Table();
        rootTable.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        rootTable.setPosition(0, 0);

        stage.addActor(rootTable);

        int res = preferences.getSymbolResolution();

        // table for menu
        Table table = new Table();
        ScrollPane scroller = new ScrollPane(table);
        rootTable.add(scroller).top().center().fill().expand();

        rootTable.setBackground(new BackroundDrawer(bg));

        // Congrat-message
        Label m = new Label(category.getEndMessage(preferences.getLanguage()),main.getLevelSkin(res/2));
        m.setWrap(true);
        table.add(m).maxWidth(Gdx.graphics.getWidth()*9/10).fill().expand();

        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                finished = true;
            }
        });

        stage.addListener(new InputListener() {

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                finished = true;
                return true;
            }

        });

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
        if(fader.isActive()) {
            fader.render();
        } else {
            stage.act();
        }
        if(finished) {
            dispose();
            main.activateMainMenu( levelConfig );
        }
    }

    @Override
    public void resize(int width, int height) {
        rebuild();
        fader.resize(width,height);
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
        stage.dispose();
         // batch.dispose(); in stage
        fader.dispose();
    }
}
