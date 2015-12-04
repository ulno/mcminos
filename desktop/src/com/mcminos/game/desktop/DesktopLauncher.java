package com.mcminos.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mcminos.game.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "McMinos-Mobile Desktop";
		config.fullscreen = false;
        config.vSyncEnabled = true;
		config.width = 1280;
		config.height = 896;
//        config.width = 1920;
//        config.height = 1080;
		new LwjglApplication(new Main(), config);
	}
}
