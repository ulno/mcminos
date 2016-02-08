package com.mcminos.game;

/**
 * Created by ulno on 06.02.16.
 */
public interface GameNetControllerListener {
    /**
     * This is called, when a button was pressed and going down.
     * @param button
     */
    public void gameNetDown(char button );

    /**
     * This is called, when a button is released and going up.
     * @param button
     */
    public void gameNetUp(char button );

    /**
     * a 2 byte analog value is sent for controller with nr analogNr
     * values from -2^15 to 2^15-1
     */
    public void gameNetAnalog(byte analogNr, int value);
}
