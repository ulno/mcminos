package com.mcminos.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by ulno on 08.01.16.
 */
public class SymbolButton {
    private int res;
    private TextureRegion symbol;
    private Group group;
    private Image bottom,top,selectLayer;
    private boolean selected = false;

    public void setSymbol(TextureRegion symbol) {
        group.clearChildren();
        this.symbol = symbol;
        bottom = new Image(Entities.menu_button_empty_bottom.getTexture(res,0));
        bottom.setSize(res, res);
        group.addActor(bottom);
        selectLayer = new Image(Entities.menu_button_empty_bottom.getTexture(res,0));
        selectLayer.setSize(res, res);
        group.addActor(bottom);
        if (symbol != null) {
            Image middle = new Image(symbol);
            middle.setSize(res, res);
            group.addActor(middle);
        }
        top = new Image(Entities.menu_button_empty_top.getTexture(res,0));
        top.setSize(res, res);
        group.addActor(top);
        group.addActor(selectLayer);
        colorNormal();
    }

    public void setSymbol(int res, TextureRegion symbol) {
        this.res = res;
        setSymbol(symbol);
    }

    public SymbolButton(int res, TextureRegion symbol) {
        group = new Group();
        group.setSize(res,res);

        setSymbol(res, symbol);

        group.addListener(new ClickListener() {
            private boolean touchDown = false;
            private boolean entered = false;

            private void selectColor() {
                if (touchDown && entered) {
                    colorHighlight();
                } else if (selected) {
                    colorSelect();
                } else {
                    colorNormal();
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                touchDown = false;
                selectColor();
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touchDown = true;
                selectColor();
                super.touchDown(event, x, y, pointer, button);
                return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                entered = true;
                selectColor();
                //super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                entered = false;
                selectColor();
                //super.exit(event, x, y, pointer, toActor);
            }

/*            @Override
            public void clicked(InputEvent event, float x, float y) {
                touchDown = false;
                entered = false;
                super.clicked(event, x, y);
            }*/
        });
    }

    public void addListener(EventListener listener) {
        group.addListener(listener);
    }

    public Group getCell() {
        return group;
    }

    public void select() {
        selected = true;
        colorSelect();
    }

    public void unselect() {
        selected = false;
        colorNormal();
    }

    private void colorSelect() {
        bottom.setColor(0.6f, 0, 0, 1);
    }

    private void colorNormal() {
        selectLayer.setVisible(false);
        selectLayer.setColor(0, 0, 0, 0);
        bottom.setColor(1,1,1,1);
    }

    private void colorHighlight() {
        selectLayer.setColor(1, 0, 0, 0.4f);
        selectLayer.setVisible(true);
    }

}
