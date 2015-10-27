package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Created by ulno on 30.08.15.
 *
 * Some personal utility functions (like binary logarithm)
 */
public class Util {
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

    /**
     *
     * @param in input number
     * @param fill align with leading zeros to thi slength
     * @return
     *
     * taken from: http://stackoverflow.com/questions/473282/how-can-i-pad-an-integers-with-zeros-on-the-left
     */
    public static String formatInteger( int in, int fill ) {
        boolean negative = false;
        int value, len = 0;

        if(in >= 0){
            value = in;
        } else {
            negative = true;
            value = - in;
            in = - in;
            len ++;
        }

        if(value == 0){
            len = 1;
        } else{
            for(; value != 0; len ++){
                value /= 10;
            }
        }

        StringBuilder sb = new StringBuilder();

        if(negative){
            sb.append('-');
        }

        for(int i = fill; i > len; i--){
            sb.append('0');
        }

        sb.append(in);

        return sb.toString();
    }
}
