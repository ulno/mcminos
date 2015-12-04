package com.mcminos.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mcminos.game.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
        /*Preferences preferencesHandle = Gdx.app.getPreferences( "com.mcminos.game.prefs");
        if(!preferencesHandle.contains("fs")) {
            preferencesHandle.putBoolean("fs",false);
        }
        boolean fs = preferencesHandle.getBoolean("fs");*/
        boolean fs = arg.length>0 && arg[0].equals("fullscreen");
        //boolean fs = true;
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "McMinos-Mobile Desktop";
        config.fullscreen = fs;
        config.vSyncEnabled = true;
        if(!fs) {
            config.width = 1280;
            config.height = 896;
        } else {
            config.width = config.getDesktopDisplayMode().width;
            config.height = config.getDesktopDisplayMode().height;
        }
        new LwjglApplication(new Main(), config);
	}
}
