package com.mcminos.game;

import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelConfig {
    private String id;
    private HashMap<String, String> title;
    private HashMap<String, String> body;

    public LevelConfig() {
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
}
