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
    private final String saveFileName;
    private final static int nrOfFileHandles = 5;
    private final FileHandle[] saveFiles = new FileHandle[nrOfFileHandles];

    /* Saved data */
    private HashMap<String, LevelStatistics> statsRecord;
    private LevelConfig lastLevel = null;

    /* kryo and crypto init */
    private static final String ALGORITHM = "Blowfish";
    private final Key secretKey = new SecretKeySpec(
            "mcmstat.ulno.net".getBytes(), ALGORITHM); // must have a length of power of 2
    private Cipher cipher;
    private Kryo kryo;

    public Statistics(Main main, String saveFileName) {
        this.main = main;
        this.saveFileName = saveFileName;
        for(int i=0; i<nrOfFileHandles; i++) {
            saveFiles[i] = Gdx.files.local(saveFileName+i);
        }
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
        lastLevel = newLevel;
        save(); // save always to remember last played level
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
            // when none, just see, if it was activated by finishing last category
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
        // forget oldest and move others
        for(int i=nrOfFileHandles-1; i>=1; i--) {
            try {
                saveFiles[i-1].moveTo(saveFiles[i]);
            } catch (Exception e) {
                Gdx.app.log("save stats", "can't move file trying next.");
            }
        }

        // try now to save current
        FileHandle fh = saveFiles[0];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            Output output = new Output(new BufferedOutputStream(new DeflaterOutputStream(new CipherOutputStream(fh.write(false), cipher))));

            kryo.writeObject(output, statsRecord);
            getLastLevel(); // make sure it's initialized
            kryo.writeObject(output, lastLevel.getCategory().getId());
            kryo.writeObject(output, lastLevel.getId());

            output.close();
        } catch (Exception e) {
            Gdx.app.log("exception in save stats", e.toString());
        }
    }

    public boolean load() {
        for( int i=0; i<nrOfFileHandles; i++) {
            FileHandle fh = saveFiles[i];

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
                    try {
                        String categoryId = kryo.readObject(input, String.class);
                        String levelId = kryo.readObject(input, String.class);
                        lastLevel = main.getLevelsConfig().getCategory(categoryId).getLevel(levelId);
                    } catch (Exception e) {
                        Gdx.app.log("exception in load stats", "Unable to load lastLevel, using first as default " + i, e);
                        lastLevel = null;
                        getLastLevel(); //set to sth reasonable
                    }
                    input.close();
                    return true;
                } catch (Exception e) {
                    Gdx.app.log("exception in load stats", "Unable to load stats file "+i, e);
                    getLastLevel(); // make sure it is not null
                    // try next
                }
            }
        }
        return false; // none worked
    }

    public LevelConfig getLastLevel() {
        if(lastLevel == null) {
            lastLevel = main.getLevelsConfig().get(0).get(0); //get first
        }
        return lastLevel;
    }
}
