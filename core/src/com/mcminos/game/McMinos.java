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
        double deltaTime = Gdx.graphics.getDeltaTime();
        //double offsetX = -0.2;
        //double offsetY = -0.5;

        // move everybody
        // for now only mcminos
        if(Game.destination.hasGfx()) { // destination is set
            // check screen distance
            double x = Game.mcminos.getX();
            double xdelta = x - Game.destination.getX();
            double xdiff = Math.abs( xdelta );
            if (xdiff < 0.02) xdelta = 0.0;
            else {
                if (Game.getScrollX() && xdiff >= Game.getLevelWidth() / 2.0) xdelta = Math.signum(xdelta);
                else xdelta = -Math.signum(xdelta);
            }
            double y = Game.mcminos.getY();
            double ydelta = y - Game.destination.getY();
            double ydiff = Math.abs( ydelta );
            if (ydiff < 0.02) ydelta = 0.0;
            else {
                if( Game.getScrollY() && ydiff >= Game.getLevelHeight() / 2.0 ) ydelta = Math.signum(ydelta);
                else ydelta = - Math.signum(ydelta);
            }
            double newx = x + xdelta * deltaTime * 2;
            double newy = y + ydelta * deltaTime * 2;

            if(Game.getScrollX()) {
                if (newx < 0.0) newx += Game.getLevelWidth();
                if (newx >= Game.getLevelWidth()) newx -= Game.getLevelWidth();
            }
            if(Game.getScrollY()) {
                if (newy < 0.0) newy += Game.getLevelHeight();
                if (newy >= Game.getLevelHeight()) newy -= Game.getLevelHeight();
            }

            Game.mcminos.setXY(newx, newy);
        }

        g.updateTime();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		g.batch.begin();
        LevelObject.drawAll();
        /*for( int windowXPos=0; windowXPos<50; windowXPos++ )
            for( int windowYPos=0; windowYPos<50; windowYPos++ )
                Entities.backgrounds_dry_grass.draw(batch, gameTime, windowXPos, windowYPos, offsetX, offsetY);
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
        // map windowXPos windowYPos to game coordinates
        // TODO: consider only first button/finger
        double x = (double) screenX / Game.resolution + Game.windowXPos - 0.5;
        if(Game.getScrollX()) {
            if (x >= Game.getLevelWidth())
                x -= Game.getLevelWidth();
            if (x <= -0.5)
                x += Game.getLevelWidth();
        }
        else {
            if( x >= Game.windowXPos + Game.getWindowBlockWidth() - 0.5 ) x = Game.windowXPos + Game.getWindowBlockWidth() - 0.5;
        }
        double y = (double) (Gdx.graphics.getHeight() - screenY) / Game.resolution + Game.windowYPos - 0.5; // flip windowYPos-axis
        if(Game.getScrollY()) {
            if(y>=Game.getLevelHeight())
                y -= Game.getLevelHeight();
            if(y<=-0.5)
                y += Game.getLevelHeight();
        }
        else {
            if( y >= Game.windowYPos + Game.getWindowBlockHeight() - 0.5 ) y = Game.windowYPos + Game.getWindowBlockHeight() - 0.5;
        }
        Game.destination.setXY(x,y);
        // Check if it's on McMinos field
        // if collide, remove destination graphics
        Game.destination.setGfx(Entities.destination);
        return true;
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
                g.gameResolutionCounter++;
                if (g.gameResolutionCounter > Entities.resolutionList.length - 1)
                    g.gameResolutionCounter = Entities.resolutionList.length - 1;
                lastZoomTime = g.gameTime;
            }
            else if( initialDistance < distance - g.windowPixelHeight /4) {
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
