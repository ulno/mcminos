package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelsConfig implements Json.Serializable {
    private ArrayList<LevelCategory> sets;

    public LevelsConfig() {
    }

    public int size() {
        return sets.size();
    }

    public LevelCategory get(int i) {
        return sets.get(i);
    }

    @Override
    public void write(Json json) {
        // wil not be called
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        ArrayList<JsonValue> list = json.readValue("sets",ArrayList.class,jsonData);
        sets = new ArrayList<>();
        for (JsonValue v : list) {
            sets.add(json.readValue(LevelCategory.class, v));
        }
        for( int nr=0; nr<sets.size(); nr ++) {
            sets.get(nr).init(this,nr);
        }
    }
}
