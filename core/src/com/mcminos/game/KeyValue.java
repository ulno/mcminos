package com.mcminos.game;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by ulno on 15.12.15.
 */
public class KeyValue {
    public String key;
    public String value;

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * read it from a text file
     *
     * @param br
     */
    public KeyValue(BufferedReader br) {
        String line = null;
        key = null;
        value = null;
        while (true) { // try until key found or eof
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                } else {
                    String split[] = line.split(";");
                    if(split.length>0) {
                        line = split[0]; // cut off comments
                        line = line.trim(); // remove whitespace
                        if (line.length() > 0 && !line.startsWith(";")) {
                            String[] strList = line.split("\\s*:\\s*");
                            if (strList.length > 0) { // ignore, if it cannot be parsed and continue
                                key = strList[0].trim();
                                if (strList.length > 1) { // if two params,  set key value, else continue until # or eof
                                    value = strList[1].trim();
                                    break;
                                } else {
                                    while ((line = br.readLine()) != null && !line.startsWith("#")) {
                                        if (value == null) {
                                            value = line; // here, we must not trim as formatting might be important
                                        } else {
                                            value = value + "\n" + line; // and no trimming here either
                                        }
                                    }
                                    if(value==null) value = "";
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Gdx.app.log("KeyValue","caught exception",e);
            }
        }
    }
}
