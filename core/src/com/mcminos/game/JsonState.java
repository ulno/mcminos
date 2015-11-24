package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by ulno on 19.11.15.
 */
public class JsonState implements Json.Serializable {
    private final Game game;

    public JsonState(Game game) {
        this.game = game;
    }

    @Override
    public void write(Json json) {

    }

    @Override
    public void read(Json json, JsonValue jsonData) {

    }
}
