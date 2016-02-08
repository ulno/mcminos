package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by ulno on 06.02.16.
 *
 * receive control-commands via MQTT
 * eventually also send status updates back
 *
 */
public class MqttController {
    public static final String topicBase = "MqttController";
    public static int buttonCount = 256;
    private boolean buttonStates[];
    public static int analogCount = 256;
    private int analogStates[];
    private final String clientId;
    private final String topic;
    private MqttClient mqttClient;
    private MqttControllerListener listener;
    private static int MESSAGE_BUFFER_SIZE = 128;
    private byte[][] messageBuffer = new byte[MESSAGE_BUFFER_SIZE][];
    private int messageBufferFill = 0;

    /**
     *
     * @param topic
     * @param hostAndPort does not necessaryly need to include port (if not default is taken)
     */
    MqttController(String topic, String hostAndPort) {
        MemoryPersistence persistence = new MemoryPersistence();
        this.topic = topicBase + "/" + topic;
        clientId = MqttClient.generateClientId();
        buttonStates = new boolean[buttonCount];
        for(int i=0; i<buttonCount; i++) buttonStates[i]=false;
        analogStates = new int[analogCount];
        for(int i=0; i<analogCount; i++) analogStates[i]=0;
        if(!(hostAndPort==null || hostAndPort.equals(""))) {
            try {
                mqttClient = new MqttClient("tcp://" + hostAndPort, clientId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                mqttClient.connect(connOpts);

                mqttClient.subscribe(this.topic,1);

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("MQTT Connection lost!");
                        System.out.println(cause.fillInStackTrace());
                    }

                    @Override
                    synchronized public void messageArrived(String topic, MqttMessage message) throws Exception {
                        byte[] payload = message.getPayload();
                        if(payload.length==2 || payload.length==3) {
                            // store
                            messageBuffer[messageBufferFill] = payload;
                            messageBufferFill ++;
                        } else {
                            // unparsable
                            Gdx.app.log("MqttController", "unparseble MQTT message received: " + new String(message.getPayload()));
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });

/*            String content="internal test";
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            //message.setQos(qos);
            mqttClient.publish(topic, message);
            System.out.println("Message published"); */

            } catch (MqttException me) {
                System.out.println("reason " + me.getReasonCode());
                System.out.println("msg " + me.getMessage());
                System.out.println("loc " + me.getLocalizedMessage());
                System.out.println("cause " + me.getCause());
                System.out.println("excep " + me);
                me.printStackTrace();
            }
        }
    }

    public void evaluateMessages() {
        for(int i=0; i<messageBufferFill; i++) {
            byte[] payload = messageBuffer[i];
            switch (payload.length) {
                case 2: // This is a button event
                    switch (payload[0]) {
                        case 'd': // down
                            buttonStates[payload[1]] = true;
                            if (listener != null) {
                                listener.mqttDown((char) payload[1]);
                            }
                            break;
                        case 'u': // down
                            buttonStates[payload[1]] = false;
                            if (listener != null) {
                                listener.mqttUp((char) payload[1]);
                            }
                            break;
                    }
                    break;
                case 3: // an analog event
                    analogStates[payload[0]] = ((payload[1] >= 128) ? (256 - payload[1]) : payload[1]) * 256 + payload[2]; // little endian two -complement
                    if (listener != null) {
                        listener.mqttAnalog(payload[0], analogStates[payload[0]]);
                    }
                    break;
                default:
            }
            messageBufferFill = 0;
        }
    }

    public void setListener(MqttControllerListener listener) {
        this.listener  = listener;
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
        try {
            if(mqttClient != null)
                mqttClient.disconnect();
        } catch (MqttException e) {
            //e.printStackTrace();
        }
        //System.out.println("Disconnected");
    }

    public void clearListener() {
        listener = null;
    }
}
