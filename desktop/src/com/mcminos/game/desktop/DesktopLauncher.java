package com.mcminos.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mcminos.game.McMinos;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "McMinos-mobile Desktop";
		config.width = 1600;
		config.height = 900;
		new LwjglApplication(new McMinos(), config);
	}
}
