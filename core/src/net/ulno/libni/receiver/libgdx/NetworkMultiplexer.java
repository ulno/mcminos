package net.ulno.libni.receiver.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by ulno on 22.02.16.
 */
public class NetworkMultiplexer extends LibniController implements UpdateListener  {
    public static final int MAX_BUFFER_SIZE = 128;
    public static final int BUFFER_HEADER_SIZE=16;
    public static final int MAX_NETWORK_CONTROLLERS=128;
    private Hashtable<NetworkControllerID,NetworkController> networkControllers = new Hashtable<>();
    private ArrayList<NetworkControllerID> networkControllerIDList = new ArrayList<>();
    private Random random = new Random();

    private DatagramChannel channel = null;
    private Thread receiverThread;

    private ByteBuffer messageBuffer = ByteBuffer.allocateDirect(MAX_BUFFER_SIZE);
    private byte messageSaved[] = new byte[MAX_BUFFER_SIZE];
    private int messageSavedSize;

    private DatagramSocket socket = null;
    //private DatagramPacket incoming = new DatagramPacket(messageBuffer, messageBuffer.length);
    private boolean finished = false;

    public NetworkMultiplexer(FileHandle file) {
        // init with contents of an external file
        int port = -1;

        if(file.exists()) {
            try {
                port = Integer.valueOf(new BufferedReader(file.reader()).readLine().trim());
            } catch (Exception e) {
                //ignore and leave hostAndPort undefined
            }
        }
        initUDP(port);

    }

    void initUDP(int port ) {
        if (port > 0) {
            try {
                //socket = new DatagramSocket(port);
                //socket.setSoTimeout(10);
                channel = DatagramChannel.open();
                channel.configureBlocking(false); // TODO: necessary?
                socket = channel.socket();
                socket.bind(new InetSocketAddress(port));
            } catch (Exception e) {
                Gdx.app.log("NetworkController", "Can't open socket.", e);
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

    void initTCP( int port ) {

    }

    void initMQTT( String host, int port, String topic ) {
    }

    /**
     * This needs to be called from render to prevent threading issues
     */
    NetworkControllerID evaluateID = new NetworkControllerID(); // declare here to prevent allocating memory all the time
    public void evaluate() {
        // Look at last received message
        // First analyze header -> and get id from there
        if(messageSaved[0]=='L' && messageSaved[1]=='B' && messageSaved[2] == 'N' && messageSaved[3] == 'I') { // Magic header correct
            long sessionID = messageSaved[8];
            sessionID = (sessionID<<8) + messageSaved[9];
            sessionID = (sessionID<<8) + messageSaved[10];
            sessionID = (sessionID<<8) + messageSaved[11];
            evaluateID.sessionID = sessionID;

            long clientID = messageSaved[12];
            clientID = (clientID<<8) + messageSaved[13];
            clientID = (clientID<<8) + messageSaved[14];
            clientID = (clientID<<8) + messageSaved[15];
            evaluateID.clientID = clientID;

            if(!networkControllers.containsKey(evaluateID)) { // creating new controller
                int index = networkControllerIDList.size();
                if(  index >= MAX_NETWORK_CONTROLLERS ) { // discard randomly one
                    index = random.nextInt(MAX_NETWORK_CONTROLLERS);
                    // delete element in hashmap
                    networkControllers.get(networkControllerIDList.get(index)).dispose();
                    networkControllers.remove(networkControllerIDList.get(index));
                }
                NetworkControllerID newID = new NetworkControllerID(sessionID, clientID);
                NetworkController controller = new NetworkController(newID);
                controller.setUpdateListener(this);
                if (networkControllerIDList.size() >= index) {
                    networkControllerIDList.add(newID);
                } else { // is in there
                    networkControllerIDList.set(index, newID);
                }
                networkControllers.put(newID, controller);
            }
            networkControllers.get(evaluateID).evaluateNetworkPackage(messageSaved, messageSavedSize);
        }
    }

    @Override
    protected boolean unmappedButtonPressed(int unmappedButtonNr) {
        return getButton(unmappedButtonNr);
    }

    @Override
    protected long unmappedAnalog(int unmappedAnalogNr) {
        return getAnalog(unmappedAnalogNr);
    }

    @Override
    public void dispose() {
        finished = true; //stop thread
        try {
            channel.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        //System.out.println("Disconnected");
        super.dispose();
    }

    @Override
    public boolean getButton(int button) {
        for(int i = networkControllerIDList.size()-1; i>=0; i--) {
            if(networkControllers.get(networkControllerIDList.get(i)).getButton(button))
                return true;
        }
        return false;
    }

    @Override
    public long getAnalog(int analogNr) {
        long analog=0;
        long analogAbs=0;
        for(int i = networkControllerIDList.size()-1; i>=0; i--) {
            NetworkController c = networkControllers.get(networkControllerIDList.get(i));
            long newAnalog = c.getAnalog(analogNr);
            long newAnalogAbs = Math.abs(newAnalog);
            if(newAnalogAbs > analogAbs) {
                analogAbs = newAnalogAbs;
                analog = newAnalog;
            }
        }
        return analog;
    }

    @Override
    public void buttonUpdated(int buttonNr, boolean pressed) {
        // just propagate down even if it might not be true
        if(updateListener != null)
            updateListener.buttonUpdated(buttonNr, pressed);
    }

    @Override
    public void analogUpdated(int analogNr, long value) {
        // just propagate down even if it might not be true
        if(updateListener != null)
            updateListener.analogUpdated(analogNr,value);
    }
}
