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

}
