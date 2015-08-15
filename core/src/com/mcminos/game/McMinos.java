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
    private int gameResolution;
    private int gameResolutionCounter;
    private float density;

    @Override
	public void create () {
        Gdx.input.setInputProcessor(this); // init Inputprocessor
        gfx  = Entities.getInstance();
        windowWidth = Gdx.graphics.getWidth(); //width of screen
        windowHeight = Gdx.graphics.getHeight(); //height of screen
		batch = new SpriteBatch();
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        gameResolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        GameGraphics.setResolutionAll(gameResolution);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowWidth = width;
        windowHeight = height;
        // Solution from here: http://gamedev.stackexchange.com/questions/68785/why-does-resizing-my-game-window-move-and-distort-my-rendering
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0, 0, width, height);
        batch.setProjectionMatrix(matrix);
    }

    @Override
	public void render () {
        int offsetX = -10;
        int offsetY = -10;

        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        for( int x=0; x<50; x++ )
            for( int y=0; y<50; y++ )
                gfx.backgrounds_dry_grass.draw(batch, gameTime, x, y, offsetX, offsetY);
		gfx.mcminos_default_front.draw(batch, gameTime, 2, 2, offsetX, offsetY);
        gfx.ghosts_hanky.draw(batch, gameTime, 4, 2, offsetX, offsetY);
        gfx.ghosts_zarathustra.draw(batch, gameTime, 0, 5, offsetX, offsetY);
        gfx.ghosts_panky.draw(batch, gameTime, 5, 5, offsetX, offsetY);
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
        switch( character) {
            case '-':
                gameResolutionCounter ++;
                if (gameResolutionCounter > Entities.resolutionList.length - 1)
                    gameResolutionCounter = Entities.resolutionList.length - 1;
                break;
            case '+':
                gameResolutionCounter --;
                if (gameResolutionCounter < 0) gameResolutionCounter = 0;
                break;
        }
        gameResolution = Entities.resolutionList[gameResolutionCounter];
        GameGraphics.setResolutionAll( gameResolution );
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
