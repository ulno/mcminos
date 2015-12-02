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
    private Ghosts ghosts;
    private EventManager eventManager;
    private long gameFrame;

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
        json.writeValue("l",game.getLevel());
        json.writeValue("m",game.getMcMinos());
        json.writeValue("g",game.getGhosts());
        json.writeValue("f",game.getTimerFrame());
        json.writeValue("e",game.getEventManager());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        level = json.readValue("l",Level.class,jsonData);
        mcminos = json.readValue("m",McMinos.class,jsonData);
        ghosts = json.readValue("g",Ghosts.class,jsonData);
        gameFrame = json.readValue("f",Long.class,jsonData);
        eventManager = json.readValue("e",EventManager.class,jsonData);
    }

    public Level getLevel() {
        return level;
    }

    public McMinos getMcminos() {
        return mcminos;
    }

    public Ghosts getGhosts() {
        return ghosts;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public long getGameFrame() {
        return gameFrame;
    }
}
