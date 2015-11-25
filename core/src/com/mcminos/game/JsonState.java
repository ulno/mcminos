package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by ulno on 19.11.15.
 */
public class JsonState implements Json.Serializable {
    private Game game;
    private Level level;
    private McMinos mcminos;

    public JsonState(Game game) {
        init(game);
    }

    public JsonState() {
        game = null;
    }

    public void init(Game game) {
        this.game = game;
    }

    @Override
    public void write(Json json) {
        json.writeValue("level",game.getLevel());
        json.writeValue("mcminos",game.getMcMinos());
        // Explosions?
        // Timingevents?
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        level = json.readValue("level",Level.class,jsonData);
        mcminos = json.readValue("mcminos",McMinos.class,jsonData);
    }

    public Level getLevel() {
        return level;
    }

    public McMinos getMcminos() {
        return mcminos;
    }
}
