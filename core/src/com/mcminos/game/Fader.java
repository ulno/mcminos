package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Created by ulno on 11.01.16.
 */
public class Fader {
    private final Main main;
    private final Audio audio;
    private int width;
    private int height;
    private ShapeRenderer box;
    private long fadeFramesLeft = 0;
    private float fadeStep;
    private float fadeValue;
    private boolean fadingIn;
    private boolean outInActive = false;
    private SpriteBatch batch;
    public final long fadeFrames = 45;
    private TextureRegion lastScreen;
    private int playMusicFixedType = -1;


    public Fader(Main main, int width, int height) {
        this.main = main;
        this.audio = main.getAudio();
        batch = new SpriteBatch();
        box = new ShapeRenderer();
        this.width = width;
        this.height = height;
    }

    public void fadeIn() {
        fadingIn = true;
        fadeFramesLeft = fadeFrames;
        fadeStep = 1.0f/ fadeFrames;
        fadeValue = 1.0f; // We start invisible and slowly become visible again
    }

    public void fadeOut() {
//        lastScreen = ScreenUtils.getFrameBufferTexture();
        lastScreen = ScreenUtils.getFrameBufferTexture(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        fadingIn = false;
        fadeFramesLeft = fadeFrames;
        fadeStep = 1.0f/ fadeFrames;
        fadeValue = 0.0f; // We start visible and slowly become invisible
    }

    public void fadeOutIn() {
        outInActive = true;
        fadeOut();
    }

    public void dispose() {
        box.dispose();
        batch.dispose();
    }

    public boolean isActive() {
        return fadeFramesLeft > 0;
    }

    public void render() {
        fadeFramesLeft--;
        if (fadeFramesLeft > 0) {
            if(fadeFramesLeft == fadeFrames-1) { // play sound delayed to prevent double play
                if(fadingIn) audio.soundPlay("fade");
                else audio.soundPlay("fade2");
            }
            int w = width;
            int h = height;
            if(outInActive) {
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                batch.begin();
                /*TextureRegion background = Entities.backgrounds_amoeboid_01.getTexture(res, 0);
                int res = Preferences.MAXRES;
                int xoffset = background.getRegionWidth();
                int yoffset = background.getRegionHeight();
                for (int x = 0; x < w + res; x += xoffset) {
                    for (int y = 0; y < h + res; y += yoffset) {
                        batch.draw(background, x, y);
                    }
                }*/
                batch.draw(lastScreen,0,0,w,h);
                batch.end();
                if (fadeFramesLeft == 1) {
                    lastScreen.getTexture().dispose();
                    outInActive = false;
                    fadeIn();
                }
            }
            if (fadingIn) {
                fadeValue -= fadeStep;
                if (fadeFramesLeft == 1) {
                    if(playMusicFixedType >=0)
                        audio.musicFixed(playMusicFixedType);
                }
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

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void fadeOutInMusicFixed(int type) {
        audio.musicStop();
        fadeOutIn();
        playMusicFixedType = type;

    }
}
