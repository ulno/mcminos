package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 10.09.15.
 */
public class Play implements Screen, GestureListener, InputProcessor {
    private final Root root;
    private final Skin skin;
    private final BitmapFont font;
    private final Table menuTable;
    private final Table toolboxTable;
    private final Window toolbox;
    private final Label bombLabel;
    private final Label dynamiteLabel;
    private final Label landmineLabel;
    private final Label chocolateLabel;
    private final Label pillLabel;
    private final Label keyLabel;
    private final Label rockmeLabel;
    private final Label umbrellaLabel;
    private final TextButton doorUpButton;
    private final TextButton doorRightButton;
    private final TextButton doorDownButton;
    private final TextButton doorLeftButton;
    private Stage menu;
    private int touchDownX;
    private int touchDownY;
    private long lastZoomTime = 0;


    public Play(String levelName) {
        root = Root.getInstance();
        skin = root.defaultSkin;
        font = root.defaultFont;
        root.currentLevelName = levelName;
        root.loadLevel( "levels/" + levelName + ".asx" );

        // Init menu
        menu = new Stage(new ScreenViewport(),root.batch);
        menuTable = new Table(skin);
        menuTable.setWidth(menu.getWidth());
        menuTable.align(Align.center | Align.top);
        TextButton toolboxButton = new TextButton("Toolbox", skin);
        toolboxButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleToolbox();
            }
        });

        menuTable.align(Align.topRight)
                .add(toolboxButton)
                .prefSize(128, 64)
                .padTop(16)
                .padRight(16);
        menuTable.setColor(new Color(1, 1, 1, 0.7f));
        menu.addActor(menuTable);

        toolbox = new Window("Toolbox",skin);
        toolbox.setMovable(true);
        toolbox.setResizable(false);
        //toolbox.setResizeBorder(8); // big border so it works also onphones
        // TODO: add x-close button
        toolbox.setSize(menu.getWidth() / 3, menu.getWidth() * 4 / 5);
        toolbox.setPosition(menu.getWidth(), 0, Align.bottomRight);
        toolbox.align(Align.topLeft); // stuff in here move to top left
        toolbox.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent
        toolbox.addListener(new DragListener() {
            float downx, downy;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //return super.touchDown(event, x, y, pointer, button);
                if (pointer == 0) {
                    downx = x;
                    downy = y;
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                //super.touchDragged(event, x, y, pointer);
                if (pointer == 0) {
                    float posx = toolbox.getX();
                    float posy = toolbox.getY();
                    toolbox.setPosition(posx - downx + x, posy - downy + y, Align.bottomLeft);
                }
            }
        });

        toolboxTable = new Table(skin);
        //toolboxTable.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent, sems to be applied already
        toolboxTable.setWidth(toolbox.getWidth());
        toolboxTable.align(Align.topLeft);

        toolboxTable.add(new Image(Entities.pills_power_pill_chocolate.getTexture(64, 0)));
        chocolateLabel = new Label( "000", skin);
        toolboxTable.add(chocolateLabel);
        TextButton chocolateActivateButton = new TextButton("*",skin);
        chocolateActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: activate Power
            }
        });
        toolboxTable.add(chocolateActivateButton).prefSize(64).pad(2);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_key.getTexture(64, 0)));
        keyLabel = new Label( "000", skin);
        toolboxTable.add(keyLabel);
        doorUpButton = new TextButton("^",skin);
        doorUpButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(root.mcminos.getLevelBlock().up());
            }
        });toolboxTable.add(doorUpButton).prefSize(64).pad(2);
        doorRightButton = new TextButton(">",skin);
        doorRightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(root.mcminos.getLevelBlock().right());
            }
        });
        toolboxTable.add(doorRightButton).prefSize(64).pad(2);
        doorDownButton = new TextButton("v",skin);
        doorDownButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(root.mcminos.getLevelBlock().down());
            }
        });
        toolboxTable.add(doorDownButton).prefSize(64).pad(2);
        doorLeftButton = new TextButton("<",skin);
        doorLeftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(root.mcminos.getLevelBlock().left());
            }
        });
        toolboxTable.add(doorLeftButton).prefSize(64).pad(2);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_bomb_default.getTexture(64, 0)));
        bombLabel = new Label( "000", skin);
        toolboxTable.add(bombLabel);
        TextButton bombDropButton = new TextButton("v",skin);
        toolboxTable.add(bombDropButton).prefSize(64).pad(2);
        TextButton bombActivateButton = new TextButton("*",skin);
        toolboxTable.add(bombActivateButton).prefSize(64).pad(2);
        bombDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if( root.bombs > 0) {
                    root.bombs--;
                    root.mcminos.getLevelBlock().makeBomb();
                }
            }
        });
        bombActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: activate Bomb explosion
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_dynamite_default.getTexture(64, 0)));
        dynamiteLabel = new Label( "000", skin);
        toolboxTable.add(dynamiteLabel);
        TextButton dynamiteDropButton = new TextButton("v",skin);
        toolboxTable.add(dynamiteDropButton).prefSize(64).pad(2);
        TextButton dynamiteActivateButton = new TextButton("*",skin);
        toolboxTable.add(dynamiteActivateButton).prefSize(64).pad(2);
        dynamiteDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(root.dynamites > 0) {
                    root.dynamites--;
                    root.mcminos.getLevelBlock().makeDynamite();
                }
            }
        });
        dynamiteActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new Explosion(root.mcminos.getLevelBlock(), LevelObject.Types.Dynamite);
                //TODO: decrease dynamite
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_land_mine_default.getTexture(64, 0)));
        landmineLabel = new Label( "000", skin);
        toolboxTable.add(landmineLabel);
        TextButton landmineDropButton = new TextButton("v",skin);
        toolboxTable.add(landmineDropButton).prefSize(64).pad(2);
        TextButton landmineActivateButton = new TextButton("*",skin);
        toolboxTable.add(landmineActivateButton).prefSize(64).pad(2);
        landmineDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if( root.landmines > 0 ) {
                    root.landmines--;
                    root.mcminos.getLevelBlock().makeLandMine();
                }
            }
        });
        landmineActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if( root.landmines > 0 ) {
                    root.landmines--;
                    root.mcminos.getLevelBlock().makeLandMineActivated();
                }
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_umbrella.getTexture(64, 0)));
        umbrellaLabel = new Label( "000", skin);
        toolboxTable.add(umbrellaLabel);
        TextButton umbrellaDropButton = new TextButton("v",skin);
        toolboxTable.add(umbrellaDropButton).prefSize(64).pad(2);
        TextButton umbrellaActivateButton = new TextButton("*",skin);
        toolboxTable.add(umbrellaActivateButton).prefSize(64).pad(2);
        umbrellaActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: activate Umbrella
            }
        });
        toolboxTable.row();

        Button leaveButton = new TextButton("Leave Level",skin);
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleToolbox();
                Root.setScreen(new MainMenu());
            }
        });
        toolboxTable.add(leaveButton).pad(4).prefSize(128, 64);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.pills_pill_default.getTexture(64, 0)));
        pillLabel = new Label( "00000", skin);
        toolboxTable.add(pillLabel);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_rock_me.getTexture(64, 0)));
        rockmeLabel = new Label( "00000", skin);
        toolboxTable.add(rockmeLabel);
        toolboxTable.row();

        Button plusButton = new TextButton("+",skin);
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                zoomPlus();
            }
        });
        Button minusButton = new TextButton("-",skin);
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                zoomMinus();
            }
        });
        toolboxTable.add(plusButton).pad(4).prefSize(64, 64);
        toolboxTable.add(minusButton).pad(4).prefSize(64,64);
        toolboxTable.row();

        toolbox.add(toolboxTable);

        // InputProcessor
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(menu,gd,this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
    }

    private void toggleDoor(LevelBlock lb) {
        if( lb.hasDoor() ) {
            if( ! lb.hasRock() ) { // if the dor is not blocked by rock
                if (root.keys > 0) {
                    root.keys--;
                    lb.toggleDoor();
                    if (lb.hasClosedDoor()) // was opened
                        root.soundPlay("rums");
                    else root.soundPlay("quietsch");
                    toggleToolbox(); // close toolbox
                }
            }
        }
    }

    public void updateToolbox() {
        chocolateLabel.setText(String.format("%03d", root.chocolates));
        keyLabel.setText(String.format("%03d", root.keys));
        bombLabel.setText(String.format("%03d", root.bombs));
        dynamiteLabel.setText(String.format("%03d", root.dynamites));
        landmineLabel.setText(String.format("%03d", root.landmines));
        umbrellaLabel.setText(String.format("%03d", root.umbrellas));
        pillLabel.setText(String.format("%05d", root.level.getPillsNumber()));
        rockmeLabel.setText(String.format("%05d", root.level.getRockmesNumber()));
        // update door buttons
        // first get mcminos' position
        LevelBlock mcmBlock = root.mcminos.getLevelBlock();
        if(mcmBlock != null) {
            if (root.keys > 0 && mcmBlock.up() != null && !mcmBlock.up().hasRock() && mcmBlock.up().hasDoor()) doorUpButton.setDisabled(false);
            else doorUpButton.setDisabled(true);
            if (root.keys > 0 && mcmBlock.right() != null && !mcmBlock.up().hasRock() && mcmBlock.right().hasDoor()) doorRightButton.setDisabled(false);
            else doorRightButton.setDisabled(true);
            if (root.keys > 0 && mcmBlock.down() != null && !mcmBlock.up().hasRock() && mcmBlock.down().hasDoor()) doorDownButton.setDisabled(false);
            else doorDownButton.setDisabled(true);
            if (root.keys > 0 && mcmBlock.left() != null && !mcmBlock.up().hasRock() && mcmBlock.left().hasDoor()) doorLeftButton.setDisabled(false);
            else doorLeftButton.setDisabled(true);
        }
    }

    public void toggleToolbox() {
        root.setToolboxShown(!root.isToolboxShown());
        if (root.isToolboxShown())
            menu.addActor(toolbox);
        else
            toolbox.remove();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        double deltaTime = Gdx.graphics.getDeltaTime();
        //double offsetX = -0.2;
        //double offsetY = -0.5;

        // moving is not handled in rendering method
        //
        root.updateTime(); // TODO: so we would actually not need to track time here as moving is handled elsewhere
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        root.batch.setColor(Color.WHITE); // reset to full brightness as destroyed by menu
        root.batch.begin();
        try {
            root.updateLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LevelObject.drawAll();
        root.updateLock.release(); // TODO: think about moving this to the end of draw

        updateToolbox();
        root.defaultFont.draw(root.batch, String.format("Score %06d",root.score), 20, Gdx.graphics.getHeight() - 20);
        // add stage and menu
        root.batch.end(); // must end before menu

        menu.act(delta);
        menu.draw();


    }

    @Override
    public void resize(int width, int height) {
        root.resize(width, height);
        menuTable.setBounds(0, 0, width, height);
        //toolboxTable.setBounds(0, 0, width, height); no these are fixed in little window
        menu.getViewport().update(width, height, true);
        toolbox.setSize(width/3, height*4/5);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        menu.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        switch( character) {
            case '+':
                zoomPlus();
                break;
            case '-':
                zoomMinus();
                break;
        }
        root.setResolution( Entities.resolutionList[root.gameResolutionCounter] );
        return false;
    }

    public void zoomPlus() {
        root.gameResolutionCounter --;
        if (root.gameResolutionCounter < 0) root.gameResolutionCounter = 0;
        root.setResolution(Entities.resolutionList[root.gameResolutionCounter]);
    }

    public void zoomMinus() {
        root.gameResolutionCounter ++;
        if (root.gameResolutionCounter > Entities.resolutionList.length - 1)
            root.gameResolutionCounter = Entities.resolutionList.length - 1;
        root.setResolution(Entities.resolutionList[root.gameResolutionCounter]);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if( ! root.isToolboxShown() ) { // just pan in this case -> see there
            if (pointer > 0) return false;
            int x = windowToGameX(screenX);
            int y = windowToGameY(screenY);
            root.setDestination(x, y);
            return false; // nnedsto be evtl. dealt with at drag
        }
        return false;
    }

    public int windowToGameY(int screenY) {
        int y = Util.shiftLeftLogical(Gdx.graphics.getHeight() - screenY, (root.virtualBlockResolutionExponent - root.resolutionExponent))
                + root.windowVPixelYPos - (root.virtualBlockResolution >> 1); // flip windowVPixelYPos-axis
        //if(root.getScrollY()) { allways
        if (y >= root.getVPixelsLevelHeight())
            y -= root.getVPixelsLevelHeight();
        if (y <= -(root.virtualBlockResolution >> 1))
            y += root.getLevelHeight();
        //}
        //else {
        //    if( y >= root.windowVPixelYPos + root.getWindowVPixelHeight() - (root.virtualBlockResolution >> 1) )
        //        y = root.windowVPixelYPos + root.getWindowVPixelHeight() - (root.virtualBlockResolution >> 1);
        //}
        return y;
    }

    public int windowToGameX(int screenX) {
        // map windowVPixelXPos windowVPixelYPos to game coordinates
        // TODO: consider only first button/finger
        int x = Util.shiftLeftLogical(screenX, root.virtualBlockResolutionExponent - root.resolutionExponent) + root.windowVPixelXPos - (root.virtualBlockResolution >> 1);
        //if(root.getScrollX()) { allways do this
        if (x >= root.getVPixelsLevelWidth())
            x -= root.getVPixelsLevelWidth();
        if (x <= -(root.virtualBlockResolution >> 1))
            x += root.getVPixelsLevelWidth();
        //}
        //else {
        //    if( x >= root.windowVPixelXPos + root.getWindowVPixelWidth() - (root.virtualBlockResolution >> 1) )
        //        x = root.windowVPixelXPos + root.getWindowVPixelWidth() - (root.virtualBlockResolution >> 1);
        //}
        return x;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return touchDown(screenX,screenY,pointer,1); // Forward to touch
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        /*// quick hack to allow touch events
        if(velocityX < 0) {
            keyTyped('-');
            return true;
        }
        else if(velocityX>0) {
            keyTyped('+');
            return true;
        }
        return true;*/
        return false;
    }

    @Override
    public boolean pan(float screenX, float screenY, float deltaX, float deltaY) {
        if( root.isToolboxShown() ) {
            int dxi = Util.shiftLeftLogical((int) deltaX, root.virtualBlockResolutionExponent - root.resolutionExponent);
            int dyi = Util.shiftLeftLogical((int) deltaY, root.virtualBlockResolutionExponent - root.resolutionExponent);
            root.windowVPixelXPos = (root.windowVPixelXPos + root.getVPixelsLevelWidth() - dxi) % root.getVPixelsLevelWidth();
            root.windowVPixelYPos = (root.windowVPixelYPos + root.getVPixelsLevelHeight() + dyi) % root.getVPixelsLevelHeight();
            return true;
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if( root.gameTime - lastZoomTime > 500 ) { // ignore some events
            if( initialDistance > distance + root.windowPixelHeight /4) {
                zoomMinus();
            }
            else if( initialDistance < distance - root.windowPixelHeight /4) {
                zoomPlus();
            }
            lastZoomTime = root.gameTime;
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

}
