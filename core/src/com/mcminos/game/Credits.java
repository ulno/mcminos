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

import java.io.BufferedReader;

/**
 * Created by ulno on 24.12.15.
 */
public class Credits implements Screen {
    private final Main main;
    private final Stage stage;
    private final SpriteBatch batch;
    private final Audio audio;
    private final LevelConfig levelConfig;
    private String credits = "Credits will be here and scrolling.\n\nUlNo+Nope";
    private ScrollPane scroller;
    private long realTime = 0;
    private long realTimePassed = 0;
    private long lastDeltaTimeLeft = 0;

    public Credits(Main main, LevelConfig levelConfig) {
        this.main = main;
        this.audio = main.getAudio();
        this.levelConfig = levelConfig;
        audio.musicFixed(2);
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage); // set inputprocessor

        // read credits for current language TODO: language
        BufferedReader br = new BufferedReader(Gdx.files.internal(Main.TEXT_FILE).reader());
        for(KeyValue kv = new KeyValue(br); kv.key!=null; kv = new KeyValue(br)) {
            if(kv.key.equals("credits")) {
                credits = kv.value;
            }
        }
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

        int res = main.getSymbolResolution()/2;

        // table for menu
        Table table = new Table();
        scroller = new ScrollPane(table);
        rootTable.add(scroller).top().center().fill().expand();

        rootTable.setBackground(new BackroundDrawer(bg));

        // Congrat-message
        Label m = new Label(credits,main.getLevelSkin(res));
        m.setWrap(true);
        table.add(m).maxWidth(Gdx.graphics.getWidth()*9/10).fill().expand();

        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.activateMainMenu( levelConfig );
            }
        });

        stage.addListener(new InputListener() {

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                main.activateMainMenu( levelConfig );
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

        float current = scroller.getScrollY();
        float scrollmax = scroller.getMaxY();
        if(current < scrollmax) {
            float gdxtime = Gdx.graphics.getDeltaTime();
            realTime = (long) (gdxtime * 1000);

            long deltaTime = (long) (gdxtime * Game.timeResolutionSquare); // needs to have long in front as gdxtime is float (don't apply long directly to gdxtime)
            deltaTime += lastDeltaTimeLeft; // apply what is left
            long step = deltaTime / Game.timeResolution;
            deltaTime -= step * Game.timeResolution;
            lastDeltaTimeLeft = deltaTime;
            if (realTimePassed > Game.timeResolutionSquare * 5) {
                current += (float) step * 16 / main.getSymbolResolution();
                scroller.setScrollY(current);
            } else {
                realTimePassed += step * Game.timeResolution;
            }
        }
        stage.draw();
        stage.act();
    }

    @Override
    public void resize(int width, int height) {
        rebuild();
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

    }
}