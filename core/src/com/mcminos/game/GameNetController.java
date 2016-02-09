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
 * <p>
 * receive control-commands via UDP
 * eventually also send status updates back
 */
public class GameNetController {
    public static final int NUMBER_OF_BUTTONS = 256;
    public static final int NUMBER_OF_BUTTON_BYTES = (NUMBER_OF_BUTTONS + 7) / 8;
    public static final int NUMBER_OF_AXIS = 16;
    public static final int NUMBER_OF_AXIS_BYTES = NUMBER_OF_AXIS * 2;
    private DatagramChannel channel = null;
    private Thread receiverThread;
    private byte message[] = new byte[NUMBER_OF_BUTTON_BYTES + NUMBER_OF_AXIS_BYTES];

    private boolean buttonStates[];
    private int analogStates[];
    private GameNetControllerListener listener;
    private static int MESSAGE_BUFFER_SIZE = NUMBER_OF_BUTTON_BYTES + NUMBER_OF_AXIS_BYTES;
    //private byte[] messageBuffer = new byte[MESSAGE_BUFFER_SIZE];
    private ByteBuffer messageBuffer = ByteBuffer.allocateDirect(MESSAGE_BUFFER_SIZE); // if nto direct -> memory leak

    private DatagramSocket socket = null;
    //private DatagramPacket incoming = new DatagramPacket(messageBuffer, messageBuffer.length);
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
                                if( channel.receive(messageBuffer) != null) {
                                    messageBuffer.flip();
                                    //System.out.println("Received sth. Length: " + messageBuffer.remaining());
                                    messageBuffer.get(message);
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
