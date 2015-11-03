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
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
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
    //private Window toolboxTable;
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
    private Touchpad touchpad;
    private Table toolbox;
    private ScrollPane toolboxScroller;
    private Group chocolatesButton;
    private Group keysButton;
    private Group dynamitesButton;
    private Group bombsButton;
    private Group landminesButton;
    private Group umbrellasButton;
    private Group medicinesButton;
    private Group menuButton;
    private TextButton menuButtonImage;
    private Image chocolatesImage;


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
//        background = Entities.backgrounds_pavement_04;
        background = Entities.backgrounds_punched_plate_02;
        game.disableMovement();
        game.currentLevelName = levelName;
        level = game.loadLevel(levelName);
        mcminos = game.getMcMinos();
        playwindow = game.getPlayWindow();

        // Basically, based on density, we want to set out default zoomlevel.
        playwindow.setResolution(gameResolutionCounter);

        // Init menu
        menu = new Stage(new ScreenViewport(), menubatch);
        /*menuTable = new Table();
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
        menu.addActor(menuTable);*/

        /*toolbox = new Window("Toolbox", skin);

        toolbox.setMovable(false);
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
        });*/


        toolbox = new Table(skin); // This is just the root of the toolbox, updated by resize
        toolbox.setPosition(0, 0);
        menu.addActor(toolbox);
        // In there, we need a table on a scrollable pane
        toolboxTable = new Table(skin);
        toolboxTable.setPosition(0,0,Align.top);
        toolboxScroller = new ScrollPane(toolboxTable);
        toolboxScroller.setScrollBarPositions(false, true);
        toolbox.setBackground(new NinePatchDrawable(skin.getPatch(("default-rect"))));
        toolbox.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent
        //toolboxTable.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent
//        toolbox.add(toolboxScroller).fill().expand().align(Align.top); // add this to root
        toolbox.add(toolboxScroller).top(); // add this to root
        toolbox.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                activateToolbox();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        ////////// Menu
        menuButton = new Group();
        menuButtonImage = new TextButton("Menu", skin);
        menuButtonImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deactivateToolbox();
                backToMenu();
            }
        });
        menuButton.addActor(menuButtonImage);
        toolboxTable.add(menuButton).row();


        /////////// chocolates
        chocolatesButton = new Group();
        chocolateLabel = new Label(Util.formatInteger(mcminos.getChocolates(), 2), skin);
        chocolatesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasChocolate() && mcminos.getPoisonDuration() == 0) {
                    mcminos.decreaseChocolates();
                    mcminos.setPowerPillValues(2, 1, 10);
                    deactivateToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });

        /////// keys
        keysButton = new Group();
        keyLabel = new Label(Util.formatInteger(mcminos.getKeys(), 2), skin);
        keysButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //super.clicked(event, x, y);
                openDoorDialog();
            }
        });

/*        doorUpButton = new TextButton("^", skin);
        doorUpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().up());
            }
        });
        //toolbox.add(doorUpButton).prefSize(64).pad(2);
        doorRightButton = new TextButton(">", skin);
        doorRightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().right());
            }
        });
//        toolbox.add(doorRightButton).prefSize(64).pad(2);
        doorDownButton = new TextButton("v", skin);
        doorDownButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().down());
            }
        });
//        toolbox.add(doorDownButton).prefSize(64).pad(2);
        doorLeftButton = new TextButton("<", skin);
        doorLeftButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDoor(mcminos.getLevelBlock().left());
            }
        });
//        toolbox.add(doorLeftButton).prefSize(64).pad(2);
        toolboxTable.row();*/

        /////// Bombs
        bombsButton = new Group();
        bombLabel = new Label(Util.formatInteger(mcminos.getBombs(), 2), skin);
        bombsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasBomb()) {
                    mcminos.decreaseBombs();
                    new Explosion(mcminos.getFromLevelBlock(), LevelObject.Types.Bomb);
                    deactivateToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });

        // TODO: work on drop only
/*        bombDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasBomb()) {
                    mcminos.decreaseBombs();
                    mcminos.getLevelBlock().makeBomb();
                    toggleToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });*/

        /////// Dynamites
        dynamitesButton = new Group();
        dynamiteLabel = new Label(Util.formatInteger(mcminos.getDynamites(), 2), skin);
        dynamitesButton.addActor(dynamiteLabel);

