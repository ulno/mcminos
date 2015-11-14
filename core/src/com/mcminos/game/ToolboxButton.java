package com.mcminos.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

/**
 * Created by ulno on 13.11.15.
 */
public class ToolboxButton  {

    private final Toolbox toolbox;
    private final Graphics gfx;
    private final Group button;
    private final int myIndex;
    private Label label = null;
    private SegmentString text;
    private boolean visible = true;
    private boolean lastVisible = false;

    public ToolboxButton(Toolbox toolbox, Graphics gfx, int labelLength, EventListener listener) {
        this.toolbox = toolbox;
        this.gfx = gfx;
        myIndex = toolbox.addButton( this );
        button = new Group();
        if(labelLength>0) {
            text = new SegmentString(labelLength);
            label = new Label(text.getStringBuilder(), toolbox.getSkin());
        }
        button.addListener(listener);
    }

    public void resize(int resolution) {
        button.clearChildren();
        button.setSize(resolution, resolution);
        Image image = new Image(gfx.getTexture(resolution, 0));
        button.addActor(image);
        if(label != null) {
            label.setPosition(resolution / 2, resolution / 2, Align.center);
            button.addActor(label);
        }
    }

    public SegmentString getText() {
        return text;
    }

    public void update() {
        if(label != null) {
            label.setText(text.getStringBuilder());
        }
    }

    /**
     * set new value
     * @param input
     * @return did vidibility change
     */
    public boolean setValue(int input) {
        boolean retvalue = false;
        text.writeInteger(input);
        if(input == 0) { // decide if visible
            button.setColor(0,0,0,0);
            visible = false;
        } else {
            button.setColor( 1,1,1,1 );
            visible = true;
        }
        update();
        if( lastVisible != visible) {
            retvalue = true;
        }
        lastVisible = visible;
        return retvalue;
    }

    public Group getActor() {
        return button;
    }

    public boolean isGreater(ToolboxButton other) {
        if(visible) {
            if( other.visible ) return myIndex > other.myIndex;
            return false;
        }
        if( other.visible ) return true;
        return false;
    }

    public boolean isVisible() {
        return visible;
    }
}
