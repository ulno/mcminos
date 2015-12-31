package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Created by ulno on 30.12.15.
 *
 * Persistence for
 * Game prefereces like playing music oraudio, game and symbol resolution, selected language
 */
public class Preferences {
    private static final int MINRES = 16;
    private static final int MAXRES = 128;
    private int symbolMinRes;

    public static final com.badlogic.gdx.Preferences preferencesHandle = Gdx.app.getPreferences("com.mcminos.game.prefs");
    private final Main main;
    private final Audio audio;
    private boolean touchpadActive = false;
    private int gameResolution;
    private int symbolResolution;
    private boolean fullScreen = false;
    private boolean dontSave = false;
    private String language;
    public static final String[] languageList = new String[]{"en","de"};

    public Preferences(Main main) {
        this.main = main;
        this.audio = main.getAudio();
        symbolMinRes = Math.max(MINRES, Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 8);
        int dynamicMinResExp = Util.log2binary(symbolMinRes);
        symbolMinRes = Math.min(MAXRES, 1<< dynamicMinResExp);
        language = java.util.Locale.getDefault().toString().substring(0,2);
        load();
    }

    private void save() {
        if (!dontSave) {
            preferencesHandle.putBoolean("s", audio.getSound());
            preferencesHandle.putBoolean("m", audio.getMusic());
            preferencesHandle.putBoolean("t", isTouchpadActive());
            preferencesHandle.putInteger("r", getGameResolution());
            preferencesHandle.putInteger("sr", getSymbolResolution());
            preferencesHandle.putBoolean("fs", getFullScreen());
            preferencesHandle.putString("l", getLanguage());
            preferencesHandle.flush();
        }
    }

    private void load() {

        if (!preferencesHandle.contains("s")) { // first time, so generate
            audio.setSound(true);
            audio.setMusic(true);
            // touchpad should be off by default

            //  Basically, based on density and screensize, we want to set our default zoomlevel.
            // densityvalue is BS float density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
            // let's do everything based on width and height - we assume width>height
            // guess something reasonable
            symbolResolution = Math.max(symbolMinRes,
                    Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 8
            );
            int closestPowerOf2 = symbolMinRes;
            int nearest = symbolMinRes;
            while (closestPowerOf2 <= 128) {
                if (Math.abs(symbolResolution - closestPowerOf2) < Math.abs(symbolResolution - nearest)) {
                    nearest = closestPowerOf2;
                }
                closestPowerOf2 *= 2;
            }
            symbolResolution = nearest;

            gameResolution = Math.max(16,
                    Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 12 // often reported too big
            );

            save(); // create preference file

        }
        dontSave = true;
        symbolResolution = preferencesHandle.getInteger("sr");
        setSound(preferencesHandle.getBoolean("s"));
        setMusic(preferencesHandle.getBoolean("m"));
        boolean tp = preferencesHandle.getBoolean("t");
        if (tp != isTouchpadActive()) toggleTouchpad();
        setGameResolution(preferencesHandle.getInteger("r"));
        setSymbolResolution(preferencesHandle.getInteger("sr"));
        fullScreen = preferencesHandle.getBoolean("fs");
        language = preferencesHandle.getString("l");
        if(language.equals("")) language="en";
        dontSave = false;
    }

    private void toggleTouchpad() {
        setTouchpadActive(!isTouchpadActive());
    }

    public boolean isTouchpadActive() {
        return touchpadActive;
    }

    public int getGameResolution() {
        return gameResolution;
    }

    public int getSymbolResolution() {
        return symbolResolution;
    }

    public void setSound(boolean sound) {
        audio.setSound( sound );
        save();
    }

    public void setMusic(boolean music) {
        audio.setMusic( music );
        save();
    }

    public int setGameResolution(int gameResolution) {
        int gameResolutionExp = Util.log2binary(gameResolution);
        gameResolution = Math.min(MAXRES,Math.max(MINRES,1<<gameResolutionExp));
        this.gameResolution = gameResolution;
        save();
        return gameResolution;
    }

    public void setSymbolResolution(int symbolResolution) {
        this.symbolResolution = symbolResolution;
        if (symbolResolution < symbolMinRes) this.symbolResolution = symbolMinRes;
        if (symbolResolution > MAXRES) this.symbolResolution = MAXRES;
        save();
    }

    public void setTouchpadActive(boolean touchpadActive) {
        this.touchpadActive = touchpadActive;
        save();
    }

    public boolean getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        save();
    }

    public void toggleSound() {
        audio.toggleSound();
        save();
    }

    public boolean getSound() {
        return audio.getSound();
    }

    public void toggleMusic() {
        audio.toggleMusic();
        save();
    }

    public boolean getMusic() {
        return audio.getMusic();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        language = lang;
        save();
    }

    public Graphics languageGfx() {
        Graphics gfx;

        switch (getLanguage()) {
            case "de":
                gfx = Entities.menu_flag_de;
                break;
            case "en":
            default:
                gfx = Entities.menu_flag_en;
                break;
        }
        return gfx;
    }

    public void nextLanguage() {
        int index;
        for(index = 0; index < languageList.length; index ++) {
            if(languageList[index].equals(language)) break;
        }
        if(index<languageList.length) {
            index = (index + 1)%languageList.length;
        }
        language = languageList[index];
        save();
    }
}
