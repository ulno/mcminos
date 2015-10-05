package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the Main class from where the game is controlled.
 *
 */
public class Main extends com.badlogic.gdx.Game {
    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Skin skin;
    private Audio audio;
    public static final String DEFAULT_UISKIN = "uiskins/default/uiskin.json";
    public static final String DEFAULT_FONT = "fonts/liberation-sans-64.fnt";


    @Override
	public void create () {
        Gdx.graphics.setVSync(true); // try some magic on the desktop TODO: check if this has any effect
        audio = new Audio();
        font = new BitmapFont(Gdx.files.internal(DEFAULT_FONT));
        skin = new Skin( Gdx.files.internal(DEFAULT_UISKIN) );
        batch = new SpriteBatch();
        this.setScreen(new Load(this));
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
        batch.dispose();
        skin.dispose();
        font.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public Skin getSkin() {
        return skin;
    }

    public Audio getAudio() {
        return audio;
    }
}
