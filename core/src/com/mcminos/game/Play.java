package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

import com.badlogic.gdx.math.Matrix4;
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
    private final OrthographicCamera camera;
    private Game game;
    private Table toolboxTable;
    private Label bombLabel;
    private Label dynamiteLabel;
    private Label landmineLabel;
    private Label chocolateLabel;
    private Label pillLabel;
    private Label keyLabel;
    private Label rockmeLabel;
    private Label umbrellaLabel;
    private PlayWindow playwindow;
    private final Skin skin;
    private McMinos mcminos;
    private final Audio audio;
    private final BitmapFont font;
    private final SpriteBatch stageBatch;
    private final SpriteBatch gameBatch;
    private final SpriteBatch backgroundBatch;
    private final SpriteBatch miniBatch;
    private final ShapeRenderer miniScreenBackground;

    private Stage stage;
    private final Main main;
    private Level level;
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
    private Table toolboxDialog = null;
    private final LevelBlock doorBlocks[] = new LevelBlock[4];



    public Play(final Main main, String levelName) {
        this.main = main;
        gameBatch = main.getBatch();
        camera = new OrthographicCamera();
        font = main.getFont();
        skin = main.getSkin();
        audio = main.getAudio();
        // don't conflict with gameBatch
        stageBatch = new SpriteBatch();
        backgroundBatch = new SpriteBatch();
        miniBatch = new SpriteBatch();
        miniScreenBackground = new ShapeRenderer();
        init(levelName);
    }

    public void init(String levelName) {
        game = new Game(main, this, camera);
//        background = Entities.backgrounds_hexagon_03;
//        background = Entities.backgrounds_pavement_04;
        background = Entities.backgrounds_punched_plate_03;
        game.disableMovement();
        game.currentLevelName = levelName;
        level = game.loadLevel(levelName);
        mcminos = game.getMcMinos();
        playwindow = game.getPlayWindow();

        //  Basically, based on density and screensize, we want to set out default zoomlevel.
        float density = Gdx.graphics.getDensity(); // figure out resolution - if this is 1, that means about 160DPI, 2: 320DPI

        int preferredResolution =  Math.max( (int) (density * 32),
                Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() ) / 16
                );
        gameResolutionCounter = playwindow.setClosestResolution(preferredResolution);

        // Init stage
        stage = new Stage(new ScreenViewport(), stageBatch);
        /*menuTable = new Table();
        menuTable.setWidth(stage.getWidth());
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
        stage.addActor(menuTable);*/

        /*toolbox = new Window("Toolbox", skin);

        toolbox.setMovable(false);
        toolbox.setResizable(false);
        //toolbox.setResizeBorder(8); // big border so it works also onphones
        toolbox.setSize(stage.getWidth() / 3, stage.getWidth() * 4 / 5);
        toolbox.setPosition(stage.getWidth(), 0, Align.bottomRight);
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
        stage.addActor(toolbox);
        // In there, we need a table on a scrollable pane
        toolboxTable = new Table(skin);
        toolboxTable.setPosition(0, 0, Align.top);
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
                gameMenu();
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
                activateChocolate();
            }
        });

        /////// keys
        keysButton = new Group();
        keyLabel = new Label(Util.formatInteger(mcminos.getKeys(), 2), skin);
        keysButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //super.clicked(event, x, y);
                doorOpener();
            }
        });


        /////// Bombs
        bombsButton = new Group();
        bombLabel = new Label(Util.formatInteger(mcminos.getBombs(), 2), skin);
        bombsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateBomb();
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
                activateDynamite();
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
                activateLandmine();
            }
        });

        /////// Umbrellas
        umbrellasButton = new Group();
        umbrellaLabel = new Label(Util.formatInteger(mcminos.getUmbrellas(), 2), skin);
        umbrellasButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateUmbrella();
            }
        });

        /////// Medicines
        medicinesButton = new Group();
        medicineLabel = new Label(Util.formatInteger(mcminos.getMedicines(), 2), skin);

        medicinesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateMedicine();
            }
        });

        toolboxImages();
        toolboxUpdate();

        // virtual joystick (called touchpad in libgdx)
        touchpad = new Touchpad(32, skin);
        Color tpColor = touchpad.getColor();
        touchpad.setColor(tpColor.r, tpColor.g, tpColor.b, 0.7f);
        touchpadResize();
        touchpad.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.isToolboxActivated()) {
                    mcminos.updateTouchpadDirections(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());
                    if (mcminos.getKeyDirections() > 0 && !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                        game.enableMovement();
                    }
                }
            }
        });
        // load this from settings
        //stage.addActor(touchpad);

        // InputProcessor
        GestureDetector gd = new GestureDetector(this);
        InputMultiplexer im = new InputMultiplexer(stage, gd, this);
        Gdx.input.setInputProcessor(im); // init multiplexed InputProcessor
    }

    private void gameMenu() {
        toolboxDialogRemove(); // make sure any other one is gone
        Table d = new Table();
        int res = playwindow.resolution;
        d.setBackground(new NinePatchDrawable(skin.getPatch(("default-rect"))));
        d.setColor(new Color(1, 1, 1, 0.9f)); // little transparent
        d.setSize(Gdx.graphics.getWidth()-res,Gdx.graphics.getHeight()-res);
        d.setPosition(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2,Align.center);
        // Basic layout
        Table topMenu = new Table(skin);
        topMenu.setHeight(res);
        ScrollPane scrollPane = new ScrollPane(topMenu);
        d.add(scrollPane).colspan(2).expandX().top().row();
        Table statisticsTable = new Table(skin);
        d.add(statisticsTable).fill().expand();
        Table storyTable = new Table(skin);
        d.add(storyTable).fill().expand();
        d.row();

        // Fill topMenu
        final Button soundButton = new TextButton("Sound\n" + (audio.getSound()?"on":"off"), skin);
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.toggleSound();
                ((Label) soundButton.getChildren().first()).setText("Sound\n" + (audio.getSound() ? "on" : "off"));
            }
        });
        topMenu.add(soundButton).left().prefSize(res, res);

        final Button musicButton = new TextButton("Music\n"+ (audio.getMusic()?"on":"off"), skin);
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.toggleMusic();
                ((Label) musicButton.getChildren().first()).setText("Music\n" + (audio.getMusic() ? "on" : "off"));
            }
        });
        topMenu.add(musicButton).prefSize(res, res);

        Button touchpadButton = new TextButton("D-Pad", skin);
        touchpadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toogleTouchpad();
            }
        });
        topMenu.add(touchpadButton).prefSize(res, res);

        Button plusButton = new TextButton("+", skin);
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toolboxDialogRemove();
                zoomPlus();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });
        topMenu.add(plusButton).prefSize(res, res);

        Button minusButton = new TextButton("-", skin);
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toolboxDialogRemove();
                zoomMinus();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });
        topMenu.add(minusButton).prefSize(res, res);

        Button leaveButton = new TextButton("Leave", skin);
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toolboxDialogRemove();
                backToMenu();
            }
        });
        topMenu.add(leaveButton).prefSize(res,res);

        Button viewButton = new TextButton("View", skin);
        viewButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toolboxDialogRemove();
            }
        });
        topMenu.add(viewButton).prefSize(res,res);

        Image exitImage = new Image(Entities.extras_missing.getTexture(res, 0));
        exitImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toolboxDialogRemove();
                deactivateToolbox();
                //super.clicked(event, x, y);
            }
        });
        topMenu.add(exitImage).prefSize(res, res);

        ///// Fill statistics
        statisticsTable.add(new Label("Statistics", skin)).top().colspan(2).center().padBottom(res / 4).row();
        // Levelname
        statisticsTable.add(new Label("Levelname: " + level.getLevelName(),skin)).colspan(2).left().row();
        // Zoomlevel + Resolution
        statisticsTable.add(new Label("Density: " + Util.formatInteger((int)(Gdx.graphics.getDensity()*160),0), skin)).left().row();
        statisticsTable.add(new Label("Zoom Level: " + Util.formatInteger(gameResolutionCounter,0), skin)).left().row();
        statisticsTable.add(new Label("Sprite Size: " + Util.formatInteger(playwindow.resolution,0), skin)).left().row();
        statisticsTable.add(new Label("Resolution: " + Util.formatInteger(Gdx.graphics.getWidth(),0) + "x" + Util.formatInteger(Gdx.graphics.getHeight(),0) , skin)).left().row();
        statisticsTable.add(new Label("Minimap Sprite Size: " + Util.formatInteger(playwindow.virtual2MiniResolution,0) , skin)).left().row();
        // Remaining pills
        statisticsTable.add(new Image(Entities.pills_pill_default.getTexture(res, 0))).left();
        pillLabel = new Label(Util.formatInteger(level.getPillsNumber(),5), skin);
        statisticsTable.add(pillLabel).left();
        statisticsTable.row();
        // Remaining rockmes
        statisticsTable.add(new Image(Entities.extras_rock_me.getTexture(res, 0))).left();
        rockmeLabel = new Label(Util.formatInteger(level.getRockmesNumber(),5), skin);
        statisticsTable.add(rockmeLabel).left();
        statisticsTable.row();

        //// Fill the story

        storyTable.add(new Label("Story", skin)).top().center().padBottom(res / 4).row();
        Label story = new Label("Here we will have at one point a beautiful " +
                "story explaing everything in this Level. " +
                "This is only some example text at this point.\n\n" +
                "Stay tuned\n\nulno + nope", skin);
        story.setWrap(true);
        storyTable.add(story).top().left().width(d.getWidth() / 2);

//        toolboxTable.setWidth(playwindow.resolution + 4);
//        toolboxTable.setHeight(playwindow.getHeightInPixels() + 4);

        toolboxDialog = d;
        stage.addActor( toolboxDialog );
    }

    private void toogleTouchpad() {
        if(touchpad.hasParent())
            touchpad.remove();
        else {
            touchpadResize();
            stage.addActor(touchpad);
        }
    }

    private void activateMedicine() {
        if (mcminos.hasMedicine() && (mcminos.getPoisonDuration() > 0 || mcminos.getDrunkLevel() > 0)) {
            mcminos.consumeMedicine();
            mcminos.increaseScore(10);
            deactivateToolbox(); // close toolbox
        } else audio.soundPlay("error");
    }

    private void activateChocolate() {
        if (mcminos.hasChocolate() && mcminos.getPoisonDuration() == 0) {
            mcminos.decreaseChocolates();
            mcminos.setPowerPillValues(2, 1, 10);
            deactivateToolbox(); // close toolbox
        } else audio.soundPlay("error");
    }

    private void activateBomb() {
        if (mcminos.hasBomb()) {
            mcminos.decreaseBombs();
            new Explosion(mcminos.getFromLevelBlock(), LevelObject.Types.Bomb);
            deactivateToolbox(); // close toolbox
        } else audio.soundPlay("error");
    }

    private void activateDynamite() {
        if (mcminos.hasDynamite()) {
            mcminos.decreaseDynamites();
            new Explosion(mcminos.getFromLevelBlock(), LevelObject.Types.Dynamite);
            deactivateToolbox(); // close toolbox
        } else audio.soundPlay("error");
    }

    private void activateLandmine() {
        if (mcminos.hasLandmine()) {
            mcminos.decreaseLandmines();
            mcminos.getFromLevelBlock().makeLandMineActivated();
            deactivateToolbox(); // close toolbox
        } else audio.soundPlay("error");
    }

    private void activateUmbrella() {
        if (mcminos.hasUmbrella()) {
            mcminos.consumeUmbrella();
            audio.soundPlay("wind");
            mcminos.increaseScore(10);
            deactivateToolbox(); // close toolbox
        } else audio.soundPlay("error");
    }

    private void doorOpener() {
        toolboxDialogRemove(); // be sure to remove last
        if (mcminos.getKeys() <= 0) {
            audio.soundPlay("error");
            return;
        }
        int mvx = mcminos.getVX() + (PlayWindow.virtualBlockResolution >> 1);
        if (level.getScrollX()) mvx = mvx % playwindow.getVPixelsLevelWidth();
        int mvy = mcminos.getVY() + (PlayWindow.virtualBlockResolution >> 1);
        if (level.getScrollY()) mvy = mvy % playwindow.getVPixelsLevelWidth();

/*         // get rounded mcminos block
         LevelBlock lb = level.get( mvx / PlayWindow.virtualBlockResolution, mvy / PlayWindow.virtualBlockResolution);
allows cheating */
        LevelBlock lb = mcminos.getLevelBlock();

        // find doors
        int doors = 0;
        int doorsCount = 0;
        doorBlocks[0] = checkDoor(lb.up(), lb.up2());
        if (doorBlocks[0] != null) { // found door up
            doors += Mover.UP;
            doorsCount++;
        }
        doorBlocks[1] = checkDoor(lb.right(), lb.right2());
        if (doorBlocks[1] != null) { // found door right
            doors += Mover.RIGHT;
            doorsCount++;
        }
        doorBlocks[2] = checkDoor(lb.down(), lb.down2());
        if (doorBlocks[2] != null) { // found door down
            doors += Mover.DOWN;
            doorsCount++;
        }
        doorBlocks[3] = checkDoor(lb.left(), lb.left2());
        if (doorBlocks[3] != null) { // found door left
            doors += Mover.LEFT;
            doorsCount++;
        }
        /*
        // this allows mcminos to hide on a door
        doorBlocks[4] = checkDoor(lb,null);
        if (doorBlocks[4] != null) { // found door on field
            doors += Mover.ALL + 1;
            doorsCount++;
        }*/
        if (doorsCount > 0) { // found doors
            if (doorsCount == 1) { // if there is only one, it can just be opened
                toggleDoor(doorBlocks[Util.log2binary(doors)]);
            } else {
                // create dialog to allow choice
                Table outerTable = new Table(skin);
                int res = playwindow.resolution;
                outerTable.setSize(res * 3, res * 3);
                // get mcminos center, to center dialog
                int mx = playwindow.vPixelToScreenX(mvx) + playwindow.getProjectionX();
                int my = playwindow.vPixelToScreenY(mvy) + playwindow.getProjectionY();
                outerTable.setPosition(mx, my, Align.center);
                Group innerGroup = new Group();
                innerGroup.setSize(res * 3, res * 3);
                outerTable.add(innerGroup).fill().expand();
                Table bgTable = new Table(skin);
                bgTable.setBackground(new NinePatchDrawable(skin.getPatch(("default-rect"))));
                bgTable.setSize(res * 3, res * 3);
                bgTable.setColor(new Color(1, 1, 1, 0.5f)); // very transparent
                innerGroup.addActor(bgTable);
                Table buttonTable = new Table(skin);
                buttonTable.setSize(res * 3, res * 3);
                innerGroup.addActor(buttonTable);
                buttonTable.setColor(new Color(1, 1, 1, 1)); // not transparent
                buttonTable.defaults().fill().expand();
                Group empty = new Group();
                empty.setSize(0, 0);
                class KeyButton extends ClickListener {
                    Image keyImage;
                    LevelBlock doorBlock;

                    KeyButton(int res, LevelBlock doorBlock) {
                        keyImage = new Image(Entities.extras_key.getTexture(res, 0));
//                        keyImage.setColor(new Color(1, 1, 1, 1));
                        this.doorBlock = doorBlock;
                        keyImage.addListener(this);
                    }

                    Image getImage() {
                        return keyImage;
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        toggleDoor(doorBlock);
                        toolboxDialogRemove();
                        //super.clicked(event, x, y);
                    }
                }
                // line 1
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.UP) > 0) buttonTable.add(new KeyButton(res, doorBlocks[0]).getImage());
                else buttonTable.add(empty);
                Image exitImage = new Image(Entities.extras_missing.getTexture(res, 0));
                buttonTable.add(exitImage).row();
                exitImage.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        toolboxDialogRemove();
                        deactivateToolbox();
                        //super.clicked(event, x, y);
                    }
                });
                // line 2
                if ((doors & Mover.LEFT) > 0) buttonTable.add(new KeyButton(res, doorBlocks[3]).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.RIGHT) > 0) buttonTable.add(new KeyButton(res, doorBlocks[1]).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.row();
                // line 3
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.DOWN) > 0) buttonTable.add(new KeyButton(res, doorBlocks[2]).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.add(empty).pad(res / 2).row();

                toolboxDialog = outerTable;
                stage.addActor(toolboxDialog);
            }
        } else { // no door found
            audio.soundPlay("error");
        }
    }

    private void toolboxDialogRemove() {
        if(toolboxDialog!=null) toolboxDialog.remove();
        toolboxDialog = null;
        for( int i=0; i<doorBlocks.length; i++)
            doorBlocks[i] = null;
    }

    private LevelBlock checkDoor(LevelBlock lb1, LevelBlock lb2) {
        if (lb1 == null) return null;
        if (lb1.hasDoor()) return lb1;
        if (lb2 == null) return null;
        if (lb2.hasDoor()) return lb2;
        return null;
    }

    private void touchpadResize() {
        int width = Gdx.graphics.getWidth();
        int tpwidth = width / 4;
        int height = Gdx.graphics.getHeight();
        touchpad.setSize(tpwidth, tpwidth);
        touchpad.setDeadzone(tpwidth / 5);
        touchpad.setPosition(width * 3 / 4, 0);
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
        menuButtonImage.setSize(res - 4, res - 4);
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
        if (mcminos.getKeys() == 0) {
            keysButton.remove();
        } else {
            keyLabel.setText(Util.formatInteger(mcminos.getKeys(), 2));
            toolboxTable.add(keysButton).row();
        }
        if (mcminos.getBombs() == 0) {
            bombsButton.remove();
        } else {
            bombLabel.setText(Util.formatInteger(mcminos.getBombs(), 2));
            toolboxTable.add(bombsButton).row();
        }
        if (mcminos.getDynamites() == 0) {
            dynamitesButton.remove();
        } else {
            dynamiteLabel.setText(Util.formatInteger(mcminos.getDynamites(), 2));
            toolboxTable.add(dynamitesButton).row();
        }
        if (mcminos.getLandmines() == 0) {
            landminesButton.remove();
        } else {
            landmineLabel.setText(Util.formatInteger(mcminos.getLandmines(), 2));
            toolboxTable.add(landminesButton).row();
        }
        if (mcminos.getUmbrellas() == 0) {
            umbrellasButton.remove();
        } else {
            umbrellaLabel.setText(Util.formatInteger(mcminos.getUmbrellas(), 2));
            toolboxTable.add(umbrellasButton).row();
        }
        if (mcminos.getMedicines() == 0) {
            medicinesButton.remove();
        } else {
            medicineLabel.setText(Util.formatInteger(mcminos.getMedicines(), 2));
            toolboxTable.add(medicinesButton).row();
        }
        toolboxTable.pack();
        float currentPadding = toolboxTable.getPadBottom();
        float newPadding = playwindow.getHeightInPixels() - toolboxTable.getHeight() + currentPadding;
        if (newPadding > 0)
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
        toolboxDialogRemove();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        /////// Handle timing events (like moving and events)
        if (game.updateTime()) { // not finished
            int w = Gdx.graphics.getWidth();
            int h = Gdx.graphics.getHeight();

            // Handle drawing
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            SpriteBatch testBatch = backgroundBatch;
            testBatch.begin();
            for (int x = 0; x < playwindow.getWidthInPixels() + playwindow.resolution; x += playwindow.resolution) {
                for (int y = 0; y < playwindow.getHeightInPixels() + playwindow.resolution; y += playwindow.resolution) {
                    background.draw(playwindow, testBatch, x, y);
                }
            }
            testBatch.end();

//            gameBatch.setColor(Color.WHITE); // reset to full brightness as destroyed by menu
            gameBatch.begin();

            ScissorStack.pushScissors(playwindow.getScissors());

            game.draw();

            gameBatch.flush();
            ScissorStack.popScissors();


            gameBatch.end(); // must end before other layers

            // draw a dark transparent rectangle to have some background for mini screen
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            miniScreenBackground.begin(ShapeRenderer.ShapeType.Filled);
            miniScreenBackground.setColor(0,0,0,0.5f); // a little transparent
            miniScreenBackground.rect(Graphics.virtualToMiniX(playwindow,0,0)- playwindow.virtual2MiniResolution,
                    Graphics.virtualToMiniY(playwindow,0,0)- playwindow.virtual2MiniResolution,
                    Graphics.virtualToMiniX(playwindow,playwindow.getVPixelsLevelWidth()-1,0),
                    Graphics.virtualToMiniX(playwindow,playwindow.getVPixelsLevelHeight()-1,0));
            miniScreenBackground.end();

            // mini screen
            miniBatch.begin();
            game.drawMini(miniBatch);
            miniBatch.end();

            drawVisibleMarker();


            stageBatch.begin();
            // score etc.
            font.draw(stageBatch,
                    "S" + Util.formatInteger(mcminos.getScore(), 6)
                            + " P" + Util.formatInteger(mcminos.getPowerDuration() >> game.timeResolutionExponent, 3)
                            + " U" + Util.formatInteger(mcminos.getUmbrellaDuration() >> game.timeResolutionExponent, 3)
                            + " T" + Util.formatInteger((mcminos.getPoisonDuration()+mcminos.getDrunkLevel()) >> game.timeResolutionExponent, 2)
                            + " L" + Util.formatInteger(mcminos.getLives(), 2)
                            + (mcminos.isMirrored() ? " M" : ""),
                    20, Gdx.graphics.getHeight() - 20);
            // " P%03d U%03d T%02d L%02d ",
            // add stage and menu
            stageBatch.end();

            toolboxUpdate(); // update toolbox based on inventory
            stage.draw();
            stage.act(delta);
        } // else level is finished
        else {
            backToMenu();
        }

    }

    private void drawVisibleMarker() {

        // visible area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        miniScreenBackground.begin(ShapeRenderer.ShapeType.Filled);
        miniScreenBackground.setColor(255,128,0,0.5f); // orange transparent

        // These are up to 8 lines (4 corners) to draw
        int t = playwindow.virtual2MiniResolution / 2; // line thickness
        int t2 = t * 2;
        int thickness = 1+t;
        // compute the visible area lower left corner
        int x0 = Graphics.virtualToMiniX(playwindow,playwindow.windowVPixelXPos,0);
        int y0 = Graphics.virtualToMiniY(playwindow,playwindow.windowVPixelYPos,0);
        // compute the upper right corner
        int x1 = Graphics.virtualToMiniX(playwindow,((playwindow.windowVPixelXPos + playwindow.getVisibleWidthInVPixels() - 1) % playwindow.getVPixelsLevelWidth()),0);
        int y1 = Graphics.virtualToMiniY(playwindow,((playwindow.windowVPixelYPos + playwindow.getVisibleHeightInVPixels() - 1) % playwindow.getVPixelsLevelHeight()),0);
        // lower left corner of mini-screen
        int mx0 = Graphics.virtualToMiniX(playwindow,0,0);
        int my0 = Graphics.virtualToMiniY(playwindow,0,0);
        // upper right corner of mini-screen
        int mx1 = Graphics.virtualToMiniX(playwindow,playwindow.getVPixelsLevelWidth()-1,0);
        int my1 = Graphics.virtualToMiniY(playwindow,playwindow.getVPixelsLevelHeight()-1,0);

        if(x0<x1) { // normal, no split
            miniScreenBackground.rect( x0-t,y0-t,x1-x0+t2+1,thickness );
            miniScreenBackground.rect( x0-t,y1,x1-x0+t2+1,thickness );
        } else { // split necessary x1 < x0
            miniScreenBackground.rect( mx0-t,y0-t,x1-mx0+t2+1,thickness );
            miniScreenBackground.rect( x0-t,y0-t,mx1-x0+t2,thickness );
            miniScreenBackground.rect( mx0-t,y1,x1-mx0+t2+1,thickness );
            miniScreenBackground.rect( x0-t,y1,mx1-x0+t2,thickness );
        }
        if(y0<y1) { // normal, no split
            miniScreenBackground.rect( x0-t,y0+1,thickness,y1-y0-1 );
            miniScreenBackground.rect( x1,y0+1,thickness,y1-y0-1 );
        } else { // split necessary y1 < y0
            miniScreenBackground.rect( x0-t,my0+1,thickness,y1-my0-1 );
            miniScreenBackground.rect( x0-t,y0+1,thickness,my1-y0-1);
            miniScreenBackground.rect( x1,my0+1,thickness,y1-my0-1 );
            miniScreenBackground.rect( x1,y0+1,thickness,my1-y0-1 );
        }

        miniScreenBackground.end();

    }

    @Override
    public void resize(int width, int height) {
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0,0,width,height);
        backgroundBatch.setProjectionMatrix(matrix);
        miniBatch.setProjectionMatrix(matrix);
        stageBatch.setProjectionMatrix(matrix);
        miniScreenBackground.setProjectionMatrix(matrix);

        playwindow.resize(width, height);
        //menuTable.setBounds(0, 0, width, height);
        //toolboxTable.setBounds(0, 0, width, height); no these are fixed in little window
        stage.getViewport().update(width, height, true);
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
        //stageBatch.dispose();
        game.dispose();
        stage.dispose();
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
        if(toolboxDialog != null) {
            LevelBlock lb = null;
            switch (keycode) {
                case Input.Keys.W:
                case Input.Keys.UP:
                    lb = doorBlocks[0];
                    break;
                case Input.Keys.D:
                case Input.Keys.RIGHT:
                    lb = doorBlocks[1];
                    break;
                case Input.Keys.S:
                case Input.Keys.DOWN:
                    lb = doorBlocks[2];
                    break;
                case Input.Keys.A:
                case Input.Keys.LEFT:
                    lb = doorBlocks[3];
                    break;
            }
            if (lb != null) {
                toggleDoor(lb);
                toolboxDialogRemove();
            }
        }
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
            case '1':
                activateToolbox();
                activateChocolate();
                break;
            case '2':
                activateToolbox();
                doorOpener();
                break;
            case '3':
                activateToolbox();
                activateBomb();
                break;
            case '4':
                activateToolbox();
                activateDynamite();
                break;
            case '5':
                activateToolbox();
                activateLandmine();
                break;
            case '6':
                activateToolbox();
                activateUmbrella();
                break;
            case '7':
                activateToolbox();
                activateMedicine();
                break;
            case 27: // Escape
            case 't':
            case 'T':
            case ' ':
                if (game.isToolboxActivated()) {
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
            if (y >= vpixelsize - PlayWindow.virtualBlockResolution)
                y = vpixelsize - PlayWindow.virtualBlockResolution - 1;
            if (y <= 0) y = 0;
        }
        return y;
    }

    public int windowToGameY(int screenY) {
        return windowToGame(Gdx.graphics.getHeight() - screenY, playwindow.getVPixelsLevelHeight(),
                playwindow.getProjectionY(), playwindow.windowVPixelYPos, level.getScrollY());
    }

    public int windowToGameX(int screenX) {
        return windowToGame(screenX, playwindow.getVPixelsLevelWidth(),
                playwindow.getProjectionX(), playwindow.windowVPixelXPos, level.getScrollX());
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
        if (amount > 0) {
            zoomMinus();
            return true;
        } else if (amount < 0) {
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
        if (button > 0 || count > 1) {
            if (game.isToolboxActivated()) {
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
