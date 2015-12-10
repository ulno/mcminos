package com.mcminos.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelSet {
    private String name;
    private String path;
    private ArrayList<LevelConfig> levels;

    public LevelSet() {
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public LevelConfig get(int i) {
        return  levels.get(i);
    }

    public int size() {
        return  levels.size();
    }
}
