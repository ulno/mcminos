package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by ulno on 13.11.15.
 */
public class Toolbox {
    private final Stage stage;
    private final Skin skin;
    private final Game game;
    private final PlayWindow playwindow;
    private final McMinos mcminos;
    private final Audio audio;
    private final Level level;
    private final Play play;

    private Table toolboxTable;
    private Label bombLabel;
    private SegmentString bombLabelText;
    private Label dynamiteLabel;
    private SegmentString dynamiteLabelText;
    private Label landmineLabel;
    private SegmentString landmineLabelText;
    private Label chocolateLabel;
    private SegmentString chocolateLabelText;
    private Label keyLabel;
    private SegmentString keyLabelText;
    private Label umbrellaLabel;
    private SegmentString umbrellaLabelText;
    private Label medicineLabel;
    private SegmentString medicineLabelText;

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

    private Label pillLabel;
    private SegmentString pillLabelText;
    private Label rockmeLabel;
    private SegmentString rockmeLabelText;


    private Table toolboxDialog = null;
    private final LevelBlock doorBlocks[] = new LevelBlock[4];


            /* old toolbox
            menuTable = new Table();
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


    public Toolbox(Game game, Play play, Stage stage, Skin skin) {
        this.game = game;
        this.play = play;
        this.stage = stage;
        this.skin = skin;
        playwindow = game.getPlayWindow();
        mcminos = game.getMcMinos();
        audio = game.getAudio();
        level = game.getLevel();
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
        toolbox.add(toolboxScroller).fill().expand().align(Align.topLeft); // add this to root
//        toolbox.add(toolboxScroller).top(); // add this to root
//        toolboxTable.setColor(0,1,0,1);
//        toolboxTable.setBackground(new NinePatchDrawable(skin.getPatch(("default-rect"))));
//        toolbox.add(toolboxTable).expand().fill().align(Align.topLeft).row();
        toolbox.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                activate();
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        resize();
        update();

    }

    public void resize() {
        int res = playwindow.resolution;
        // adjust size
        toolbox.setWidth(res + 4); // 4 for border
        toolbox.setHeight(playwindow.getHeightInPixels());
        addImages();
    }

    public void addImages() {
        int res = playwindow.resolution;

        toolboxTable.clearChildren(); // empty all

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


        /////////// chocolates
        chocolatesButton = new Group();
        chocolateLabelText = new SegmentString(2);
        chocolateLabel = new Label(chocolateLabelText, skin);
        chocolatesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateChocolate();
            }
        });

        /////// keys
        keysButton = new Group();
        keyLabelText = new SegmentString(2);
        keyLabel = new Label(keyLabelText.getStringBuilder(), skin);
        keysButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //super.clicked(event, x, y);
                doorOpener();
            }
        });


        /////// Bombs
        bombsButton = new Group();
        bombLabelText = new SegmentString(2);
        bombLabel = new Label(bombLabelText.getStringBuilder(), skin);
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
        dynamiteLabelText = new SegmentString(2);
        dynamiteLabel = new Label(dynamiteLabelText.getStringBuilder(), skin);
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
        landmineLabelText = new SegmentString(2);
        landmineLabel = new Label(landmineLabelText.getStringBuilder(), skin);
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
        umbrellaLabelText = new SegmentString(2);
        umbrellaLabel = new Label(umbrellaLabelText.getStringBuilder(), skin);
        umbrellasButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateUmbrella();
            }
        });

        /////// Medicines
        medicinesButton = new Group();
        medicineLabelText = new SegmentString(2);
        medicineLabel = new Label(medicineLabelText.getStringBuilder(), skin);

        medicinesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateMedicine();
            }
        });


        // now set correct sizes
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

        chocolateLabelText.writeInteger(mcminos.getChocolates());
        chocolateLabel.setText(chocolateLabelText.getStringBuilder());
        keyLabelText.writeInteger(mcminos.getKeys());
        keyLabel.setText(keyLabelText.getStringBuilder());
        bombLabelText.writeInteger(mcminos.getBombs());
        bombLabel.setText(bombLabelText.getStringBuilder());
        dynamiteLabelText.writeInteger(mcminos.getDynamites());
        dynamiteLabel.setText(dynamiteLabelText.getStringBuilder());
        landmineLabelText.writeInteger(mcminos.getLandmines());
        landmineLabel.setText(landmineLabelText.getStringBuilder());
        umbrellaLabelText.writeInteger(mcminos.getUmbrellas());
        umbrellaLabel.setText(umbrellaLabelText.getStringBuilder());
        medicineLabelText.writeInteger(mcminos.getMedicines());
        medicineLabel.setText(medicineLabelText.getStringBuilder());

        // make all visible
        toolboxTable.add(menuButton).row();
        toolboxTable.add(chocolatesButton).row();
        toolboxTable.add(keysButton).row();
        toolboxTable.add(bombsButton).row();
        toolboxTable.add(dynamitesButton).row();
        toolboxTable.add(landminesButton).row();
        toolboxTable.add(umbrellasButton).row();
        toolboxTable.add(medicinesButton).expandY().top().left().row();

        update();
    }

    public void update() {
        //////// Chocolates
        if (mcminos.getChocolates() == 0) {
            chocolatesButton.setColor(0,0,0,0);
        } else {
            chocolatesButton.setColor( 1,1,1,1);
            chocolateLabelText.writeInteger(mcminos.getChocolates());
        }
        chocolateLabel.setText(chocolateLabelText.getStringBuilder()); // outside if as this triggers update
        if (mcminos.getKeys() == 0) {
            keysButton.setColor(0,0,0,0);
        } else {
            keysButton.setColor(1,1,1,1);
            keyLabelText.writeInteger(mcminos.getKeys());
        }
        keyLabel.setText(keyLabelText.getStringBuilder());
        if (mcminos.getBombs() == 0) {
            bombsButton.setColor(0,0,0,0);
        } else {
            bombsButton.setColor(1,1,1,1);
            bombLabelText.writeInteger(mcminos.getBombs());
        }
        bombLabel.setText(bombLabelText.getStringBuilder());
        if (mcminos.getDynamites() == 0) {
            dynamitesButton.setColor(0,0,0,0);
        } else {
            dynamitesButton.setColor(1,1,1,1);
            dynamiteLabelText.writeInteger(mcminos.getDynamites());
        }
        dynamiteLabel.setText(dynamiteLabelText.getStringBuilder());
        if (mcminos.getLandmines() == 0) {
            landminesButton.setColor(0,0,0,0);
        } else {
            landminesButton.setColor(1,1,1,1);
            landmineLabelText.writeInteger(mcminos.getLandmines());
        }
        landmineLabel.setText(landmineLabelText.getStringBuilder());
        if (mcminos.getUmbrellas() == 0) {
            umbrellasButton.setColor(0,0,0,0);
        } else {
            umbrellasButton.setColor(1,1,1,1);
            umbrellaLabelText.writeInteger(mcminos.getUmbrellas());
        }
        umbrellaLabel.setText(umbrellaLabelText.getStringBuilder());
        if (mcminos.getMedicines() == 0) {
            medicinesButton.setColor(0,0,0,0);
        } else {
            medicinesButton.setColor(1,1,1,1);
            medicineLabelText.writeInteger(mcminos.getMedicines());
        }
        medicineLabel.setText(medicineLabelText.getStringBuilder());
/*        toolboxTable.pack();

        // TODO: fix padding, creates (garbage collected) memory leak?
        float currentPadding = toolboxTable.getPadBottom();
        float newPadding = playwindow.getHeightInPixels() - toolboxTable.getHeight() + currentPadding;
        if (newPadding > 0)
            toolboxTable.padBottom(newPadding);
        else
            toolboxTable.padBottom(0);
*/
    }


    public void activate() {
        game.setToolboxActivated(true);
    }

    public void deactivate() {
        game.setToolboxActivated(false);
        removeDialog();
    }

    private void removeDialog() {
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

    private void toggleDoor(LevelBlock lb) {
        if (lb.hasDoor()) {
            if (!lb.hasRock()) { // if the door is not blocked by rock
                if (mcminos.hasKey()) {
                    mcminos.decreaseKeys();
                    lb.toggleDoor();
                    if (lb.hasClosedDoor()) // was opened
                        audio.soundPlay("rums");
                    else audio.soundPlay("quietsch");
                    deactivate(); // close toolboxTable
                }
            }
        }
    }

    public void activateMedicine() {
        if (mcminos.hasMedicine() && (mcminos.getPoisonDuration() > 0 || mcminos.getDrunkLevel() > 0)) {
            mcminos.consumeMedicine();
            mcminos.increaseScore(10);
            deactivate(); // close toolbox
        } else audio.soundPlay("error");
    }

    public void activateChocolate() {
        if (mcminos.hasChocolate() && mcminos.getPoisonDuration() == 0) {
            mcminos.decreaseChocolates();
            mcminos.setPowerPillValues(2, 1, 10);
            deactivate(); // close toolbox
        } else audio.soundPlay("error");
    }

    public void activateBomb() {
        if (mcminos.hasBomb()) {
            mcminos.decreaseBombs();
            new Explosion(mcminos.getFromLevelBlock(), LevelObject.Types.Bomb);
            deactivate(); // close toolbox
        } else audio.soundPlay("error");
    }

    public void activateDynamite() {
        if (mcminos.hasDynamite()) {
            mcminos.decreaseDynamites();
            new Explosion(mcminos.getFromLevelBlock(), LevelObject.Types.Dynamite);
            deactivate(); // close toolbox
        } else audio.soundPlay("error");
    }

    public void activateLandmine() {
        if (mcminos.hasLandmine()) {
            mcminos.decreaseLandmines();
            mcminos.getFromLevelBlock().makeLandMineActivated();
            deactivate(); // close toolbox
        } else audio.soundPlay("error");
    }

    public void activateUmbrella() {
        if (mcminos.hasUmbrella()) {
            mcminos.consumeUmbrella();
            audio.soundPlay("wind");
            mcminos.increaseScore(10);
            deactivate(); // close toolbox
        } else audio.soundPlay("error");
    }

    public void doorOpener() {
        removeDialog(); // be sure to remove last
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

                    KeyButton(int res, LevelBlock doorBlock, Graphics gfx) {
                        keyImage = new Image(gfx.getTexture(res, 0));
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
                        removeDialog();
                        //super.clicked(event, x, y);
                    }
                }
                // line 1
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.UP) > 0) buttonTable.add(new KeyButton(res, doorBlocks[0],Entities.toolbox_key_option_up).getImage());
                else buttonTable.add(empty);
                Image exitImage = new Image(Entities.extras_missing.getTexture(res, 0));
                buttonTable.add(exitImage).row();
                exitImage.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        removeDialog();
                        deactivate();
                        //super.clicked(event, x, y);
                    }
                });
                // line 2
                if ((doors & Mover.LEFT) > 0) buttonTable.add(new KeyButton(res, doorBlocks[3],Entities.toolbox_key_option_left).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.RIGHT) > 0) buttonTable.add(new KeyButton(res, doorBlocks[1],Entities.toolbox_key_option_right).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.row();
                // line 3
                buttonTable.add(empty).pad(res / 2);
                if ((doors & Mover.DOWN) > 0) buttonTable.add(new KeyButton(res, doorBlocks[2],Entities.toolbox_key_option_down).getImage());
                else buttonTable.add(empty).pad(res / 2);
                buttonTable.add(empty).pad(res / 2).row();

                toolboxDialog = outerTable;
                stage.addActor(toolboxDialog);
            }
        } else { // no door found
            audio.soundPlay("error");
        }
    }

    private void gameMenu() {
        removeDialog(); // make sure any other one is gone
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
                play.toogleTouchpad();
            }
        });
        topMenu.add(touchpadButton).prefSize(res, res);

        Button plusButton = new TextButton("+", skin);
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.zoomPlus();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });
        topMenu.add(plusButton).prefSize(res, res);

        Button minusButton = new TextButton("-", skin);
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.zoomMinus();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });
        topMenu.add(minusButton).prefSize(res, res);

        Button leaveButton = new TextButton("Leave", skin);
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.backToMenu();
            }
        });
        topMenu.add(leaveButton).prefSize(res,res);

        Button viewButton = new TextButton("View", skin);
        viewButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
            }
        });
        topMenu.add(viewButton).prefSize(res,res);

        Image exitImage = new Image(Entities.extras_missing.getTexture(res, 0));
        exitImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                deactivate();
                //super.clicked(event, x, y);
            }
        });
        topMenu.add(exitImage).prefSize(res, res);

        ///// Fill statistics
        statisticsTable.add(new Label("Statistics", skin)).top().colspan(2).center().padBottom(res / 4).row();
        // Levelname
        statisticsTable.add(new Label("Levelname: " + level.getLevelName(),skin)).colspan(2).left().row();
        // Zoomlevel + Resolution
        statisticsTable.add(new Label(new StringBuilder("Density: ").append((int)(Gdx.graphics.getDensity()*160)), skin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Zoom Level: ").append(play.getGameResolutionCounter()), skin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Sprite Size: ").append(playwindow.resolution), skin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Resolution: ").append(Gdx.graphics.getWidth()).append("x").append(Gdx.graphics.getHeight()), skin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Minimap Sprite Size: ").append(playwindow.virtual2MiniResolution) , skin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("FPS: ").append((int)(Gdx.graphics.getFramesPerSecond())), skin)).left().row();
        // Remaining pills
        statisticsTable.add(new Image(Entities.pills_pill_default.getTexture(res, 0))).left();
        pillLabel = new Label(new StringBuilder(5).append(level.getPillsNumber()), skin);
        statisticsTable.add(pillLabel).left();
        statisticsTable.row();
        // Remaining rockmes
        statisticsTable.add(new Image(Entities.extras_rock_me.getTexture(res, 0))).left();
        rockmeLabel = new Label(new StringBuilder(2).append(level.getRockmesNumber()), skin);
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

    public void checkDoorKey(int keycode) {
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
                removeDialog();
            }
        }

    }
}
