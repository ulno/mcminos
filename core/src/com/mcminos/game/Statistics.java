package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.util.ObjectMap;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.Key;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * used for recording progression and statisticsof levels
 * Created by ulno on 20.12.15.
 */
public class Statistics {
    private final Main main;
    private final FileHandle saveFile;

    HashMap<String, LevelStatistics> statsRecord;

    /* kryo and crypto init */
    private static final String ALGORITHM = "Blowfish";
    private final Key secretKey = new SecretKeySpec("stats.mcminos.ulno.net".getBytes(), ALGORITHM);
    private Cipher cipher;
    private Kryo kryo;

    public Statistics(Main main, FileHandle saveFile) {
        this.main = main;
        this.saveFile = saveFile;
        initKryo();
        if (!load()) {
            statsRecord = new HashMap<>(); // reinit, if file can't be read
        }
    }

    public void activate(LevelConfig newLevel) {
        String name = newLevel.getName();
        if (!statsRecord.containsKey(name)) {
            statsRecord.put(name, new LevelStatistics());
        }
        save(); // TODO: save only when necessary
    }

    public boolean activated(LevelConfig level) {
        String name = level.getName();
        LevelsConfig lsc = main.getLevelsConfig();
        LevelConfig lc = lsc.getLevel(name);
        String activated = lc.getCategory().getActivated();
        switch(activated) {
            case "all":
                return true;
            case "first":
                if(lc.getNr() == 0){
                    return true;
                }
                break;
            // when none, just see, if it was activateby finishing last category
        }

        if (statsRecord.containsKey(name)) {
            return statsRecord.get(name).isActivated();
        }
        return false;
    }

    public void update(LevelConfig level, long newTime) {
        String name = level.getName();
        statsRecord.get(name).update(newTime);
        save();
    }

    /**
     * Init cryptographic variables and Kryo for load and save
     */
    void initKryo() {
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (Exception e) {
            Gdx.app.log("exception in initKryo", e.toString());
        }
        kryo = new Kryo();
        //Log.DEBUG();
    }


    public void save() {
        // TODO: make backup and build fallback
        FileHandle fh = saveFile;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            Output output = new Output(new BufferedOutputStream(new DeflaterOutputStream(new CipherOutputStream(fh.write(false), cipher))));

            kryo.writeObject(output, statsRecord);

            output.close();
        } catch (Exception e) {
            Gdx.app.log("exception in save stats", e.toString());
        }
    }

    public boolean load() {
        FileHandle fh = saveFile;

        // check if the save-game exists
        if (fh.exists()) {
            try {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                Input input = new Input(new BufferedInputStream(new InflaterInputStream(new CipherInputStream(fh.read(), cipher))));

                // set context for kryo
                ObjectMap context = kryo.getContext();
                context.put("main", main); // is needed in level

                // restore the state
                statsRecord = kryo.readObject(input, HashMap.class);

                input.close();
            } catch (Exception e) {
                Gdx.app.log("Unable to load stats file", e.toString());
                return false; // not successful
            }
        } else {
            return false; // doesn't exist, can't load
        }
        return true; // success
    }
}
