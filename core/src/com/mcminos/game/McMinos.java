package com.mcminos.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class McMinos extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture img;
	Entities gfx;
    long gameTime = 0;
    private int windowWidth;
    private int windowHeight;
    private int gameResolution = 128;

    @Override
	public void create () {
        gfx  = Entities.getInstance();
        windowWidth = Gdx.graphics.getWidth(); //width of screen
        windowHeight = Gdx.graphics.getHeight(); //height of screen
        GameGraphics.setResolutionAll(gameResolution);
		batch = new SpriteBatch();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowWidth = width;
        windowHeight = height;
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0, 0, width, height);
        batch.setProjectionMatrix(matrix);
    }

    @Override
	public void render () {
        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
		//Gdx.gl.glClearColor(0, 0, 1, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        img = gfx.backgrounds_dry_grass.getTexture(gameTime);
        for( int x=0; x<windowWidth; x+=gameResolution)
            for( int y=0; y<windowHeight; y+=gameResolution)
                batch.draw(img, x, y);
		img = gfx.mcminos_default_front.getTexture(gameTime);
        batch.draw(img, 100, 100);
        img = gfx.ghosts_hanky.getTexture(gameTime);
        batch.draw(img, 100, 300);
        img = gfx.ghosts_zarathustra.getTexture(gameTime);
        batch.draw(img, 300, 300);
        img = gfx.ghosts_panky.getTexture(gameTime);
        batch.draw(img, 300, 100);
		batch.end();
	}

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
