package com.mcminos.game;

import com.badlogic.gdx.*;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the Main class from where the game is controlled.
 *
 */
public class Main extends Game  {
    private Root root;

    @Override
	public void create () {
        root = Root.getInstance();
        Gdx.graphics.setVSync(true); // try some magic on the desktop TODO: check if this has any effect
        root.setMain(this); // need reference to switch screen
        this.setScreen(new Load());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
	public void render () {
        super.render();
	}

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        root.dispose();
    }

}
