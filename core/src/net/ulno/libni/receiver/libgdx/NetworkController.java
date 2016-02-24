package net.ulno.libni.receiver.libgdx;

/**
 * Created by ulno on 06.02.16.
 *
 * receive control-commands via Network (udp, tcp, or mqtt)
 * eventually also send status updates back
 */
public class NetworkController extends LibniController {
    private boolean buttonStates[];
    private int analogStates[];
    NetworkControllerID id;

    public NetworkController(NetworkControllerID networkControllerID) {
        super();
        this.id = this.id;
        buttonStates = new boolean[NUMBER_OF_BUTTONS];
        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) buttonStates[i] = false;
        analogStates = new int[NUMBER_OF_ANALOGS];
        for (int i = 0; i < NUMBER_OF_ANALOGS; i++) analogStates[i] = 0;
    }

    /**
     * Get the network package which belongs to this NetworkController
     * This will actually call the listener, if something has changed
     */
    public void evaluateNetworkPackage(byte[] messageSaved, int messageSavedSize ) {
        // see if packages arrived
        if (updateListener != null) { // if initialized and somebody is subscribed
            int pointer = 0;
            // parse header
            pointer += NetworkMultiplexer.BUFFER_HEADER_SIZE; // TODO: do something here! Look at version information
            // parse last message received and compare with actual states
            boolean pressed;
            byte button;
            while (pointer <= messageSavedSize -2 ) {
                int event = messageSaved[pointer];
                pointer++;
                switch (event) {
                    case 1: // Button Up
                        buttonEvent(messageSaved[pointer], false);
                        pointer++;
                        break;
                    case 2: // Button Down
                        buttonEvent(messageSaved[pointer], true);
                        pointer++;
                        break;
                    case 3:
                        button = messageSaved[pointer];
                        pointer++;
                        byte high = messageSaved[pointer];
                        pointer++;
                        byte low = messageSaved[pointer];
                        int newState = ((high >= 128) ? (256 - high) : high) * 256 + low; // little endian two -complement
                        if (newState != analogStates[button]) {
                            analogStates[button] = newState;
                            registerAnalogEvent(button);
                        }
                        pointer++;
                }
            }
        }
    }

    private void buttonEvent(byte buttonNr, boolean newState) {
        boolean oldState = buttonStates[buttonNr];
        if (oldState != newState) {
            buttonStates[buttonNr] = newState;
            registerButtonEvent(buttonNr);
        }
    }


    @Override
    protected boolean unmappedButtonPressed(int unmappedButtonNr) {
        return buttonStates[unmappedButtonNr];
    }

    @Override
    protected long unmappedAnalog(int unmappedAnalogNr) {
        return analogStates[unmappedAnalogNr];
    }

}
