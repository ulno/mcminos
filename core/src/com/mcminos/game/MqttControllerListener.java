package com.mcminos.game;

/**
 * Created by ulno on 06.02.16.
 */
public interface MqttControllerListener {
    /**
     * This is called, when a button was pressed and going down.
     * @param button
     */
    public void mqttDown( char button );

    /**
     * This is called, when a button is released and going up.
     * @param button
     */
    public void mqttUp( char button );

    /**
     * a 2 byte analog value is sent for controller with nr analogNr
     * values from -2^15 to 2^15-1
     */
    public void mqttAnalog(byte analogNr, int value);
}
