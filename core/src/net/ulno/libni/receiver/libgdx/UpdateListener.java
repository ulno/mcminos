package net.ulno.libni.receiver.libgdx;

/**
 * Created by ulno on 22.02.16.
 */
public interface UpdateListener {
    public void buttonUpdated(int buttonNr, boolean pressed);
    public void analogUpdated(int analogNr, long value);
}