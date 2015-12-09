package com.mcminos.game;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Created by ulno on 13.11.15.
 */
public class ToolboxButton  {

    private final Toolbox toolbox;
    private final Table table;
    private Graphics gfx;
    private final Group button;
    private final int myIndex;
    private Label label = null;
    private SegmentString text;
    private boolean visible = true;
    private boolean lastVisible = true; // we start with all visible (last and actual have to be the same in the start)
    private int lastResolution;

    public ToolboxButton(Toolbox toolbox, Graphics gfx, int labelLength, EventListener listener) {
        this.toolbox = toolbox;
        this.table = toolbox.getTable();
        this.gfx = gfx;
        myIndex = toolbox.addButton( this );
        button = new Group();
        if(labelLength>0) {
            text = new SegmentString(labelLength);
            label = new Label(text.getStringBuilder(), toolbox.getLevelSkin());
        }
        button.addListener(listener);
    }

    public void rebuildButton(int resolution) {
        this.lastResolution = resolution;
        button.clearChildren();
        button.setSize(resolution, resolution);
        Image image = new Image(gfx.getTexture(resolution, 0));
        button.addActor(image);
        if (label != null) {
            label = new Label(text.getStringBuilder(), toolbox.getLevelSkin()); // resolution could have changed
            int w = (int) label.getWidth();
            int h = (int) label.getHeight();
            label.setPosition((resolution - w) / 2, (resolution - h) / 2);
            button.addActor(label);
        }
    }

    private void rebuildButton() {
        rebuildButton(lastResolution);
    }


    public SegmentString getText() {
        return text;
    }

    public void updateText() {
        if(label != null) {
            label.setText(text.getStringBuilder());
        }
    }

    /**
     * set new value
     * @param input
     * @return did visibility change
     */
    public boolean setValue(int input) {
        text.writeInteger(input);
        updateText();
        if(input == 0) { // decide if visible
            return hide();
        } else {
            return show();
        }
    }

    /**
     * @return did visibility change
     */
    public boolean show() {
        if( lastVisible == false ) {
            //   button.setColor( 1,1,1,1 );
            visible = true;
            lastVisible = true;
            return true;
        }
        return false;
    }

    /**
     * @return did visibility change
     */
    public boolean hide() {
        if (lastVisible == true) {
            //  button.setColor(0,0,0,0);
            visible = false;
            lastVisible = false;
            return true;
        }
        return false;
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

    /**
     * @return did graphics change
     */
    public boolean setGraphics(Graphics gfx) {
        if(gfx != this.gfx) {
            this.gfx = gfx;
            rebuildButton();
            return true;
        }
        return false;
    }

    public Cell<Group> addToTable() {
        return table.add(getActor());
    }
}
