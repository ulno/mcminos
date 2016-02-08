package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by ulno on 06.02.16.
 * <p>
 * receive control-commands via UDP
 * eventually also send status updates back
 */
public class GameNetController {
    public static final int NUMBER_OF_BUTTONS = 256;
    public static final int NUMBER_OF_BUTTON_BYTES = (NUMBER_OF_BUTTONS + 7) / 8;
    public static final int NUMBER_OF_AXIS = 16;
    public static final int NUMBER_OF_AXIS_BYTES = NUMBER_OF_AXIS * 2;
    private Thread receiverThread;
    private byte message[] = new byte[NUMBER_OF_BUTTON_BYTES + NUMBER_OF_AXIS_BYTES];

    private boolean buttonStates[];
    private int analogStates[];
    private GameNetControllerListener listener;
    private static int MESSAGE_BUFFER_SIZE = 0x10000;
    private byte[] messageBuffer = new byte[MESSAGE_BUFFER_SIZE];

    private DatagramSocket socket = null;
    private DatagramPacket incoming = new DatagramPacket(messageBuffer, messageBuffer.length);
    private boolean finished = false;

    private boolean getButtonFromMessage(int nr) {
        int byteNr = nr / 8;
        int bitNr = nr % 8;
        return (message[byteNr] & (1 << bitNr)) > 0;
    }

    private int getAxisFromMessage(int nr) {
        int byteNr = NUMBER_OF_BUTTON_BYTES + nr * 2;
        byte high = message[byteNr];
        byte low = message[byteNr + 1];
        return ((high >= 128) ? (256 - high) : high) * 256 + low; // little endian two -complement
    }

    /**
     * @param port on which port to listen, if <= 0 don't activate
     */
    GameNetController(int port) {
        for(int i=0; i<message.length; i++) message[i] = 0; // init
        buttonStates = new boolean[NUMBER_OF_BUTTONS];
        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) buttonStates[i] = false;
        analogStates = new int[NUMBER_OF_AXIS];
        for (int i = 0; i < NUMBER_OF_AXIS; i++) analogStates[i] = 0;
        if (port > 0) {
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(500);
            } catch (Exception e) {
                Gdx.app.log("GameNetController", "Can't open socket.", e);
            }

            receiverThread = new Thread() {
                @Override
                public void run() {
                    if (socket != null) {
                        while (!finished) {
                            try {
                                //System.out.println("Trying to receive...");
                                socket.receive(incoming);
                                //System.out.println("Received sth. Length: " + incoming.getLength());
                                if (incoming.getLength() == message.length) {
                                    // seems correct, so save it and overwrite whatever was received before
                                    for (int i = message.length - 1; i >= 0; i--) message[i] = messageBuffer[i];
                                }
                            } catch (IOException e) {
                                //System.out.println("...receive... failed");
                                //e.printStackTrace(); // might just have timed out
                            }
                        }
                    }

                }
            };
            receiverThread.start();

        }
    }

    /**
     * This will actually call the listener, if something has changed
     */
    public void evaluateMessages() {
        // see if packages arrived
        if (socket != null && listener != null) { // if initialized and somebody is subscribed
            // parse last message received and compare with actual states
            for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
                boolean pressed = getButtonFromMessage(i);
                if (pressed != buttonStates[i]) {
                    buttonStates[i] = pressed;
                    if (pressed) listener.gameNetDown((char) i);
                    else listener.gameNetUp((char) i);
                }
            }
            for (int i = 0; i < NUMBER_OF_AXIS; i++) {
                int newState = getAxisFromMessage(i);
                if (newState != analogStates[i]) {
                    analogStates[i] = newState;
                    listener.gameNetAnalog((byte) i, newState);
                }
            }
        }
    }

    public void setListener(GameNetControllerListener listener) {
        this.listener = listener;
    }

    public boolean isUp(char b) {
        return !isDown(b);
    }

    public boolean isDown(char b) {
        return buttonStates[(byte) b];
    }

    public int getAnalog(byte analogNr) {
        return analogStates[analogNr];
    }

    public void dispose() {
        clearListener();
        finished = true; //stop thread
        try {
            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        //System.out.println("Disconnected");
    }

    public void clearListener() {
        listener = null;
    }
}
