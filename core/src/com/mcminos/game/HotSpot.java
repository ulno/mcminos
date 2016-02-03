package com.mcminos.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

/**
 * Created by ulno on 03.02.16.
 */
public class HotSpot {
    private int activateHint;
    private Actor actor;
    private HotSpot up;
    private HotSpot right;
    private HotSpot down;
    private HotSpot left;
    private ScrollPane scrollPane;

    private void update(Actor actor, ScrollPane scrollPane, int activateHint) {
        this.actor = actor;
        this.scrollPane = scrollPane;
        this.activateHint = activateHint;
    }

    public HotSpot(Actor actor, ScrollPane scrollPane, int activateHint) {
        up = null;
        right = null;
        down = null;
        left = null;
        update(actor, scrollPane, activateHint);
    }

    public HotSpot setUp(HotSpot hs) {
        this.up = hs;
        return hs;
    }

    public HotSpot setRight(HotSpot hs) {
        this.right = hs;
        return hs;
    }

    public HotSpot setDown(HotSpot hs) {
        this.down = hs;
        return hs;
    }

    public HotSpot setLeft(HotSpot hs) {
        this.left = hs;
        return hs;
    }

    public Actor getActor() {
        return actor;
    }

    public HotSpot getUp() {
        return up;
    }

    public HotSpot getCreateUp(Actor actor, ScrollPane scrollPane, int activateHint) {
        if(up == null) {
            up = new HotSpot(actor, scrollPane, activateHint);
            up.setDown( this );
        } else {
            up.update(actor, scrollPane, activateHint);
        }
        return up;
    }

    public HotSpot getRight() {
        return right;
    }

    public HotSpot getCreateRight(Actor actor, ScrollPane scrollPane, int activateHint) {
        if(right == null) {
            right = new HotSpot(actor, scrollPane, activateHint);
            right.setLeft( this );
        } else {
            right.update(actor, scrollPane, activateHint);
        }
        return right;
    }

    public HotSpot getDown() {
        return down;
    }

    public HotSpot getCreateDown(Actor actor, ScrollPane scrollPane, int activateHint) {
        if(down == null) {
            down = new HotSpot(actor, scrollPane, activateHint);
            down.setUp(this);
        } else {
            down.update(actor, scrollPane, activateHint);
        }
        return down;
    }

    public HotSpot getLeft() {
        return left;
    }

    public HotSpot getCreateLeft(Actor actor, ScrollPane scrollPane, int activateHint) {
        if(left == null) {
            left = new HotSpot(actor, scrollPane, activateHint);
            left.setRight( this );
        } else {
            left.update(actor, scrollPane, activateHint);
        }
        return left;
    }

    public int getActivateHint() {
        return activateHint;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
