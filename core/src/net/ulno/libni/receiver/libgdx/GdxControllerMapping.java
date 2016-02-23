package net.ulno.libni.receiver.libgdx;

/**
 * Created by ulno on 23.02.16.
 */
public class GdxControllerMapping extends Mapping {
    @Override
    public GdxControllerMapping addButton(int libniButton, int keycode) {
        return (GdxControllerMapping) super.addButton(libniButton, keycode);
    }

    @Override
    public GdxControllerMapping addButtonFromAnalog(int libniButton, int analogNr, boolean positive) {
        return (GdxControllerMapping) super.addButtonFromAnalog(libniButton, analogNr, positive);
    }

    @Override
    public GdxControllerMapping addAnalog(int libniAnalog, int analogNr) {
        return (GdxControllerMapping) super.addAnalog(libniAnalog, analogNr);
    }
}

