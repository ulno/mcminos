package com.mcminos.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;

/**
 * Created by ulno on 10.09.15.
 */
public class Load implements Screen {
    private AssetManager manager;

    public Load() {
        // initialize
        // pre-load the graphics to show in the load screen
        manager = new AssetManager();
        Entities.scheduleLoad(manager);
        scheduleSounds();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Root.batch.begin();
        int percentLoaded = Math.round(manager.getProgress() * 100);
        Root.defaultFont.draw(Root.batch, "Loading resources " + percentLoaded + "%", 20, (Gdx.graphics.getHeight() - 64)/2);
        Root.batch.end();
        // do Loading updates
        if( manager.update(200) ) { // check if done
            // finish initialization
            Entities.finishLoad();
            finishSounds();
            Root root = Root.getInstance();
            root.init();
            // Then switch screen
            MainMenu screen = new MainMenu();
            root.setScreen(screen);
        }
    }

    public void scheduleSounds() {
        for( String s:Root.soundNames) {
            manager.load("sounds/" + s + ".wav", Sound.class);
        }
    }

    public void finishSounds() {
        for( String s:Root.soundNames ) {
            Sound sound = manager.get("sounds/" + s + ".wav");
            Root.soundList.put(s, sound);
        }
    }

    @Override
    public void resize(int width, int height) {

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
