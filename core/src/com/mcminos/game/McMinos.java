package com.mcminos.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

public class McMinos implements ApplicationListener, GestureListener, InputProcessor {
    private int touchDownX;
    private int touchDownY;
    private long lastZoomTime = 0;
    private Game g = Game.getInstance();

    @Override
	public void create () {
        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        im.addProcessor(this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
        g.init();
        g.loadLevel("levels/level008.asx");
    }

    @Override
    public void resize(int width, int height) {
        //super.resize(width, height);
        g.resize(width,height);
    }

    @Override
	public void render () {
        //double offsetX = -0.2;
        //double offsetY = -0.5;

        g.updateTime();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		g.batch.begin();
        LevelObject.drawAll();
        /*for( int x=0; x<50; x++ )
            for( int y=0; y<50; y++ )
                Entities.backgrounds_dry_grass.draw(batch, gameTime, x, y, offsetX, offsetY);
        Entities.mcminos_default_front.draw(batch, gameTime, 2, 2, offsetX, offsetY);
        Entities.ghosts_hanky.draw(batch, gameTime, 4, 2, offsetX, offsetY);
        Entities.ghosts_zarathustra.draw(batch, gameTime, 0, 5, offsetX, offsetY);
        Entities.ghosts_panky.draw(batch, gameTime, 5, 5, offsetX, offsetY);

        Entities.walls_default_01.draw(batch, gameTime, 10, 10, offsetX, offsetY);
        Entities.walls_default_02.draw(batch, gameTime, 11, 10, offsetX, offsetY);
        Entities.walls_default_04.draw(batch, gameTime, 12, 10, offsetX, offsetY);
        Entities.walls_default_08.draw(batch, gameTime, 13, 10, offsetX, offsetY);

        Entities.pills_power_pill_apple.draw(batch, gameTime, 1, 1, offsetX, offsetY);
        Entities.pills_power_pill_cookie.draw(batch, gameTime, 1, 3, offsetX, offsetY);*/
        g.batch.end();
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
                g.gameResolutionCounter ++;
                if (g.gameResolutionCounter > Entities.resolutionList.length - 1)
                    g.gameResolutionCounter = Entities.resolutionList.length - 1;
                break;
            case '+':
                g.gameResolutionCounter --;
                if (g.gameResolutionCounter < 0) g.gameResolutionCounter = 0;
                break;
        }
        g.resolution = Entities.resolutionList[g.gameResolutionCounter];
        GameGraphics.setResolutionAll( );
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
        if( g.gameTime - lastZoomTime > 500 ) { // ignore some events
            if( initialDistance > distance + g.h /4) {
                g.gameResolutionCounter++;
                if (g.gameResolutionCounter > Entities.resolutionList.length - 1)
                    g.gameResolutionCounter = Entities.resolutionList.length - 1;
                lastZoomTime = g.gameTime;
            }
            else if( initialDistance < distance - g.h /4) {
                g.gameResolutionCounter--;
                if (g.gameResolutionCounter < 0) g.gameResolutionCounter = 0;
                lastZoomTime = g.gameTime;
            }
            g.resolution = Entities.resolutionList[g.gameResolutionCounter];
            GameGraphics.setResolutionAll(  );
        }
        return true; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
