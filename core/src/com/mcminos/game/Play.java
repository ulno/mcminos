package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 10.09.15.
 */
public class Play implements Screen, GestureListener, InputProcessor {
    private final SpriteBatch menubatch;
    private final OrthographicCamera camera;
    private Game game;
    private Table menuTable;
    private Table toolboxTable;
    private Window toolbox;
    private Label bombLabel;
    private Label dynamiteLabel;
    private Label landmineLabel;
    private Label chocolateLabel;
    private Label pillLabel;
    private Label keyLabel;
    private Label rockmeLabel;
    private Label umbrellaLabel;
    private TextButton doorUpButton;
    private TextButton doorRightButton;
    private TextButton doorDownButton;
    private TextButton doorLeftButton;
    private PlayWindow playwindow;
    private final Skin skin;
    private McMinos mcminos;
    private final Audio audio;
    private final BitmapFont font;
    private final SpriteBatch batch;
    private final Main main;
    private Level level;
    private Stage menu;
    private int touchDownX;
    private int touchDownY;
    private long lastZoomTime = 0;
    private int gameResolutionCounter = 0;
    private Label medicineLabel;
    Graphics background;

    public Play(final Main main, String levelName) {
        this.main = main;
        batch = main.getBatch();
        camera = new OrthographicCamera();
        font = main.getFont();
        skin = main.getSkin();
        audio = main.getAudio();
        menubatch = new SpriteBatch(); // don't conflict with gaming batch
        init(levelName);
    }