/*            dynamiteDropButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (mcminos.hasDynamite()) {
                        mcminos.decreaseDynamites();
                        mcminos.getLevelBlock().makeDynamite();
                        toggleToolbox(); // close toolbox
                    } else audio.soundPlay("error");
                }
            });*/
        dynamitesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasDynamite()) {
                    mcminos.decreaseDynamites();
                    new Explosion(mcminos.getFromLevelBlock(), LevelObject.Types.Dynamite);
                    deactivateToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });

        /////// Landmines
        landminesButton = new Group();
        landmineLabel = new Label(Util.formatInteger(mcminos.getLandmines(), 2), skin);
        landminesButton.addActor(landmineLabel);

            /*landmineDropButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (mcminos.hasLandmine()) {
                        mcminos.decreaseLandmines();
                        mcminos.getLevelBlock().makeLandMine();
                        toggleToolbox(); // close toolbox
                    } else audio.soundPlay("error");
                }
            });*/
        landminesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasLandmine()) {
                    mcminos.decreaseLandmines();
                    mcminos.getFromLevelBlock().makeLandMineActivated();
                    deactivateToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });

        /////// Umbrellas
        umbrellasButton = new Group();
        umbrellaLabel = new Label(Util.formatInteger(mcminos.getUmbrellas(), 2), skin);
        umbrellasButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasUmbrella()) {
                    mcminos.consumeUmbrella();
                    audio.soundPlay("wind");
                    mcminos.increaseScore(10);
                    deactivateToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });

        /////// Medicines
        medicinesButton = new Group();
        medicineLabel = new Label(Util.formatInteger(mcminos.getMedicines(), 2), skin);

        medicinesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasMedicine() && mcminos.getPoisonDuration() > 0) {
                    mcminos.consumeMedicine();
                    mcminos.increaseScore(10);
                    deactivateToolbox(); // close toolbox
                } else audio.soundPlay("error");
            }
        });


/*        toolbox.add(new Image(Entities.pills_pill_default.getTexture(64, 0)));
        pillLabel = new Label("00000", skin);
        toolbox.add(pillLabel);
        toolbox.row();

        toolbox.add(new Image(Entities.extras_rock_me.getTexture(64, 0)));
        rockmeLabel = new Label("00000", skin);
        toolbox.add(rockmeLabel);
        toolbox.row();

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
        toolbox.add(plusButton).pad(4).prefSize(64, 64);
        toolbox.add(minusButton).pad(4).prefSize(64, 64);
        toolbox.row();

        toolbox.add(toolbox); */



//        toolboxTable.setWidth(playwindow.resolution + 4);
//        toolboxTable.setHeight(playwindow.getHeightInPixels() + 4);

