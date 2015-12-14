package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelConfig {
    private String id;
    private HashMap<String, String> title;
    private HashMap<String, String> body;
    private LevelCategory category;
    private int nr = -1;

    public LevelConfig() {
    }

    /**
     * is called after json-read to initialize
     * @param category
     * @param nr
     */
    public void init(LevelCategory category, int nr) {
        this.category = category;
        this.nr = nr;
    }

    public String getTitle(String lang) {
        if(!hasTitle(lang)) return null;
        return title.get(lang);
    }

    public boolean hasTitle(String lang) {
        return title.containsKey(lang);
    }

    public String getBody(String lang) {
        if(!hasBody(lang)) return null;
        return body.get(lang);
    }

    public boolean hasBody(String lang) {
        return body.containsKey(lang);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return category.getPath() + "/" + getId();
    }

    public int getCategoryNr() {
        return category.getNr();
    }

    public int getNr() {
        return nr;
    }

    public LevelConfig getNextLevel() {
        return category.get(nr+1);
    }
}
