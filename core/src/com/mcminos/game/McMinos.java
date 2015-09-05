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
        g.loadLevel("levels/level006.asx");
        g.startMover();
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
            Game.updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LevelObject.drawAll();
        Game.updateLock.release();
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
        int x = Util.shiftLeftLogical(screenX, Game.virtualBlockResolutionExponent - Game.resolutionExponent) + Game.windowVPixelXPos - (Game.virtualBlockResolution >> 1);
        //if(Game.getScrollX()) { allways do this
            if ( x >= Game.getVPixelsLevelWidth() )
                x -= Game.getVPixelsLevelWidth();
            if ( x <= - (Game.virtualBlockResolution >> 1) )
                x += Game.getVPixelsLevelWidth();
        //}
        //else {
        //    if( x >= Game.windowVPixelXPos + Game.getWindowVPixelWidth() - (Game.virtualBlockResolution >> 1) )
        //        x = Game.windowVPixelXPos + Game.getWindowVPixelWidth() - (Game.virtualBlockResolution >> 1);
        //}

        int y = Util.shiftLeftLogical(Gdx.graphics.getHeight() - screenY, (Game.virtualBlockResolutionExponent - Game.resolutionExponent))
                + Game.windowVPixelYPos - (Game.virtualBlockResolution >> 1); // flip windowVPixelYPos-axis
        //if(Game.getScrollY()) { allways
            if( y >= Game.getVPixelsLevelHeight())
                y -= Game.getVPixelsLevelHeight();
            if( y <= - (Game.virtualBlockResolution >> 1) )
                y += Game.getLevelHeight();
        //}
        //else {
        //    if( y >= Game.windowVPixelYPos + Game.getWindowVPixelHeight() - (Game.virtualBlockResolution >> 1) )
        //        y = Game.windowVPixelYPos + Game.getWindowVPixelHeight() - (Game.virtualBlockResolution >> 1);
        //}
        Game.destination.moveTo(x, y);
        // Check if it's on McMinos field
        // if collide, remove destination graphics
        Game.destination.setGfx(Entities.destination);
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
                Game.gameResolutionCounter++;
                if (Game.gameResolutionCounter > Entities.resolutionList.length - 1)
                    Game.gameResolutionCounter = Entities.resolutionList.length - 1;
            }
            else if( initialDistance < distance - g.windowPixelHeight /4) {
                Game.gameResolutionCounter--;
                if (Game.gameResolutionCounter < 0) g.gameResolutionCounter = 0;
            }
            g.setResolution(Entities.resolutionList[Game.gameResolutionCounter]);
            lastZoomTime = g.gameTime;
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
