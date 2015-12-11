package com.mcminos.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelsConfig {
    private ArrayList<LevelSet> sets;

    public LevelsConfig() {
    }

    public int size() {
        return sets.size();
    }

    public LevelSet get(int i) {
        return sets.get(i);
    }
}
