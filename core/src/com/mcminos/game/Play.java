package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

/**
 * Created by ulno on 10.09.15.
 */
public class Play implements Screen {
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        double deltaTime = Gdx.graphics.getDeltaTime();
        //double offsetX = -0.2;
        //double offsetY = -0.5;

        // moving is not handled in rendering method
        //
        Root.updateTime(); // TODO: so we would actually not need to track time here
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Root.batch.begin();
        try {
            Root.updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LevelObject.drawAll();
        Root.updateLock.release();
        Root.defaultFont.draw(Root.batch, "Test " + Root.level.getPillsNumber(), 20, Gdx.graphics.getHeight() - 20);
        /*for( int windowVPixelXPos=0; windowVPixelXPos<50; windowVPixelXPos++ )
            for( int windowVPixelYPos=0; windowVPixelYPos<50; windowVPixelYPos++ )
                Entities.backgrounds_dry_grass.draw(batch, gameTime, windowVPixelXPos, windowVPixelYPos, offsetX, offsetY);
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
        Root.batch.end();

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

    }
}
