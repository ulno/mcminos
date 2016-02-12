package com.mcminos.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by ulno on 06.02.16.
 * <p/>
 * receive control-commands via UDP
 * eventually also send status updates back
 */
public class GameNetController {
    public static final int NUMBER_OF_BUTTONS = 256;
    public static final int NUMBER_OF_AXIS = 16;
    public static final int MAX_BUFFER_SIZE = 128;
    private DatagramChannel channel = null;
    private Thread receiverThread;

    private boolean buttonStates[];
    private int analogStates[];
    private GameNetControllerListener listener;
    private ByteBuffer messageBuffer = ByteBuffer.allocateDirect(MAX_BUFFER_SIZE);
    private byte messageSaved[] = new byte[MAX_BUFFER_SIZE];
    private int messageSavedSize;

    private DatagramSocket socket = null;
    //private DatagramPacket incoming = new DatagramPacket(messageBuffer, messageBuffer.length);
    private boolean finished = false;

    /**
     * @param port on which port to listen, if <= 0 don't activate
     */
    GameNetController(int port) {
        buttonStates = new boolean[NUMBER_OF_BUTTONS];
        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) buttonStates[i] = false;
        analogStates = new int[NUMBER_OF_AXIS];
        for (int i = 0; i < NUMBER_OF_AXIS; i++) analogStates[i] = 0;
        if (port > 0) {
            try {
                //socket = new DatagramSocket(port);
                //socket.setSoTimeout(10);
                channel = DatagramChannel.open();
                channel.configureBlocking(false); // TODO: necessary?
                socket = channel.socket();
                socket.bind(new InetSocketAddress(port));
            } catch (Exception e) {
                Gdx.app.log("GameNetController", "Can't open socket.", e);
            }

            receiverThread = new Thread() {
                @Override
                public void run() {
                    if (channel != null) {
                        while (!finished) {
                            try {
                                //System.out.println("Trying to receive...");
                                //socket.receive(incoming);
                                messageBuffer.clear();
                                if (channel.receive(messageBuffer) != null) {
                                    messageBuffer.flip();
                                    //System.out.println("Received sth. Length: " + messageBuffer.remaining());
                                    messageSavedSize = messageBuffer.remaining();
                                    if(messageSavedSize <= MAX_BUFFER_SIZE)
                                        messageBuffer.get(messageSaved, 0, messageSavedSize);
                                }
                            } catch (Exception e) {
                                //System.out.println("...receive... failed");
                                //e.printStackTrace(); // might just have timed out
                            }
                            try {
                                Thread.sleep(10); // prevent busy wait and race
                            } catch (InterruptedException e) {


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
        if (channel != null && listener != null) { // if initialized and somebody is subscribed
            // parse last message received and compare with actual states
            int pointer = 0;
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
                            listener.gameNetAnalog(button, newState);
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
            if (newState) listener.gameNetUp((char) buttonNr);
            else listener.gameNetDown((char) buttonNr);
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
            channel.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        //System.out.println("Disconnected");
    }

    public void clearListener() {
        listener = null;
    }
}
