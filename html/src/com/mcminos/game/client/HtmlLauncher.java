package com.mcminos.game.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.mcminos.game.Main;

public class HtmlLauncher extends GwtApplication {
    // methods adapted from: http://stackoverflow.com/questions/22419472/how-to-handle-resize-in-libgdx-on-html-gwtapplication
    // TODO: check why size seems to be a littel bigger than visible size

    @Override
    public ApplicationListener getApplicationListener() {
        return new Main();
    }


    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent ev) {
                Gdx.graphics.setDisplayMode(ev.getWidth() * 9 / 10, ev.getHeight() * 9 / 10, false);
            }
        });
    }

    @Override
    public GwtApplicationConfiguration getConfig() {
        int height = com.google.gwt.user.client.Window.getClientHeight() * 9 / 10;
        int width = com.google.gwt.user.client.Window.getClientWidth() * 9 / 10;
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(width, height);
        return cfg;
    }

}