package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the Main class from where the game is controlled.
 *
 */
public class Main extends Game implements ApplicationListener, GestureListener, InputProcessor {
    private int touchDownX;
    private int touchDownY;
    private long lastZoomTime = 0;
    private Root g = Root.getInstance();

    @Override
	public void create () {
        Gdx.graphics.setVSync(true); // try some magic on the desktop TODO: check if this has any effect
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(gd,this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
        g.init();
        g.loadLevel("levels/level023.asx");
        g.startMover();
        g.loadSounds();
    }

    @Override
    public void resize(int width, int height) {
        //super.resize(width, height);
        g.resize(width,height);
    }

    @Override
	public void render () {
        double deltaTime = Gdx.graphics.getDeltaTime();
        //double offsetX = -0.2;
        //double offsetY = -0.5;

        // moving is not handled in rendering method
        //

        g.updateTime(); // TODO: so we would actually not need to track time here
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		g.batch.begin();
        try {
            Root.updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LevelObject.drawAll();
        Root.updateLock.release();
        g.defaultFont.draw(g.batch,"Test " + g.level.getPillsNumber(),20,Gdx.graphics.getHeight()-20);
        /*for( int windowVPixelXPos=0; windowVPixelXPos<50; windowVPixelXPos++ )
            for( int windowVPixelYPos=0; windowVPixelYPos<50; windowVPixelYPos++ )
                Entities.backgrounds_dry_grass.draw(batch, gameTime, windowVPixelXPos, windowVPixelYPos, offsetX, offsetY);
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
        g.setResolution( Entities.resolutionList[g.gameResolutionCounter] );
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if( pointer>0) return false;
        // map windowVPixelXPos windowVPixelYPos to game coordinates
        // TODO: consider only first button/finger
        int x = Util.shiftLeftLogical(screenX, Root.virtualBlockResolutionExponent - Root.resolutionExponent) + Root.windowVPixelXPos - (Root.virtualBlockResolution >> 1);
        //if(Root.getScrollX()) { allways do this
            if ( x >= Root.getVPixelsLevelWidth() )
                x -= Root.getVPixelsLevelWidth();
            if ( x <= - (Root.virtualBlockResolution >> 1) )
                x += Root.getVPixelsLevelWidth();
        //}
        //else {
        //    if( x >= Root.windowVPixelXPos + Root.getWindowVPixelWidth() - (Root.virtualBlockResolution >> 1) )
        //        x = Root.windowVPixelXPos + Root.getWindowVPixelWidth() - (Root.virtualBlockResolution >> 1);
        //}

        int y = Util.shiftLeftLogical(Gdx.graphics.getHeight() - screenY, (Root.virtualBlockResolutionExponent - Root.resolutionExponent))
                + Root.windowVPixelYPos - (Root.virtualBlockResolution >> 1); // flip windowVPixelYPos-axis
        //if(Root.getScrollY()) { allways
            if( y >= Root.getVPixelsLevelHeight())
                y -= Root.getVPixelsLevelHeight();
            if( y <= - (Root.virtualBlockResolution >> 1) )
                y += Root.getLevelHeight();
        //}
        //else {
        //    if( y >= Root.windowVPixelYPos + Root.getWindowVPixelHeight() - (Root.virtualBlockResolution >> 1) )
        //        y = Root.windowVPixelYPos + Root.getWindowVPixelHeight() - (Root.virtualBlockResolution >> 1);
        //}
        Root.destination.moveTo(x, y);
        // Check if it's on Main field
        // if collide, remove destination graphics
        Root.destination.setGfx(Entities.destination);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return touchDown(screenX,screenY,pointer,1); // Forward to touch
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
        /*// quick hack to allow touch events
        if(velocityX < 0) {
            keyTyped('-');
            return true;
        }
        else if(velocityX>0) {
            keyTyped('+');
            return true;
        }
        return true;*/
        return false;
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
            if( initialDistance > distance + g.windowPixelHeight /4) {
                Root.gameResolutionCounter++;
                if (Root.gameResolutionCounter > Entities.resolutionList.length - 1)
                    Root.gameResolutionCounter = Entities.resolutionList.length - 1;
            }
            else if( initialDistance < distance - g.windowPixelHeight /4) {
                Root.gameResolutionCounter--;
                if (Root.gameResolutionCounter < 0) g.gameResolutionCounter = 0;
            }
            g.setResolution(Entities.resolutionList[Root.gameResolutionCounter]);
            lastZoomTime = g.gameTime;
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
