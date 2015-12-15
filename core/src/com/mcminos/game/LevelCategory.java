package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 09.12.15.
 */
public class LevelCategory {
    private String name;
    private String id;
    private HashMap<String, String> endmessage = new HashMap<>();
    private ArrayList<LevelConfig> levels = new ArrayList<>();
    private int nr = -1;
    private LevelsConfig levelsConfig;
    private Graphics gfx = null;
    private int gfxStep = 0;


    public LevelCategory(LevelsConfig lc, String id) {
        this.levelsConfig = lc;
        this.nr = lc.size();
        name = id;        // do some reasonable defaults
        lc.add(this); // add yourself
        this.levelsConfig = lc;
        this.id = id; // id equals path
        // read info file
        FileHandle fh = Gdx.files.internal("levels/" + id + "/info");
        if (fh.exists()) {
            BufferedReader br = new BufferedReader(fh.reader());
            KeyValue kv;
            while ((kv = new KeyValue(br)).key != null) {
                switch (kv.key) {
                    case "name":
                        name = kv.value;
                        break;
                    case "gfx":
                        if(kv.value.contains(",")) {
                            String split[] = kv.value.split(",");
                            gfx = Graphics.getByName(split[0].trim());
                            gfxStep = Integer.parseInt(split[1].trim());
                        } else {
                            gfx = Graphics.getByName(kv.value);
                            gfxStep = 0;
                        }
                        break;
                    case "end":
                        endmessage.put("en", kv.value);
                        break;
                    default:
                        if (kv.key.startsWith("end-")) {
                            String lang = kv.key.substring(4);
                            endmessage.put(lang, kv.value);
                        }
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public LevelConfig get(int i) {
        if (levels.size() > i)
            return levels.get(i);
        else
            return null;
    }

    public int size() {
        return levels.size();
    }


    public int getNr() {
        return nr;
    }

    public void add(LevelConfig levelConfig) {
        levels.add(levelConfig);
    }

    public LevelsConfig getLevelsConfig() {
        return levelsConfig;
    }

    public Graphics getGfx() {
        return gfx;
    }

    public TextureRegion getTexture(int res) {
        if(gfx != null) {
            return gfx.getTextureDirectStep(res, gfxStep);
        }
        return null;
    }
}
