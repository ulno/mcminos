package net.ulno.libni.receiver.libgdx;

import com.badlogic.gdx.Gdx;

/**
 * Created by ulno on 22.02.16.
 */
public class KeyboardLibniController extends LibniController {
    KeyboardLibniController() {
        super(new KeyboardMapping());
    }

    public KeyboardLibniController(Mapping keyboardMapping) {
        super(keyboardMapping);
    }

    @Override
    protected boolean unmappedButtonPressed(int keycode) {
        return Gdx.input.isKeyPressed(keycode);
    }

    @Override
    protected long unmappedAnalog(int unmappedAnalogNr) {
        return 0;
    }

}