    public void init(String levelName) {
        game = new Game(main, this, camera);
//        background = Entities.backgrounds_hexagon_03;
        background = Entities.backgrounds_pavement_04;
        game.disableMovement();
        game.currentLevelName = levelName;
        level = game.loadLevel(levelName);
        mcminos = game.getMcMinos();
        playwindow = game.getPlayWindow();

        // Basically, based on density, we want to set out default zoomlevel.
        playwindow.setResolution(gameResolutionCounter);

        // Init menu
        menu = new Stage(new ScreenViewport(), menubatch);
        menuTable = new Table();
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

        toolbox = new Window("Toolbox", skin);
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
        chocolateLabel = new Label("000", skin);
        toolboxTable.add(chocolateLabel);
        TextButton chocolateActivateButton = new TextButton("*", skin);
        chocolateActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasChocolate() && mcminos.getPoisonDuration() == 0) {
                    mcminos.decreaseChocolates();
                    mcminos.setPowerPillValues(2, 1, 10);
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        toolboxTable.add(chocolateActivateButton).prefSize(64).pad(2);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_key.getTexture(64, 0)));
        keyLabel = new Label("000", skin);
        toolboxTable.add(keyLabel);
        doorUpButton = new TextButton("^", skin);
        doorUpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().up());
            }
        });
        toolboxTable.add(doorUpButton).prefSize(64).pad(2);
        doorRightButton = new TextButton(">", skin);
        doorRightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().right());
            }
        });
        toolboxTable.add(doorRightButton).prefSize(64).pad(2);
        doorDownButton = new TextButton("v", skin);
        doorDownButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().down());
            }
        });
        toolboxTable.add(doorDownButton).prefSize(64).pad(2);
        doorLeftButton = new TextButton("<", skin);
        doorLeftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().left());
            }
        });
        toolboxTable.add(doorLeftButton).prefSize(64).pad(2);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_bomb_default.getTexture(64, 0)));
        bombLabel = new Label("000", skin);
        toolboxTable.add(bombLabel);
        TextButton bombDropButton = new TextButton("v", skin);
        toolboxTable.add(bombDropButton).prefSize(64).pad(2);
        TextButton bombActivateButton = new TextButton("*", skin);
        toolboxTable.add(bombActivateButton).prefSize(64).pad(2);
        bombDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasBomb()) {
                    mcminos.decreaseBombs();
                    mcminos.getLevelBlock().makeBomb();
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        bombActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasBomb()) {
                    mcminos.decreaseBombs();
                    new Explosion(mcminos.getLevelBlock(), LevelObject.Types.Bomb);
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_dynamite_default.getTexture(64, 0)));
        dynamiteLabel = new Label("000", skin);
        toolboxTable.add(dynamiteLabel);
        TextButton dynamiteDropButton = new TextButton("v", skin);
        toolboxTable.add(dynamiteDropButton).prefSize(64).pad(2);
        TextButton dynamiteActivateButton = new TextButton("*", skin);
        toolboxTable.add(dynamiteActivateButton).prefSize(64).pad(2);
        dynamiteDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasDynamite()) {
                    mcminos.decreaseDynamites();
                    mcminos.getLevelBlock().makeDynamite();
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        dynamiteActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasDynamite()) {
                    mcminos.decreaseDynamites();
                    new Explosion(mcminos.getLevelBlock(), LevelObject.Types.Dynamite);
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_land_mine_default.getTexture(64, 0)));
        landmineLabel = new Label("000", skin);
        toolboxTable.add(landmineLabel);
        TextButton landmineDropButton = new TextButton("v", skin);
        toolboxTable.add(landmineDropButton).prefSize(64).pad(2);
        TextButton landmineActivateButton = new TextButton("*", skin);
        toolboxTable.add(landmineActivateButton).prefSize(64).pad(2);
        landmineDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasLandmine()) {
                    mcminos.decreaseLandmines();
                    mcminos.getLevelBlock().makeLandMine();
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        landmineActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasLandmine()) {
                    mcminos.decreaseLandmines();
                    mcminos.getLevelBlock().makeLandMineActivated();
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_umbrella.getTexture(64, 0)));
        umbrellaLabel = new Label("000", skin);
        toolboxTable.add(umbrellaLabel);
        TextButton umbrellaDropButton = new TextButton("v", skin);
        toolboxTable.add(umbrellaDropButton).prefSize(64).pad(2);
        TextButton umbrellaActivateButton = new TextButton("*", skin);
        toolboxTable.add(umbrellaActivateButton).prefSize(64).pad(2);
        umbrellaActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasUmbrella()) {
                    mcminos.consumeUmbrella();
                    audio.soundPlay("wind");
                    mcminos.increaseScore(10);
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_medicine.getTexture(64, 0)));
        medicineLabel = new Label("000", skin);
        toolboxTable.add(medicineLabel);
        TextButton medicineDropButton = new TextButton("v", skin);
        toolboxTable.add(medicineDropButton).prefSize(64).pad(2);
        TextButton medicineActivateButton = new TextButton("*", skin);
        toolboxTable.add(medicineActivateButton).prefSize(64).pad(2);
        medicineActivateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasMedicine() && mcminos.getPoisonDuration() > 0) {
                    mcminos.consumeMedicine();
                    mcminos.increaseScore(10);
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });
        toolboxTable.row();

        Button leaveButton = new TextButton("Leave Level", skin);
        final Play thisScreen = this;
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleToolbox();
                backToMenu();
            }
        });
        toolboxTable.add(leaveButton).pad(4).prefSize(128, 64);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.pills_pill_default.getTexture(64, 0)));
        pillLabel = new Label("00000", skin);
        toolboxTable.add(pillLabel);
        toolboxTable.row();

        toolboxTable.add(new Image(Entities.extras_rock_me.getTexture(64, 0)));
        rockmeLabel = new Label("00000", skin);
        toolboxTable.add(rockmeLabel);
        toolboxTable.row();

        Button plusButton = new TextButton("+", skin);
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                zoomPlus();
            }
        });
        Button minusButton = new TextButton("-", skin);
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                zoomMinus();
            }
        });
        toolboxTable.add(plusButton).pad(4).prefSize(64, 64);
        toolboxTable.add(minusButton).pad(4).prefSize(64, 64);
        toolboxTable.row();

        toolbox.add(toolboxTable);

        // InputProcessor
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(menu, gd, this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
    }

    public void backToMenu() {
        this.dispose();
        main.setScreen(new MainMenu(main, level.getLevelName()));
    }

    private void toggleDoor(LevelBlock lb) {
        if (lb.hasDoor()) {
            if (!lb.hasRock()) { // if the dor is not blocked by rock
                if (mcminos.hasKey()) {
                    mcminos.decreaseKeys();
                    lb.toggleDoor();
                    if (lb.hasClosedDoor()) // was opened
                        audio.soundPlay("rums");
                    else audio.soundPlay("quietsch");
                    toggleToolbox(); // close toolbox
                }
            }
        }
    }

    public void updateToolbox() {
        chocolateLabel.setText(Util.formatInteger(mcminos.getChocolates(), 3));
        keyLabel.setText(Util.formatInteger(mcminos.getKeys(),3));
        bombLabel.setText(Util.formatInteger(mcminos.getBombs(),3));
        dynamiteLabel.setText(Util.formatInteger(mcminos.getDynamites(),3));
        landmineLabel.setText(Util.formatInteger(mcminos.getLandmines(),3));
        umbrellaLabel.setText(Util.formatInteger(mcminos.getUmbrellas(),3));
        medicineLabel.setText(Util.formatInteger(mcminos.getMedicines(),3));
        pillLabel.setText(Util.formatInteger(level.getPillsNumber(),5));
        rockmeLabel.setText(Util.formatInteger(level.getRockmesNumber(),5));
        // update door buttons
        // first get mcminos' position
        LevelBlock mcmBlock = mcminos.getLevelBlock();
        if (mcmBlock != null) {
            if (mcminos.hasKey() && mcmBlock.up() != null && !mcmBlock.up().hasRock() && mcmBlock.up().hasDoor())
                doorUpButton.setDisabled(false);
            else doorUpButton.setDisabled(true);
            if (mcminos.hasKey() && mcmBlock.right() != null && !mcmBlock.up().hasRock() && mcmBlock.right().hasDoor())
                doorRightButton.setDisabled(false);
            else doorRightButton.setDisabled(true);
            if (mcminos.hasKey() && mcmBlock.down() != null && !mcmBlock.up().hasRock() && mcmBlock.down().hasDoor())
                doorDownButton.setDisabled(false);
            else doorDownButton.setDisabled(true);
            if (mcminos.hasKey() && mcmBlock.left() != null && !mcmBlock.up().hasRock() && mcmBlock.left().hasDoor())
                doorLeftButton.setDisabled(false);
            else doorLeftButton.setDisabled(true);
        }
    }

    public void toggleToolbox() {
        game.setToolboxShown(!game.isToolboxShown());
        if (playwindow.game.isToolboxShown())
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
        game.updateTime(); // TODO: so we would actually not need to track time here as moving is handled elsewhere
//        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        menubatch.begin();
        for( int x = 0; x < playwindow.getWidthInPixels() + playwindow.resolution; x += playwindow.resolution ) {
            for( int y = 0; y < playwindow.getHeightInPixels() + playwindow.resolution; y += playwindow.resolution ) {
                background.draw(playwindow,menubatch,x,y);
            }
        }
        menubatch.end();

        batch.setColor(Color.WHITE); // reset to full brightness as destroyed by menu
        batch.begin();

        ScissorStack.pushScissors(playwindow.getScissors());

        game.synchronizedDraw();

        batch.flush();
        ScissorStack.popScissors();
        batch.end(); // must end before menu

        updateToolbox();
        menubatch.begin();
        font.draw(menubatch,
                "S" + Util.formatInteger(mcminos.getScore(), 6)
                        + " P" + Util.formatInteger(mcminos.getPowerDuration() >> game.timeResolutionExponent, 3)
                        + " U" + Util.formatInteger(mcminos.getUmbrellaDuration() >> game.timeResolutionExponent, 3)
                        + " T" + Util.formatInteger(mcminos.getPoisonDuration() >> game.timeResolutionExponent, 2)
                        + " L" + Util.formatInteger(mcminos.getLives(), 2)
                        + (mcminos.isMirrored() ? " M" : ""),
                20, Gdx.graphics.getHeight() - 20);
        // " P%03d U%03d T%02d L%02d ",
        // add stage and menu
        menubatch.end();

        menu.act(delta);
        menu.draw();


    }

    @Override
    public void resize(int width, int height) {
        playwindow.resize(width, height);
        menuTable.setBounds(0, 0, width, height);
        //toolboxTable.setBounds(0, 0, width, height); no these are fixed in little window
        menu.getViewport().update(width, height, true);
        toolbox.setSize(width / 3, height * 4 / 5);
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
        menubatch.dispose();
        game.dispose();
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
        switch (character) {
            case '+':
                zoomPlus();
                playwindow.setResolution(gameResolutionCounter);
                break;
            case '-':
                zoomMinus();
                playwindow.setResolution(gameResolutionCounter);
                break;
            case 27:
            case ' ':
                toggleToolbox();
                break;
            case 'p':
            case 'P':
                // TODO: check if this enables to cheat
                game.disableMovement();
                break;
        }
        return false;
    }

    public void zoomPlus() {
        gameResolutionCounter--;
        if (gameResolutionCounter < 0) gameResolutionCounter = 0;
        playwindow.setResolution(gameResolutionCounter);
    }

    public void zoomMinus() {
        gameResolutionCounter++;
        if (gameResolutionCounter > Entities.resolutionList.length - 1)
            gameResolutionCounter = Entities.resolutionList.length - 1;
        playwindow.setResolution(gameResolutionCounter);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!mcminos.isKilled() && !mcminos.isFalling()) {
            game.enableMovement();
        }
        if (!game.isToolboxShown()) { // just pan in this case -> see there
            if (button > 0) return false;
            int x = windowToGameX(screenX);
            int y = windowToGameY(screenY);
            mcminos.setDestination(x, y);
            return false; // nnedsto be evtl. dealt with at drag
        }
        return false;
    }

    public int windowToGameY(int screenY) {
        int y = Util.shiftLeftLogical(Gdx.graphics.getHeight() - screenY - playwindow.getProjectionY(), (PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent))
                + playwindow.windowVPixelYPos - (PlayWindow.virtualBlockResolution >> 1); // flip windowVPixelYPos-axis
        //if(game.getScrollY()) { allways
        if (y >= playwindow.getVPixelsLevelHeight())
            y -= playwindow.getVPixelsLevelHeight();
        if (y <= -(playwindow.virtualBlockResolution >> 1))
            y += playwindow.getLevelHeight();
        //}
        //else {
        //    if( y >= game.windowVPixelYPos + game.getVisibleHeightInVPixels() - (game.virtualBlockResolution >> 1) )
        //        y = game.windowVPixelYPos + game.getVisibleHeightInVPixels() - (game.virtualBlockResolution >> 1);
        //}
        return y;
    }

    public int windowToGameX(int screenX) {
        // map windowVPixelXPos windowVPixelYPos to game coordinates
        // TODO: consider only first button/finger
        int x = Util.shiftLeftLogical(screenX-playwindow.getProjectionX(), PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent)
                + playwindow.windowVPixelXPos - (PlayWindow.virtualBlockResolution >> 1);
        //if(game.getScrollX()) { allways do this
        if (x >= playwindow.getVPixelsLevelWidth())
            x -= playwindow.getVPixelsLevelWidth();
        if (x <= -(playwindow.virtualBlockResolution >> 1))
            x += playwindow.getVPixelsLevelWidth();
        //}
        //else {
        //    if( x >= game.windowVPixelXPos + game.getVisibleWidthInVPixels() - (game.virtualBlockResolution >> 1) )
        //        x = game.windowVPixelXPos + game.getVisibleWidthInVPixels() - (game.virtualBlockResolution >> 1);
        //}
        return x;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return touchDown(screenX, screenY, pointer, 0); // Forward to touch
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if( amount > 0) {
            zoomMinus();
            return true;
        }
        else if(amount < 0)  {
            zoomPlus();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return touchDown((int) x, (int) y, pointer, button); // Forward to touch
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (button>0 || count > 1) {
            toggleToolbox();
            return true;
        }
        return touchDown(x, y, 0, button);
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
        if (playwindow.game.isToolboxShown()) {
            int dxi = Util.shiftLeftLogical((int) deltaX, PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent);
            int dyi = Util.shiftLeftLogical((int) deltaY, PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent);
            playwindow.windowVPixelXPos = (playwindow.windowVPixelXPos + playwindow.getVPixelsLevelWidth() - dxi) % playwindow.getVPixelsLevelWidth();
            playwindow.windowVPixelYPos = (playwindow.windowVPixelYPos + playwindow.getVPixelsLevelHeight() + dyi) % playwindow.getVPixelsLevelHeight();
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
        if (game.getGameTime() - lastZoomTime > 500) { // ignore some events
            if (initialDistance > distance + playwindow.visibleHeightInPixels / 4) {
                zoomMinus();
            } else if (initialDistance < distance - playwindow.visibleHeightInPixels / 4) {
                zoomPlus();
            }
            lastZoomTime = game.getGameTime();
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

}
