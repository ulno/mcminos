package com.mcminos.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class McMinos implements ApplicationListener, GestureListener, InputProcessor {
	SpriteBatch batch;
	Entities gfx;
    long gameTime = 0;
    private int windowWidth;
    private int windowHeight;
    private int gameResolution;
    private int gameResolutionCounter;
    private float density;
    private int touchDownX;
    private int touchDownY;
    private long lastZoomTime = 0;

    @Override
	public void create () {
        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        im.addProcessor(this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
        gfx  = Entities.getInstance();
        windowWidth = Gdx.graphics.getWidth(); //width of screen
        windowHeight = Gdx.graphics.getHeight(); //height of screen
		batch = new SpriteBatch();
        density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // Basically, based on density, we want to set out default zoomlevel.
        gameResolutionCounter = 0;
        gameResolution = Entities.resolutionList[gameResolutionCounter]; // TODO: figure out resolution, for now, just use 128
        GameGraphics.setResolutionAll(gameResolution);
        // Load a level
        // Level
    }

    @Override
    public void resize(int width, int height) {
        //super.resize(width, height);
        windowWidth = width;
        windowHeight = height;
        // Solution from here: http://gamedev.stackexchange.com/questions/68785/why-does-resizing-my-game-window-move-and-distort-my-rendering
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0, 0, windowWidth, windowHeight);
        batch.setProjectionMatrix(matrix);
    }

    @Override
	public void render () {
        double offsetX = -0.2;
        double offsetY = -0.5;

        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        for( int x=0; x<50; x++ )
            for( int y=0; y<50; y++ )
                Entities.backgrounds_dry_grass.draw(batch, gameTime, x, y, offsetX, offsetY);
        Entities.mcminos_default_front.draw(batch, gameTime, 2, 2, offsetX, offsetY);
        Entities.ghosts_hanky.draw(batch, gameTime, 4, 2, offsetX, offsetY);
        Entities.ghosts_zarathustra.draw(batch, gameTime, 0, 5, offsetX, offsetY);
        Entities.ghosts_panky.draw(batch, gameTime, 5, 5, offsetX, offsetY);
        Entities.walls_default_04.draw(batch, gameTime, 3, 7, offsetX, offsetY);
        Entities.walls_default_03.draw(batch, gameTime, 3, 6, offsetX, offsetY);
        Entities.walls_default_08.draw(batch, gameTime, 4, 6, offsetX, offsetY);
        Entities.walls_default_00.draw(batch, gameTime, 3, 4, offsetX, offsetY);
        Entities.pills_power_pill_apple_power_pill_apple.draw(batch, gameTime, 1, 1, offsetX, offsetY);
        Entities.pills_power_pill_cookie.draw(batch, gameTime, 1, 3, offsetX, offsetY);
        batch.end();
	}

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

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

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        // quick hack to allow touch events
        if(velocityX < 0) {
            keyTyped('-');
            return true;
        }
        else if(velocityX>0) {
            keyTyped('+');
            return true;
        }
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if( gameTime - lastZoomTime > 500 ) { // ignore some events
            if( initialDistance > distance + windowHeight/4) {
                gameResolutionCounter++;
                if (gameResolutionCounter > Entities.resolutionList.length - 1)
                    gameResolutionCounter = Entities.resolutionList.length - 1;
                lastZoomTime = gameTime;
            }
            else if( initialDistance < distance - windowHeight/4) {
                gameResolutionCounter--;
                if (gameResolutionCounter < 0) gameResolutionCounter = 0;
                lastZoomTime = gameTime;
            }
            gameResolution = Entities.resolutionList[gameResolutionCounter];
            GameGraphics.setResolutionAll( gameResolution );
        }
        return true; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
