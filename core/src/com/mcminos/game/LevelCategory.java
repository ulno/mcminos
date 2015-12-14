package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelCategory implements Json.Serializable {
    private String name;
    private String path;
    private HashMap<String, String> endmessage = null;
    private ArrayList<LevelConfig> levels;
    private int nr = -1;
    private LevelsConfig levelsConfig;

    public LevelCategory() {
    }

    /**
     * After json read
     */
    public void init(LevelsConfig lc, int categoryNr) {
        levelsConfig = lc;
        this.nr = categoryNr;
        for( int nr=0; nr<levels.size(); nr++) {
            levels.get(nr).init(this,nr);
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public LevelConfig get(int i) {
        if(levels.size() > i)
            return  levels.get(i);
        else
            return null;
    }

    public int size() {
        return  levels.size();
    }

    @Override
    public void write(Json json) {
        // will not be used as thisis read-only
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        name = json.readValue("name",String.class, jsonData);
        path = json.readValue("path", String.class, jsonData);
        endmessage = json.readValue("endmessage", HashMap.class, jsonData);

        ArrayList<JsonValue> list = json.readValue("levels",ArrayList.class,jsonData);
        levels = new ArrayList<>();
        for (JsonValue v : list) {
            levels.add(json.readValue(LevelConfig.class, v));
        }


    }

    public int getNr() {
        return nr;
    }
}
