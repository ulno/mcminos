package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
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
public class Credits implements Screen, ControllerListener {
    private final Main main;
    private final Stage stage;
    private final SpriteBatch batch;
    private final Audio audio;
    private final LevelConfig levelConfig;
    private final Preferences preferences;
    private final Fader fader;
    private String credits = "Credits will be here and scrolling.\n\nUlNo+Nope";
    private ScrollPane scroller;
    private long realTime = 0;
    private long realTimePassed = 0;
    private long lastDeltaTimeLeft = 0;
    private boolean finished = false;

    public Credits(Main main, LevelConfig levelConfig) {
        this.main = main;
        this.preferences = main.getPreferences();
        this.audio = main.getAudio();
        this.levelConfig = levelConfig;
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);
        fader = new Fader(main,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        fader.fadeOutInMusicFixed(2);

        Gdx.input.setInputProcessor(stage); // set inputprocessor
        Controllers.clearListeners();
        Controllers.addListener(this);

        // read credits for current language TODO: language
        BufferedReader br = new BufferedReader(Gdx.files.internal(Main.TEXT_FILE).reader());
        KeyValue kv;
        boolean creditsRead = false;
        while ((kv = new KeyValue(br)).key != null) {
            switch (kv.key) {
                case "credits":
                    if(!creditsRead)
                        credits = kv.value;
                    break;
                default:
                    if (kv.key.startsWith("credits-")) {
                        String lang = kv.key.substring(8);
                        if(lang.equals(preferences.getLanguage())) {
                            credits = kv.value;
                            creditsRead = true;
                        }
                    }
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

        int res = preferences.getSymbolResolution()/2;

        // table for menu
        Table table = new Table();
        scroller = new ScrollPane(table);
        rootTable.add(scroller).top().center().fill().expand();

        rootTable.setBackground(new BackroundDrawer(bg));

        // Credits-message
        Label m = new Label(credits + "\n\nVersion: " + main.getVersionString(),main.getLevelSkin(res));
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

        if(!fader.isActive()) {
            float current = scroller.getScrollY();
            float scrollmax = scroller.getMaxY();
            if (current < scrollmax) {
                float gdxtime = Gdx.graphics.getDeltaTime();
                realTime = (long) (gdxtime * 1000);

                long deltaTime = (long) (gdxtime * Game.timeResolutionSquare); // needs to have long in front as gdxtime is float (don't apply long directly to gdxtime)
                deltaTime += lastDeltaTimeLeft; // apply what is left
                long step = deltaTime / Game.timeResolution;
                deltaTime -= step * Game.timeResolution;
                lastDeltaTimeLeft = deltaTime;
                if (realTimePassed > Game.timeResolutionSquare * 5) {
                    current += (float) step * preferences.getSymbolResolution() / PlayWindow.virtualBlockResolution;
                    scroller.setScrollY(current);
                } else {
                    realTimePassed += step * Game.timeResolution;
                }
            }
            stage.act();
        }
        stage.draw();
        fader.render();
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
        fader.dispose();
        stage.dispose();
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        finished = true;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
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
}