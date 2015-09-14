package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenu implements Screen {

    private final Skin skin;

    public MainMenu() {

        skin = new Skin(Gdx.files.internal("uiskins/default/uiskin.json"));
        Root.stage.clear();

        TextButton button = new TextButton("Click Me",skin,"default");
        button.setWidth(200);
        button.setHeight(50);
        Root.stage.addActor(button);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Root.batch.begin();
        //Root.defaultFont.draw(Root.batch, "Loading resources " + percentLoaded + "%", 20, (Gdx.graphics.getHeight() - 64) / 2);
        Root.batch.end();
        Root.stage.act(Gdx.graphics.getDeltaTime());
        Root.stage.draw();
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
