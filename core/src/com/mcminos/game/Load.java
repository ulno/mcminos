package com.mcminos.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 10.09.15.
 */
public class Load implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final Texture loadscreen;
    private final Image loadimage;
    private final Main main;
    private final SpriteBatch batch;
    private final Audio audio;
    private int step = 0; // step to load
    private AssetManager manager;
    private ProgressBar bar;
    private boolean loadingDone = false;
    private float progress = 0.0f;
    private LevelsConfig levelsConfig;

    public Load(Main main) {

        this.main = main;
        audio = main.getAudio();
        manager = new AssetManager(); // create the assetmanager to do scheduled loading

        // initialize and load necessary elements, which we need to show progress (image + skin)

        // pre-load the graphics to show in the load screen
        loadscreen = new Texture(Gdx.files.internal("images/loadscreen.png"));
        loadimage = new Image(loadscreen);
        loadimage.setZIndex(0);
        loadimage.setScaling(Scaling.none);
        Util.scaleBackground(loadimage);

        // Set up everything for the current screen
        skin = main.getMenuSkin(32); // so far only one is loaded
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);

        // build stage
        stage.addActor(loadimage);

        //int percentLoaded = Math.round(manager.getProgress() * 100);
        //font.draw(Game.batch, "Loading resources " + percentLoaded + "%", 20, (Gdx.graphics.getHeight() - 64) / 2);

        /*ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(skin.newDrawable("white", Color.DARK_GRAY), textureBar);
        barStyle.knobBefore = barStyle.knob;
        bar = new ProgressBar(0, 10, 0.5f, false, barStyle);*/
        bar = new ProgressBar(0.0f, 1.0f, 0.01f, false, skin);

        stage.addActor(bar);

        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void show() {

    }

    /**
     * @return true, if all is loaded
     */
    public boolean loadNext() {
        switch (step) {
            case 0:
                main.loadSkinAndFont(8);
                progress = 0.03f;
                step++;
                break;
            case 1:
                main.loadSkinAndFont(16);
                progress = 0.06f;
                step++;
                break;
            case 2:
                main.loadSkinAndFont(64);
                progress = 0.09f;
                step++;
                break;
            case 3:
                main.loadSkinAndFont(128);
                progress = 0.12f;
                step++;
                break;
            case 4:
                scheduleLoadsAudio();
                progress = 0.14f;
                step++;
                break;
            case 5:
                scheduleLoadsGraphics();
                progress = 0.14f;
                step++;
                break;
            case 6:
                if (manager.update(200)) { // check if done
                    step++;
                }
                progress = 0.2f + manager.getProgress() * 0.6f;
                break;
            case 7:
                Entities.finishLoad(manager.get("entities/pack.atlas", TextureAtlas.class));
                progress = 0.85f;
                step++;
                break;
            case 8:
                finishLoads();
                progress = 0.9f;
                step++;
                break;
            case 9:
                levelsConfig = new LevelsConfig("levels/list");
                main.initLevelsConfig(levelsConfig);
                progress = 0.98f;
                step ++;
                break;
            case 10:
                main.loadUserStats();
                progress = 1.0f;
                return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // draw state
        //Gdx.gl.glClearColor(0, 0, 0, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Util.scaleBackground(loadimage);

        bar.setPosition(w / 5, 0);
        bar.setSize(w * 3 / 5, h / 5);

        // set value of progress bar
        bar.setValue(progress);
        stage.act(delta);
        stage.draw();

        if ( loadNext() ) {
            // Then switch screen
            this.dispose();
            // was done begore main.initLevelsConfig(levelsConfig);
            MainMenu mainMenu = new MainMenu(main);
            main.initMainMenu( mainMenu );
            main.activateMainMenu( null );
        }

    }

    public void scheduleLoadsGraphics() {
        // Graphics
        Entities.scheduleLoad(manager);
    }

    public void scheduleLoadsAudio() {
        audio.scheduleLoads(manager);
    }

    public void finishLoads() {
        audio.finishLoads(manager);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        //manager.dispose(); disables the sounds
        loadscreen.dispose();
        batch.dispose();
    }
}
