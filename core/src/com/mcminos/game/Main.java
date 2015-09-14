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
    private Root root;

    @Override
	public void create () {
        root = Root.getInstance();
        Gdx.graphics.setVSync(true); // try some magic on the desktop TODO: check if this has any effect
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(gd,this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
        root.setMain(this); // need reference to switch screen
        this.setScreen(new Load());
    }

    @Override
    public void resize(int width, int height) {
        //super.resize(width, height);
        root.resize(width, height);
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
                root.gameResolutionCounter ++;
                if (root.gameResolutionCounter > Entities.resolutionList.length - 1)
                    root.gameResolutionCounter = Entities.resolutionList.length - 1;
                break;
            case '+':
                root.gameResolutionCounter --;
                if (root.gameResolutionCounter < 0) root.gameResolutionCounter = 0;
                break;
        }
        root.setResolution( Entities.resolutionList[root.gameResolutionCounter] );
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
        if( root.gameTime - lastZoomTime > 500 ) { // ignore some events
            if( initialDistance > distance + root.windowPixelHeight /4) {
                Root.gameResolutionCounter++;
                if (Root.gameResolutionCounter > Entities.resolutionList.length - 1)
                    Root.gameResolutionCounter = Entities.resolutionList.length - 1;
            }
            else if( initialDistance < distance - root.windowPixelHeight /4) {
                Root.gameResolutionCounter--;
                if (Root.gameResolutionCounter < 0) root.gameResolutionCounter = 0;
            }
            root.setResolution(Entities.resolutionList[Root.gameResolutionCounter]);
            lastZoomTime = root.gameTime;
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

}
