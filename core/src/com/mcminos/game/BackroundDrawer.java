package com.mcminos.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by ulno on 20.12.15.
 */
public class BackroundDrawer implements Drawable {
    private final TextureRegion background;

    public BackroundDrawer(TextureRegion background) {
        this.background = background;
    }

    @Override
    public void draw(Batch batch, float x0f, float y0f, float widthf, float heightf) {
        int x0 = (int) x0f;
        int y0 = (int) y0f;
        int width = (int) widthf;
        int height = (int) heightf;

        for (int x = x0; x < x0 + width; x += background.getRegionWidth())
            for (int y = y0; y < 0 + height; y += background.getRegionHeight())
                batch.draw(background, x, y);
    }

    @Override
    public float getLeftWidth() {
        return 0;
    }

    @Override
    public void setLeftWidth(float leftWidth) {

    }

    @Override
    public float getRightWidth() {
        return 0;
    }

    @Override
    public void setRightWidth(float rightWidth) {

    }

    @Override
    public float getTopHeight() {
        return 0;
    }

    @Override
    public void setTopHeight(float topHeight) {

    }

    @Override
    public float getBottomHeight() {
        return 0;
    }

    @Override
    public void setBottomHeight(float bottomHeight) {

    }

    @Override
    public float getMinWidth() {
        return 0;
    }

    @Override
    public void setMinWidth(float minWidth) {

    }

    @Override
    public float getMinHeight() {
        return 0;
    }

    @Override
    public void setMinHeight(float minHeight) {

    }

}
