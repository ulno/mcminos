package net.ulno.libni.receiver.libgdx;

import java.util.ArrayList;

/**
 * Class needs to be extended for the rigth controller type (Keyboard, GdxController, Network - udp, tcp, or MQTT)
 * Created by ulno on 22.02.16.
 */
public abstract class LibniController {
    public static final int NUMBER_OF_BUTTONS = 256;
    public static final int NUMBER_OF_ANALOGS = 16;

    Mapping mapping;
    UpdateListener updateListener = null;

    private boolean[] buttons = new boolean[NUMBER_OF_BUTTONS];
    private long[] analogs = new long[NUMBER_OF_ANALOGS];

    public void init(Mapping mapping) {
        for(int i = 0; i > NUMBER_OF_BUTTONS; i++) {
            buttons[i] = false;
        }
        for(int i = 0; i > NUMBER_OF_ANALOGS; i++) {
            analogs[i] = 0;
        }
        this.mapping = mapping;
    }

    public LibniController() {
        Mapping mapping = new Mapping();
        mapping.initDefault();
        init(mapping);
    }

    public LibniController(Mapping mapping) {
        init(mapping);
    }

    void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    void clearUpdateListener() {
        updateListener = null;
    }

    /**
     * This should usually be overwritten by the extending class
     *
     * @param unmappedButtonNr
     * @return
     */
    protected abstract boolean unmappedButtonPressed( int unmappedButtonNr );

    protected abstract long unmappedAnalog( int unmappedAnalogNr );

    /**
     * This is called when there was a button change happening through a registered event
     * @param buttonNr
     * @param pressed button changed to pressed-state
     */
    private void buttonUpdated( int buttonNr, boolean pressed ) {
        if(updateListener != null) {
            updateListener.buttonUpdated( buttonNr, pressed );
        }
    }

    /**
     * needs to be called when an event was triggered for this
     * @param unmappedButtonNr
     */
    public void registerButtonEvent( int unmappedButtonNr ) {
        int button = mapping.getButton(unmappedButtonNr);
        if(button < 0) return; // is not handled
        ArrayList<Integer> triggers = mapping.getTriggers(button);
        boolean currentState = buttons[button];
        boolean newState = false;
        if (triggers != null) for (int i = triggers.size() - 1; i >= 0; i--) {
            if (unmappedButtonPressed(triggers.get(i))) { // if any is pressed, say button is pressed
                newState = true;
                break;
            }
        }
        if(!newState) {
            // check also the analogs to compute state
            ArrayList<Integer> analogs = mapping.getAnalogs(button);
            if(analogs!=null) for (int i = analogs.size() - 1; i >= 0; i--) {
               newState = unmappedAnalogToButton(analogs.get(i), button);

            }
        }

        if(newState != currentState) {
            buttons[button] = newState;
            buttonUpdated( button, newState );
        }
    }

    private boolean unmappedAnalogToButton(int analogNr, int buttonNr) {
        long analog = unmappedAnalog(analogNr);
        if(analog != 0) {
            if (mapping.isAnalogPositive(analogNr)) {
                return analog > 0;
            } else {
                return analog < 0;
            }
        }
        return false;
    }

    public void registerAnalogEvent( int unmappedAnalogNr ) {
        // TODO: else anything analog is lost
    }

    public void evaluate() {
        // usually empty but called from render to do some cooperative multitasking tasks here
    }

    public void dispose() {
        clearUpdateListener();
    }

    public boolean getButton(int button) {
        return buttons[button];
    }

    public long getAnalog(int analog) {
        return analogs[analog];
    }
}
