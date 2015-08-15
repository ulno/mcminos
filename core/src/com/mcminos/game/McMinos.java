package com.mcminos.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class McMinos extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Entities gfx;
    long gameTime = 0;
	
	@Override
	public void create () {
        gfx  = Entities.getInstance();
		batch = new SpriteBatch();
        //img = new Texture( Gdx.files.internal("entities/mcminos_default_front_128_1.png"));
	}

	@Override
	public void render () {
        gameTime += (long)(Gdx.graphics.getDeltaTime() * 1000);
		//Gdx.gl.glClearColor(0, 0, 1, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        img = gfx.backgrounds_dry_grass.getTexture(128, gameTime);
        for( int x=0; x<1920; x+=128)
            for( int y=0; y<1080; y+=128)
                batch.draw(img, x, y);
		img = gfx.mcminos_default_front.getTexture(128, gameTime);
        batch.draw(img, 100, 100);
        img = gfx.ghosts_hanky.getTexture(128,gameTime);
        batch.draw(img, 100, 300);
        img = gfx.ghosts_zarathustra.getTexture(128,gameTime);
        batch.draw(img, 300, 300);
        img = gfx.ghosts_panky.getTexture(128,gameTime);
        batch.draw(img, 300, 100);
		batch.end();
	}
}