/*        // update door buttons
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
        }*/

        toolboxImages();
        toolboxUpdate();

        // virtual joystick (called touchpad in libgdx)
        touchpad = new Touchpad(32,skin);
        Color tpColor = touchpad.getColor();
        touchpad.setColor(tpColor.r,tpColor.g,tpColor.b,0.7f);
        touchpadResize();
        touchpad.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.isToolboxActivated()) {
                    mcminos.updateTouchpadDirections( touchpad.getKnobPercentX(), touchpad.getKnobPercentY() );
                    if (mcminos.getKeyDirections() > 0 && !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                        game.enableMovement();
                    }
                }
            }
        });
        menu.addActor( touchpad );

        // InputProcessor
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(menu, gd, this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
    }

    private void openDoorDialog() {
        Dialog d = new Dialog("Select door", skin);
        //mx= mcminos.getVX()
        //d.setPosition(mcminos.);
    }

    private void touchpadResize() {
        int width = Gdx.graphics.getWidth();
        int tpwidth = width / 4;
        int height = Gdx.graphics.getHeight();
        touchpad.setSize( tpwidth, tpwidth );
        touchpad.setDeadzone( tpwidth/5 );
        touchpad.setPosition( width * 3 / 4, 0 );
    }

    public void backToMenu() {
        this.dispose();
        main.setScreen(new MainMenu(main, level.getLevelName()));
    }

    private void toggleDoor(LevelBlock lb) {
        if (lb.hasDoor()) {
            if (!lb.hasRock()) { // if the door is not blocked by rock
                if (mcminos.hasKey()) {
                    mcminos.decreaseKeys();
                    lb.toggleDoor();
                    if (lb.hasClosedDoor()) // was opened
                        audio.soundPlay("rums");
                    else audio.soundPlay("quietsch");
                    deactivateToolbox(); // close toolboxTable
                }
            }
        }
    }

    public void toolboxResize() {
        int res = playwindow.resolution;
        // adjust size
        toolbox.setWidth(res + 4); // 4 for border
        toolbox.setHeight(playwindow.getHeightInPixels());
        toolboxImages();
    }

    public void toolboxImages() {
        int res = playwindow.resolution;

        menuButton.setSize(res, res);
        menuButtonImage.setSize(res-4,res-4);
        menuButtonImage.setPosition(res / 2, res / 2, Align.center);

        chocolatesButton.clearChildren();
        chocolatesButton.setSize(res, res);
        chocolatesImage = new Image(Entities.pills_power_pill_chocolate.getTexture(res, 0));
        chocolatesButton.addActor(chocolatesImage);
        chocolateLabel.setPosition(res / 2, res / 2, Align.center);
        chocolatesButton.addActor(chocolateLabel);

        keysButton.clearChildren();
        keysButton.setSize(res, res);
        keysButton.addActor(new Image(Entities.extras_key.getTexture(res, 0)));
        keyLabel.setPosition(res / 2, res / 2, Align.center);
        keysButton.addActor(keyLabel);

        bombsButton.clearChildren();
        bombsButton.setSize(res, res);
        bombsButton.addActor(new Image(Entities.extras_bomb_default.getTexture(res, 0)));
        bombLabel.setPosition(res / 2, res / 2, Align.center);
        bombsButton.addActor(bombLabel);

        dynamitesButton.clearChildren();
        dynamitesButton.setSize(res, res);
        dynamitesButton.addActor(new Image(Entities.extras_dynamite_default.getTexture(res, 0)));
        dynamiteLabel.setPosition(res / 2, res / 2, Align.center);
        dynamitesButton.addActor(dynamiteLabel);

        landminesButton.clearChildren();
        landminesButton.setSize(res, res);
        landminesButton.addActor(new Image(Entities.extras_land_mine_default.getTexture(res, 0)));
        landmineLabel.setPosition(res / 2, res / 2, Align.center);
        landminesButton.addActor(landmineLabel);

        umbrellasButton.clearChildren();
        umbrellasButton.setSize(res, res);
        umbrellasButton.addActor(new Image(Entities.extras_umbrella.getTexture(res, 0)));
        umbrellaLabel.setPosition(res / 2, res / 2, Align.center);
        umbrellasButton.addActor(umbrellaLabel);

        medicinesButton.clearChildren();
        medicinesButton.setSize(res, res);
        medicinesButton.addActor(new Image(Entities.extras_medicine.getTexture(res, 0)));
        medicineLabel.setPosition(res / 2, res / 2, Align.center);
        medicinesButton.addActor(medicineLabel);
    }

    public void toolboxUpdate() {
        //////// Chocolates
        if (mcminos.getChocolates() == 0) {
            chocolatesButton.remove();
        } else {
            chocolateLabel.setText(Util.formatInteger(mcminos.getChocolates(), 2));
            toolboxTable.add(chocolatesButton).row();
        }
        if(mcminos.getKeys() == 0) {
            keysButton.remove();
        } else {
            keyLabel.setText(Util.formatInteger(mcminos.getKeys(),2));
            toolboxTable.add(keysButton).row();
        }
        if(mcminos.getBombs() == 0) {
            bombsButton.remove();
        } else {
            bombLabel.setText(Util.formatInteger(mcminos.getBombs(), 2));
            toolboxTable.add(bombsButton).row();
        }
        if(mcminos.getDynamites() == 0) {
            dynamitesButton.remove();
        } else {
            dynamiteLabel.setText(Util.formatInteger(mcminos.getDynamites(),2));
            toolboxTable.add(dynamitesButton).row();
        }
        if(mcminos.getLandmines() == 0) {
            landminesButton.remove();
        } else {
            landmineLabel.setText(Util.formatInteger(mcminos.getLandmines(),2));
            toolboxTable.add(landminesButton).row();
        }
        if(mcminos.getUmbrellas() == 0) {
            umbrellasButton.remove();
        } else {
            umbrellaLabel.setText(Util.formatInteger(mcminos.getUmbrellas(),2));
            toolboxTable.add(umbrellasButton).row();
        }
        if(mcminos.getMedicines() == 0) {
            medicinesButton.remove();
        } else {
            medicineLabel.setText(Util.formatInteger(mcminos.getMedicines(),2));
            toolboxTable.add(medicinesButton).row();
        }
        toolboxTable.pack();
        float currentPadding = toolboxTable.getPadBottom();
        float newPadding = playwindow.getHeightInPixels() - toolboxTable.getHeight() + currentPadding;
        if(newPadding>0)
            toolboxTable.padBottom(newPadding);
        else
            toolboxTable.padBottom(0);

/*        pillLabel.setText(Util.formatInteger(level.getPillsNumber(),5));
        rockmeLabel.setText(Util.formatInteger(level.getRockmesNumber(),5));*/
    }

    public void activateToolbox() {
        game.setToolboxActivated(true);
    }

    public void deactivateToolbox() {
        game.setToolboxActivated(false);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        /////// Handle timing events (like moving and events)
        if( game.updateTime() ) { // not finished

            // Handle drawing
//        Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            menubatch.begin();
            for (int x = 0; x < playwindow.getWidthInPixels() + playwindow.resolution; x += playwindow.resolution) {
                for (int y = 0; y < playwindow.getHeightInPixels() + playwindow.resolution; y += playwindow.resolution) {
                    background.draw(playwindow, menubatch, x, y);
                }
            }
            menubatch.end();

            batch.setColor(Color.WHITE); // reset to full brightness as destroyed by menu
            batch.begin();

            ScissorStack.pushScissors(playwindow.getScissors());

            game.draw();

            batch.flush();
            ScissorStack.popScissors();
            batch.end(); // must end before menu


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

            toolboxUpdate(); // update toolbox based on inventory
            menu.draw();
            menu.act(delta);
        } // else level is finished
        else {
            backToMenu();
        }

    }

    @Override
    public void resize(int width, int height) {
        playwindow.resize(width, height);
        //menuTable.setBounds(0, 0, width, height);
        //toolboxTable.setBounds(0, 0, width, height); no these are fixed in little window
        menu.getViewport().update(width, height, true);
        //toolboxTable.setSize(width / 3, height * 4 / 5);
        toolboxResize();
        touchpadResize();
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
        mcminos.updateKeyDirections();
        if (!game.isToolboxActivated()) {
            if (mcminos.getKeyDirections() > 0 && !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                game.enableMovement();
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        mcminos.updateKeyDirections();
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        switch (character) {
            case '+':
                zoomPlus();
                playwindow.setResolution(gameResolutionCounter);
                toolboxResize();
                break;
            case '-':
                zoomMinus();
                playwindow.setResolution(gameResolutionCounter);
                toolboxResize();
                break;
            case 27:
            case ' ':
                if(game.isToolboxActivated()) {
                    deactivateToolbox();
                } else {
                    activateToolbox();
                }
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
        toolboxResize();
    }

    public void zoomMinus() {
        gameResolutionCounter++;
        if (gameResolutionCounter > Entities.resolutionList.length - 1)
            gameResolutionCounter = Entities.resolutionList.length - 1;
        playwindow.setResolution(gameResolutionCounter);
        toolboxResize();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!game.isToolboxActivated()) { // just pan in this case -> see there
            // TODO: consider only first button/finger
            if (button > 0) return false;
            if (!mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                game.enableMovement();
            }
            int x = windowToGameX(screenX);
            int y = windowToGameY(screenY);
            mcminos.setDestination(x, y);
            return false; // needs to be evtl. dealt with at drag
        }
        return false;
    }

    public int windowToGame(int screenCoordinate, int vpixelsize, int projection, int vpixelpos, boolean scroll) {
        // map to game coordinates
        int y = Util.shiftLeftLogical(screenCoordinate - projection, (PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent))
                + vpixelpos - (PlayWindow.virtualBlockResolution >> 1); // flip windowVPixelYPos-axis
        if (scroll) {
            if (y >= vpixelsize)
                y -= vpixelsize;
            if (y <= -(playwindow.virtualBlockResolution >> 1))
                y += vpixelsize;
        } else {
            if (y >= vpixelsize - PlayWindow.virtualBlockResolution )
                y = vpixelsize - PlayWindow.virtualBlockResolution - 1;
            if (y <= 0) y = 0;
        }
        return y;
    }

    public int windowToGameY(int screenY) {
        return windowToGame(Gdx.graphics.getHeight()-screenY, playwindow.getVPixelsLevelHeight(),
                playwindow.getProjectionY(), playwindow.windowVPixelYPos, level.getScrollY() );
    }

    public int windowToGameX(int screenX) {
        return windowToGame(screenX, playwindow.getVPixelsLevelWidth(),
                playwindow.getProjectionX(), playwindow.windowVPixelXPos, level.getScrollX() );
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
            if(game.isToolboxActivated()) {
                deactivateToolbox();
            } else {
                activateToolbox();
            }
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
        if (playwindow.game.isToolboxActivated()) {
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
