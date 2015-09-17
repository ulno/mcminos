package com.mcminos.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 10.09.15.
 */
public class Load implements Screen {
    private final BitmapFont font;
    private final Stage stage;
    private final Skin skin;
    private final Texture loadscreen;
    private final Image loadimage;
    private AssetManager manager;
    private ProgressBar bar;
    private boolean loadingDone = false;

    public Load() {

        // initialize and load necessary elements, we need to show progress
        // TODO: also bg image?

        // pre-load the graphics to show in the load screen
        manager = new AssetManager();
        scheduleLoads();
        loadscreen = new Texture( Gdx.files.internal("images/loadscreen.png"));
        loadimage = new Image(loadscreen);
        loadimage.setZIndex(0);
        loadimage.setScaling(Scaling.none);
        Root.scaleBackground(loadimage);

        // Set up everything for the current screen
        skin = Root.defaultSkin;
        font = Root.defaultFont;
        stage = new Stage(new ScreenViewport(),Root.batch);

        // build stage
        stage.addActor(loadimage);


        //int percentLoaded = Math.round(manager.getProgress() * 100);
        //font.draw(Root.batch, "Loading resources " + percentLoaded + "%", 20, (Gdx.graphics.getHeight() - 64) / 2);

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

        Root.scaleBackground(loadimage);

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
                Entities.finishLoad();
                finishLoads();
                Root root = Root.getInstance();
                root.init();
                // Then switch screen
                MainMenu screen = new MainMenu();
                root.setScreen(screen);
            }
        }
    }

    public void scheduleLoads() {
        // Graphics
        Entities.scheduleLoad(manager);
        // Sounds
        for( String s:Root.soundNames) {
            manager.load("sounds/" + s + ".wav", Sound.class);
        }
        // UIs
        // manager.load(UISKIN_DEFAULT, Skin.class); needs to be pre-loaded
    }

    public void finishLoads() {
        // Sounds
        for( String s:Root.soundNames ) {
            Sound sound = manager.get("sounds/" + s + ".wav");
            Root.soundList.put(s, sound);
        }
        // UIs
        // Root.defaultSkin =  manager.get(UISKIN_DEFAULT); needs to be pre-laoded
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
        manager.dispose();
        loadscreen.dispose();
    }
}
