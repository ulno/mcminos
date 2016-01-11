package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
    public final long fadeFrames = 60;


    public Fader(Main main) {
        this.main = main;
        this.audio = main.getAudio();
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

    public void dispose() {

    }

    public boolean isActive() {
        return fadeFramesLeft > 0;
    }

    public void render() {
        fadeFramesLeft--;
        if (fadeFramesLeft > 0) {
            if (fadingIn) {
                fadeValue -= fadeStep;
            } else { // fading Out
                fadeValue += fadeStep;
            }
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            box.begin(ShapeRenderer.ShapeType.Filled);
            box.setColor(0, 0, 0, fadeValue);
            box.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            box.end();
        }
    }
}
