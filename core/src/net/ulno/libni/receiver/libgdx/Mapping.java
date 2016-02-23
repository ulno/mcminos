package net.ulno.libni.receiver.libgdx;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulno on 22.02.16.
 */
public class Mapping {
    HashMap<Integer,Integer> keycodeToLibniButton = new HashMap<>();
    HashMap<Integer,ArrayList<Integer>> libniButtonToKeycodes = new HashMap<>();
    HashMap<Integer,Integer> analogToLibniButton = new HashMap();
    HashMap<Integer,Boolean> analogToLibniButtonSelect = new HashMap();
    HashMap<Integer,ArrayList<Integer>> libniButtonToAnalogs = new HashMap<>();
    HashMap<Integer,Integer> analogToLibniAnalog = new HashMap();
    HashMap<Integer,ArrayList<Integer>> libniAnalogToAnalogs = new HashMap<>();

    public Mapping addButton( int libniButton, int keycode ) {
        if(!keycodeToLibniButton.containsKey(keycode)) {
            ArrayList<Integer> list;

            keycodeToLibniButton.put(keycode, libniButton);
            if (libniButtonToKeycodes.containsKey(libniButton)) {
                // add to existing list
                list = libniButtonToKeycodes.get(libniButton);
                list.add(keycode);
            } else {
                // create new list
                list = new ArrayList<Integer>();
                list.add(keycode);
                libniButtonToKeycodes.put(libniButton, list);
            }
        } else {
            Gdx.app.log("Mapping.addButton","Button "+keycode+" already assigned.");
        }
        return this;
    }

    /**
     * Map an analog value to a button
     *
     * @param libniButton
     * @param analogNr
     * @param positive sepcify if this being positive is mapped to pressed (and if negative to unpressed) or vice versa
     */
    public Mapping addButtonFromAnalog(int libniButton, int analogNr, boolean positive) {
        if(!analogToLibniButton.containsKey(analogNr)) {
            ArrayList<Integer> list;

            analogToLibniButton.put(analogNr, libniButton);
            analogToLibniButtonSelect.put(analogNr, positive);
            if (libniButtonToAnalogs.containsKey(libniButton)) {
                // add to existing list
                list = libniButtonToAnalogs.get(libniButton);
                list.add(analogNr);
            } else {
                // create new list
                list = new ArrayList<Integer>();
                list.add(analogNr);
                libniButtonToAnalogs.put(libniButton, list);
            }
        } else {
            Gdx.app.log("Mapping.addButtonFromAnalog","Analog "+analogNr+" already assigned.");
        }
        return this;
    }

    public Mapping addAnalog( int libniAnalog, int analogNr ) {
        // TODO: implement!
        return this;
    }


    public void initDefault() {
        for(int i = 0; i< LibniController.NUMBER_OF_BUTTONS; i++ ) {
            addButton(i,i);
        }
        for(int i = 0; i< LibniController.NUMBER_OF_ANALOGS; i++ ) {
            addAnalog(i,i);
        }
    }

    public int getButton(int toMap) {
        if(!keycodeToLibniButton.containsKey(toMap)) return -1;
        return keycodeToLibniButton.get(toMap);
    }

    public ArrayList<Integer> getTriggers(int libniButton) {
        return libniButtonToKeycodes.get(libniButton);
    }

    public ArrayList<Integer> getAnalogs(int buttonNr) {
        return libniAnalogToAnalogs.get(buttonNr);
    }

    public boolean isAnalogPositive(int analogNr) {
        return analogToLibniButtonSelect.get(analogNr);
    }
}
