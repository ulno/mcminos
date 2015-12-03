package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;

/**
 * Created by ulno on 27.08.15.
 *
 * This is the Main class from where the game is controlled.
 *
 */
public class Main extends com.badlogic.gdx.Game {
    private Game game;
    private SpriteBatch batch;
    private Skin skin;
    private Audio audio;
    public static final String DEFAULT_UISKIN = "uiskins/default/uiskin.json";
    public static final String DEFAULT_FONT = "liberation-sans";
    private HashMap<Integer,BitmapFont> fontList = new HashMap<>();
    private int symbolResolution;

    @Override
	public void create () {
        Gdx.graphics.setVSync(true); // try some magic on the desktop TODO: check if this has any effect
        audio = new Audio();
        loadFont(32);
        loadFont(64);
        loadFont(128);
        skin = new Skin( Gdx.files.internal(DEFAULT_UISKIN) );
        batch = new SpriteBatch();
        //  Basically, based on density and screensize, we want to set our default zoomlevel.
        // densityvalue is BS float density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI
        // let's do everything based on width and height - we assume width>height
        if(Game.preferencesHandle.contains("sr")) {
            symbolResolution = Game.preferencesHandle.getInteger("sr");
        } else { // guess something reasonable
            symbolResolution = Math.max(16,
                    Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 8
            );
            int closestPowerOf2 = 16;
            int nearest = 16;
            while (closestPowerOf2 <= 128) {
                if (Math.abs(symbolResolution - closestPowerOf2) < Math.abs(symbolResolution - nearest)) {
                    nearest = closestPowerOf2;
                }
                closestPowerOf2 *= 2;
            }
            symbolResolution = nearest;
        }
        this.setScreen(new Load(this));
    }

    public int getSymbolResolution() {
        return symbolResolution;
    }

    public void setSymbolResolution(int symbolResolution) {
        if(symbolResolution < 16) symbolResolution = 16;
        if(symbolResolution > 128) symbolResolution = 128;
        this.symbolResolution = symbolResolution;
    }

    private void loadFont(int res) {
        String fontName = "fonts/" + DEFAULT_FONT + "-" + res + ".fnt";
        fontList.put(res,new BitmapFont(Gdx.files.internal(fontName)));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
	public void render () {
        super.render();
	}

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
        for(BitmapFont f: fontList.values()) {
            f.dispose();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont(int res) {
        return fontList.get(res);
    }

    public Skin getSkin() {
        return skin;
    }

    public Audio getAudio() {
        return audio;
    }
}
