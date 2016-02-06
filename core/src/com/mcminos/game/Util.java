package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

import java.io.BufferedReader;

/**
 * Created by ulno on 30.08.15.
 *
 * Some personal utility functions (like binary logarithm)
 */
public class Util {
    static final float CONTROLLER_THRESHOLD = 0.2f;

    public static int log2binary( int bits )
    {
        if( bits == 0 )
            return 0; // or throw exception
        return 31 - Integer.numberOfLeadingZeros( bits );
    }

    public static int shiftLeftLogical( int number, int shift) {
        if(number > 0)
            return shift > 0 ? number << shift : number >> - shift;
        else
            return shift > 0 ? -((-number) << shift) : -((-number) >> - shift);
    }

    /**
     * Position this image so that it can be a background image which never has black bars. We are rather cutting some borders.
     * @param img
     */
    public static void scaleBackground(Image img) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float iw = img.getWidth();
        float ih = img.getHeight();

        float scale = Math.max(w/iw, h / ih);
        img.setScale(scale);
        img.setPosition(w / 2 - iw * scale / 2, h / 2 - ih * scale / 2);
    }


    public static int getKeyDirections(MqttController mqttController) {
        int keyDirections = 0;
        if(Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.UP)
                || mqttController.isDown('w')
                ) keyDirections |= Mover.UP;
        if(Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                || mqttController.isDown('d')
                ) keyDirections |= Mover.RIGHT;
        if(Gdx.input.isKeyPressed(Input.Keys.S)
                || Gdx.input.isKeyPressed(Input.Keys.DOWN)
                || mqttController.isDown('s')
                ) keyDirections |= Mover.DOWN;
        if(Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)
                || mqttController.isDown('a')
                ) keyDirections |= Mover.LEFT;
        Array<Controller> controllers = Controllers.getControllers();
        for(int i=controllers.size-1; i>=0; i--) {
            Controller c = controllers.get(i);
            for(int axes=0; axes<3; axes++) {
                float x = c.getAxis(axes*2);
                float y = c.getAxis(axes*2+1);
                float yThres = 0, xThres = 0;
                if (y < -CONTROLLER_THRESHOLD) {
                    yThres = -y;
                    keyDirections |= Mover.UP;
                } else if (y > CONTROLLER_THRESHOLD) {
                    yThres = y;
                    keyDirections |= Mover.DOWN;
                }
                if (x > CONTROLLER_THRESHOLD) {
                    xThres = x;
                    keyDirections |= Mover.RIGHT;
                } else if (x < -CONTROLLER_THRESHOLD) {
                    xThres = -x;
                    keyDirections |= Mover.LEFT;
                }
                if (yThres > xThres * 2) { // significantly bigger in y
                    keyDirections &= Mover.UP | Mover.DOWN; // forget x
                } else if (xThres > yThres * 2) { // significantly bigger in x
                    keyDirections &= Mover.LEFT | Mover.RIGHT; // forget y
                }
            }
            for( int pov=0; pov<1; pov ++ ) { // TODO: check how many
                switch (c.getPov(pov)) {
                    case north:
                        keyDirections |= 1;
                        break;
                    case northEast:
                        keyDirections |= 3;
                        break;
                    case east:
                        keyDirections |= 2;
                        break;
                    case southEast:
                        keyDirections |= 6;
                    case south:
                        keyDirections |= 4;
                        break;
                    case southWest:
                        keyDirections |= 12;
                        break;
                    case west:
                        keyDirections |= 8;
                        break;
                    case northWest:
                        keyDirections |= 9;
                }
            }
        }
        return keyDirections;
    }
}
