package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by ulno on 11.01.16.
 */
public class Fader {
    private final Main main;
    private final Audio audio;
    private ShapeRenderer box;
    private long fadeFramesLeft = 0;
    private long fadeInterval = 0;
    private float fadeStep;
    private float fadeValue;
    private boolean fadingIn;
    private boolean outInActive = false;
    private SpriteBatch batch;
    public final long fadeFrames = 45;


    public Fader(Main main) {
        this.main = main;
        this.audio = main.getAudio();
        batch = new SpriteBatch();
        box = new ShapeRenderer();
    }

    public void fadeIn() {
        fadingIn = true;
        fadeFramesLeft = fadeFrames;
        fadeInterval = fadeFrames;
        fadeStep = 1.0f/ fadeFrames;
        fadeValue = 1.0f; // We start invisible and slowly become visible again
        audio.soundPlay("fade");
    }

    public void fadeOut() {
        fadingIn = false;
        fadeFramesLeft = fadeFrames;
        fadeInterval = fadeFrames;
        fadeStep = 1.0f/ fadeFrames;
        fadeValue = 0.0f; // We start visible and slowly become invisible
        audio.soundPlay("fade2");
    }

    public void fadeOutIn() {
        outInActive = true;
        fadeOut();
    }

    public void dispose() {

    }

    public boolean isActive() {
        return fadeFramesLeft > 0;
    }

    public void render() {
        fadeFramesLeft--;
        if (fadeFramesLeft > 0) {
            int w = Gdx.graphics.getWidth();
            int h = Gdx.graphics.getHeight();
            if(outInActive) {
                int res = main.getPreferences().getGameResolution();
                TextureRegion background = Entities.backgrounds_amoeboid_01.getTexture(res, 0);
                batch.begin();
                int xoffset = background.getRegionWidth();
                int yoffset = background.getRegionHeight();
                for (int x = 0; x < w + res; x += xoffset) {
                    for (int y = 0; y < h + res; y += yoffset) {
                        batch.draw(background, x, y);
                    }
                }
                batch.end();
                if (fadeFramesLeft == 1) {
                    outInActive = false;
                    fadeIn();
                }
            }
            if (fadingIn) {
                fadeValue -= fadeStep;
            } else { // fading Out
                fadeValue += fadeStep;
            }
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            box.begin(ShapeRenderer.ShapeType.Filled);
            box.setColor(0, 0, 0, fadeValue);
            box.rect(0, 0, w, h);
            box.end();
        }
    }
}
