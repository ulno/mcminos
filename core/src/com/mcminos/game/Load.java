package com.mcminos.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    private AssetManager manager;
    private ProgressBar bar;
    private boolean loadingDone = false;

    public Load(Main main) {
        
        this.main = main;
        audio = main.getAudio();

        // initialize and load necessary elements, we need to show progress
        // TODO: also bg image?

        // pre-load the graphics to show in the load screen
        manager = new AssetManager();
        scheduleLoads();
        loadscreen = new Texture( Gdx.files.internal("images/loadscreen.png"));
        loadimage = new Image(loadscreen);
        loadimage.setZIndex(0);
        loadimage.setScaling(Scaling.none);
        Util.scaleBackground(loadimage);

        // Set up everything for the current screen
        skin = main.getSkin();
        batch = main.getBatch();
        stage = new Stage(new ScreenViewport(), batch);

        // build stage
        stage.addActor(loadimage);


        //int percentLoaded = Math.round(manager.getProgress() * 100);
        //font.draw(Game.batch, "Loading resources " + percentLoaded + "%", 20, (Gdx.graphics.getHeight() - 64) / 2);

        /*ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(skin.newDrawable("white", Color.DARK_GRAY), textureBar);
        barStyle.knobBefore = barStyle.knob;
        bar = new ProgressBar(0, 10, 0.5f, false, barStyle);*/
        bar = new ProgressBar(0.0f,1.0f,0.01f,false,skin);

        stage.addActor(bar);

        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void show() {

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
        bar.setSize(w*3/5,h/5);

        // set value of progress bar
        bar.setValue(manager.getProgress()*0.9f);
        stage.act(delta);
        stage.draw();

        // do Loading updates
        if( manager.update(200) ) { // check if done TODO: check value for updates here
            if( ! loadingDone ) {
                // finish initialization
                loadingDone = true;
                Entities.finishLoad(manager.get("entities/pack.atlas", TextureAtlas.class));
                finishLoads();
                //game.loadLevel();
                // Then switch screen
                this.dispose();
                MainMenu screen = new MainMenu(main,null);
                main.setScreen(screen);
            }
        }
    }

    public void scheduleLoads() {
        // Graphics
        Entities.scheduleLoad(manager);
        // Sounds
        for( String s: Audio.soundNames) {
            manager.load("sounds/" + s + ".wav", Sound.class);
        }
        // UIs
        // manager.load(DEFAULT_UISKIN, Skin.class); needs to be pre-loaded
    }

    public void finishLoads() {
        // Sounds
        for( String s: Audio.soundNames ) {
            Sound sound = manager.get("sounds/" + s + ".wav");
            audio.addSound(s, sound);
        }
        // UIs
        // Game.skin =  manager.get(DEFAULT_UISKIN); needs to be pre-laoded
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
    }
}
