package com.mcminos.game;

import com.badlogic.gdx.Screen;

/**
 * Created by ulno on 11.01.16.
 */
public class FadeExit implements Screen {
    private final Main main;
    private final Fader fader;
    private int lingerTime = 20; // Time after Fadeout to stay befoe calling exit

    public FadeExit(Main main) {
        this.main = main;
        main.getAudio().musicStop();
        fader = new Fader(main);
        fader.fadeOut();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(!fader.isActive()) {
            lingerTime --;
        }
        fader.render();
        if(lingerTime == 0) {
            main.exit();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        fader.dispose();
    }
}
