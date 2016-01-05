package com.mcminos.game;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelsConfig {
    private ArrayList<LevelCategory> categories = new ArrayList<>();

    /**
     * Init this configuration from the given levellist
     * @param levelListFile
     */
    public LevelsConfig(String levelListFile) {
        BufferedReader br = new BufferedReader(Gdx.files.internal(levelListFile).reader());
        try {
            for(String line = br.readLine();  line != null; line = br.readLine() ) {
                if(line.length() > 0) { // ignore comments and empty lines
                    line = line.split(";")[0];
                }
                line = line.trim();
                if(line.length() > 0) {
                    new LevelConfig(this, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int size() {
        return categories.size();
    }

    public LevelCategory get(int i) {
        return categories.get(i);
    }

    public LevelCategory getCategory(String categoryId) {
        int index = findCategoryIndexById(categoryId);
        if(index>=0) { // found
            return get(index);
        }
        return null;
    }

    private int findCategoryIndexById(String categoryId) {
        int index;
        for(index = categories.size()-1; index>=0; index--) {
            if(categories.get(index).getId().equals(categoryId)) {
                break;
            }
        }
        return index;
    }

    public LevelConfig getLevel(String name) {
        String split[] = name.split("/"); // TODO: also catch wrong names?
        String categoryId = split[0];
        LevelCategory c = getCategory(categoryId);
        String levelId = split[1];
        LevelConfig l = c.getLevel(levelId);
        return l;
    }

    public void add(LevelCategory category) {
        categories.add(category);
    }



/*    public void read(Json json, JsonValue jsonData) {
        ArrayList<JsonValue> list = json.readValue("categories",ArrayList.class,jsonData);
        categories = new ArrayList<>();
        for (JsonValue v : list) {
            categories.add(json.readValue(LevelCategory.class, v));
        }
        for( int nr=0; nr<categories.size(); nr ++) {
            categories.get(nr).init(this,nr);
        }
    }*/
}
