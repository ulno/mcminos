package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.ArrayList;

/**
 * Created by ulno on 13.11.15.
 */
public class Toolbox {
    private final Stage stage;
    private Skin levelSkin;
    private Skin menuSkin;
    private final PlayWindow playwindow;
    private final McMinos mcminos;
    private final Audio audio;
    private final Level level;
    private final Play play;
    private final Main main;

    private Table table;
    private Table rootTable;

    private boolean activated = false;

    private ScrollPane toolboxScroller;
    private ToolboxButton chocolateButton;
    private ToolboxButton keyButton;
    private ToolboxButton dynamiteButton;
    private ToolboxButton bombButton;
    private ToolboxButton landmineButton;
    private ToolboxButton umbrellaButton;
    private ToolboxButton medicineButton;
    private ToolboxButton playPauseButton;
    private ToolboxButton menuButton;
    private Image menuButtonImage;
    private Image chocolatesImage;

    private Label pillLabel;
    private SegmentString pillLabelText;
    private Label rockmeLabel;
    private SegmentString rockmeLabelText;


    private Table toolboxDialog = null;
    private final LevelBlock doorBlocks[] = new LevelBlock[4];
    private ArrayList<ToolboxButton> buttonList = new ArrayList<>();
    private boolean rebuildNecessary = true; // at the beginning it has to be rebuilt
    private boolean activatingTouchInProgress = false;

            /* old rootTable
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

        /*rootTable = new Window("Toolbox", skin);

        rootTable.setMovable(false);
        rootTable.setResizable(false);
        //rootTable.setResizeBorder(8); // big border so it works also onphones
        rootTable.setSize(stage.getWidth() / 3, stage.getWidth() * 4 / 5);
        rootTable.setPosition(stage.getWidth(), 0, Align.bottomRight);
        rootTable.align(Align.topLeft); // stuff in here move to top left
        rootTable.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent
        rootTable.addListener(new DragListener() {
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
                    float posx = rootTable.getX();
                    float posy = rootTable.getY();
                    rootTable.setPosition(posx - downx + x, posy - downy + y, Align.bottomLeft);
                }
            }
        });*/


    public Toolbox(Play play, PlayWindow playwindow, McMinos mcminos, Audio audio, Level level, Stage stage) {
        this.play = play;
        this.main = play.getMain();
        this.stage = stage;
        menuSkin = main.getMenuSkin(main.getSymbolResolution()/2);
        levelSkin = main.getLevelSkin(main.getSymbolResolution()/2);
        this.playwindow = playwindow;
        this.mcminos = mcminos;
        this.audio = audio;
        this.level = level;
        // TODO: check if skin needs to be dependent on resolution
        rootTable = new Table(menuSkin); // This is just the root of the table, updated by resize
        rootTable.setPosition(0, 0);
        stage.addActor(rootTable);
        // In there, we need a table on a scrollable pane
        table = new Table(menuSkin);
        table.setPosition(0, 0, Align.top);
        toolboxScroller = new ScrollPane(table);
        toolboxScroller.setScrollBarPositions(false, true);
        rootTable.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
        rootTable.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent
        //table.setColor(new Color(1, 1, 1, 0.8f)); // just a little transparent
        rootTable.add(toolboxScroller).fill().expand().align(Align.topLeft); // add this to root
//        rootTable.add(toolboxScroller).top(); // add this to root
//        table.setColor(0,1,0,1);
//        table.setBackground(new NinePatchDrawable(skin.getPatch(("default-rect"))));
//        rootTable.add(table).expand().fill().align(Align.topLeft).row();
        rootTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(event.getListenerActor() == rootTable) {
                    activatingTouchInProgress = !activated;
                    activate();
                    //table.fire(event);
                    //return super.touchDown(event, x, y, pointer, button);
                }
                return false;
            }
        });

        playPauseButton = new ToolboxButton( this, Entities.menu_pause, 0, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!activatingTouchInProgress ) {
                    if (activated) {
                        deactivate();
                    } else {
                        activate();
                    }
                }
            }
        });
        menuButton = new ToolboxButton( this, Entities.menu_settings, 0, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameMenu();
            }
        });
        chocolateButton = new ToolboxButton( this, Entities.pills_power_pill_chocolate, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateChocolate();
            }
        });
        keyButton = new ToolboxButton( this, Entities.extras_key, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doorOpener();
            }
        });
        bombButton = new ToolboxButton( this, Entities.extras_bomb_default, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateBomb();
            }
        });
        // TODO: work on drop only, long click/right click?
/*        bombDropButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mcminos.hasBomb()) {
                    mcminos.decreaseBombs();
                    mcminos.getLevelBlock().makeBomb();
                    toggleToolbox(); // close rootTable
                } else audio.soundPlay("error");
            }
        });*/
        dynamiteButton = new ToolboxButton( this, Entities.extras_dynamite_default, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateDynamite();
            }
        });
/*            dynamiteDropButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (mcminos.hasDynamite()) {
                        mcminos.decreaseDynamites();
                        mcminos.getLevelBlock().makeDynamite();
                        toggleToolbox(); // close rootTable
                    } else audio.soundPlay("error");
                }
            });*/
        landmineButton = new ToolboxButton( this, Entities.extras_land_mine_default, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateLandmine();
            }
        });

            /*landmineDropButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (mcminos.hasLandmine()) {
                        mcminos.decreaseLandmines();
                        mcminos.getLevelBlock().makeLandMine();
                        toggleToolbox(); // close rootTable
                    } else audio.soundPlay("error");
                }
            });*/
        umbrellaButton = new ToolboxButton( this, Entities.extras_umbrella, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateUmbrella();
            }
        });
        medicineButton = new ToolboxButton( this, Entities.extras_medicine, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateMedicine();
            }
        });

        resize();
    }

    public void resize() {
        int res = play.getSymbolResolution();
        menuSkin = main.getMenuSkin(res/2);
        levelSkin = main.getLevelSkin(res/2);
        // adjust size
        rootTable.setWidth(res + 4); // 4 for border
        rootTable.setHeight(playwindow.getHeightInPixels());

        for(int i=buttonList.size()-1; i>=0; i--) {
            buttonList.get(i).rebuildButton(res);
        }

        update();

        scheduleRebuild();
    }

    public void rebuild() {
        if( rebuildNecessary ) {
            Cell<Group> last = null;
            boolean rowadded = true;

            // make all visible
            table.clearChildren();

            for (int i = 0; i < buttonList.size(); i++) {
                if (!rowadded) {
                    last.row();
                    rowadded = true;
                }
                ToolboxButton tb = buttonList.get(i);
                if (tb.isVisible()) {
                    last = tb.addToTable();
                    rowadded = false;
                }
            }
            if (last != null)
                last.expandY().top().left().row();
            rebuildNecessary = false;
        }
    }

    public void update() {
        boolean c = false;
        c = c || playPauseButton.setGraphics(activated?Entities.menu_play:Entities.menu_pause); // set right image in pause-button
        c = c || chocolateButton.setValue(mcminos.getChocolates());
        c = c || keyButton.setValue(mcminos.getKeys());
        c = c || bombButton.setValue(mcminos.getBombs());
        c = c || dynamiteButton.setValue(mcminos.getDynamites());
        c = c || landmineButton.setValue(mcminos.getLandmines());
        c = c || umbrellaButton.setValue(mcminos.getUmbrellas());
        c = c || medicineButton.setValue(mcminos.getMedicines());
        // does not seem to work: sort();
        if(c) { // visibility changed
            scheduleRebuild();
        }
    }

    /**
     * make sure to trigger a rebuild at a later point
     */
    private void scheduleRebuild() {
        rebuildNecessary = true;
    }

    /* swap actor seems not to work
    private void sort() {
        // bubble sort toolbox
        ToolboxButton tmp;
        for( int i = buttonList.size()-1; i >= 1; i-- )
            for( int j = i; j >= 1; j-- ) {
                if( buttonList.get(j-1).isGreater(buttonList.get(j)) ) {
                    // bubble up
                    tmp = buttonList.get(j);
                    table.swapActor(tmp.getActor(), buttonList.get(j-1).getActor());
                    buttonList.set(j,buttonList.get(j-1));
                    buttonList.set(j-1, tmp);
                }
            }
    }
*/

    public void activate() {
        play.getGame().stopTimer();
        if(!activated) {
            activated = true;
            rebuild();
        }
    }

    public void deactivate() {
        if( activated ) {
            activated = false;
            removeDialog();
            rebuild();
        }
        play.getGame().startTimer();
    }

    private void removeDialog() {
        if(toolboxDialog!=null) toolboxDialog.remove();
        toolboxDialog = null;
        for( int i=0; i<doorBlocks.length; i++)
            doorBlocks[i] = null;
    }

    public boolean dialogActive() {
        return toolboxDialog != null;
    }

    private LevelBlock checkDoor(LevelBlock lb1, LevelBlock lb2) {
        if (lb1 == null) return null;
        if (lb1.hasDoor()) return lb1;
        if (lb2 == null) return null;
        if (lb2.hasDoor()) return lb2;
        return null;
    }

    public void toggleDoor(LevelBlock lb) {
        if (lb.hasDoor()) {
            if (!lb.hasRock()) { // if the door is not blocked by rock
                if (mcminos.hasKey()) {
                    mcminos.decreaseKeys();
                    lb.toggleDoor();
                    if (lb.hasClosedDoor()) // was opened
                        audio.soundPlay("rums");
                    else audio.soundPlay("quietsch");
                    deactivate(); // close table
                }
            }
        }
    }

    public void activateMedicine() {
        if (mcminos.hasMedicine() && (mcminos.getPoisonDuration() > 0 || mcminos.getDrunkLevel() > 0)) {
            mcminos.consumeMedicine();
            mcminos.increaseScore(10);
            deactivate(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateChocolate() {
        if (mcminos.hasChocolate() && mcminos.getPoisonDuration() == 0) {
            mcminos.decreaseChocolates();
            mcminos.setPowerPillValues(2, 1, 10);
            deactivate(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateBomb() {
        if (mcminos.hasBomb()) {
            mcminos.decreaseBombs();
            level.getGame().schedule(EventManager.Types.FuseBomb,mcminos.getFromLevelBlock());
            deactivate(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateDynamite() {
        if (mcminos.hasDynamite()) {
            mcminos.decreaseDynamites();
            level.getGame().schedule(EventManager.Types.FuseDynamite,mcminos.getFromLevelBlock());
            deactivate(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateLandmine() {
        if (mcminos.hasLandmine()) {
            mcminos.decreaseLandmines();
            mcminos.getFromLevelBlock().makeLandMineActivated();
            deactivate(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateUmbrella() {
        if (mcminos.hasUmbrella()) {
            mcminos.consumeUmbrella();
            audio.soundPlay("wind");
            mcminos.increaseScore(10);
            deactivate(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void doorOpener() {
        removeDialog(); // be sure to remove last
        if (mcminos.getKeys() <= 0) {
            audio.soundPlay("error");
            return;
        }
        int mvx = mcminos.getVX() + (PlayWindow.virtualBlockResolution >> 1);
        if (level.getScrollX()) mvx = mvx % level.getVPixelsWidth();
        int mvy = mcminos.getVY() + (PlayWindow.virtualBlockResolution >> 1);
        if (level.getScrollY()) mvy = mvy % level.getVPixelsHeight();

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
                Table outerTable = new Table(menuSkin);
                int res = playwindow.resolution;
                outerTable.setSize(res * 3, res * 3);
                // get mcminos center, to center dialog
                int mx = playwindow.vPixelToScreenX(mvx) + playwindow.getProjectionX();
                int my = playwindow.vPixelToScreenY(mvy) + playwindow.getProjectionY();
                outerTable.setPosition(mx, my, Align.center);
                Group innerGroup = new Group();
                innerGroup.setSize(res * 3, res * 3);
                outerTable.add(innerGroup).fill().expand();
                Table bgTable = new Table(menuSkin);
                bgTable.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
                bgTable.setSize(res * 3, res * 3);
                bgTable.setColor(new Color(1, 1, 1, 0.5f)); // very transparent
                innerGroup.addActor(bgTable);
                Table buttonTable = new Table(menuSkin);
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
                Image exitImage = new Image(Entities.toolbox_abort.getTexture(res, 0));
                buttonTable.add(exitImage).row();
                exitImage.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
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
        int res = play.getSymbolResolution();
        d.setBackground(new NinePatchDrawable(menuSkin.getPatch(("default-rect"))));
        d.setColor(new Color(1, 1, 1, 0.9f)); // little transparent
        d.setSize(Gdx.graphics.getWidth()-res,Gdx.graphics.getHeight()-res);
        d.setPosition(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2,Align.center);
        // Basic layout
        Table topMenu = new Table(menuSkin);
        topMenu.setHeight(res);
        ScrollPane scrollPane = new ScrollPane(topMenu);
        d.add(scrollPane).colspan(2).expandX().fillX().top().row();
        Table statisticsTable = new Table(menuSkin);
        scrollPane = new ScrollPane(statisticsTable);
        d.add(scrollPane).fill().expand();
        Table storyTable = new Table(menuSkin);
        scrollPane = new ScrollPane(storyTable);
        d.add(scrollPane).fill().expand();
        d.row();

        // Fill topMenu
        final Group soundButton = new Group();
        final TextureRegion emptyButtonGfx = Entities.menu_button_empty.getTexture(res,0);
        //soundButton.addActor(new Image(emptyButtonGfx));
        soundButton.addActor(new Image(audio.getSound()?
                Entities.menu_button_sound_on.getTexture(play.getSymbolResolution(),0)
                :Entities.menu_button_sound_off.getTexture(play.getSymbolResolution(),0)));
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.toggleSound();
                soundButton.clearChildren();
                soundButton.addActor(new Image(audio.getSound()?
                        Entities.menu_button_sound_on.getTexture(play.getSymbolResolution(),0)
                        :Entities.menu_button_sound_off.getTexture(play.getSymbolResolution(),0)));
                play.savePreferences();
            }
        });
        topMenu.add(soundButton).left().prefSize(res, res);

        final Group musicButton = new Group();
        //musicButton.addActor(new Image(emptyButtonGfx));
        musicButton.addActor(new Image(audio.getMusic()?
                Entities.menu_button_music_on.getTexture(play.getSymbolResolution(),0)
                :Entities.menu_button_music_off.getTexture(play.getSymbolResolution(),0)));
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.toggleMusic();
                musicButton.clearChildren();
                musicButton.addActor(new Image(audio.getMusic()?
                        Entities.menu_button_music_on.getTexture(play.getSymbolResolution(),0)
                        :Entities.menu_button_music_off.getTexture(play.getSymbolResolution(),0)));
                play.savePreferences();
            }
        });
        topMenu.add(musicButton).left().prefSize(res, res);

/*        final Button musicButton = new TextButton("Music\n"+ (audio.getMusic()?"on":"off"), skin);
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.toggleMusic();
                ((Label) musicButton.getChildren().first()).setText("Music\n" + (audio.getMusic() ? "on" : "off"));
            }
        });
        topMenu.add(musicButton).prefSize(res, res);
*/

        final Group touchpadButton = new Group();
        //touchpadButton.addActor(new Image(emptyButtonGfx));
        touchpadButton.addActor(new Image(Entities.menu_button_touchpad_off.getTexture(res,0)));
        touchpadButton.addActor(new Image(play.isTouchpadActive()?
                Entities.menu_button_touchpad_on.getTexture(play.getSymbolResolution(),0)
                :Entities.menu_button_touchpad_off.getTexture(play.getSymbolResolution(),0)));
        touchpadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                touchpadButton.clearChildren();
                touchpadButton.addActor(new Image(play.toggleTouchpad()?
                        Entities.menu_button_touchpad_on.getTexture(play.getSymbolResolution(),0)
                        :Entities.menu_button_touchpad_off.getTexture(play.getSymbolResolution(),0)));
                play.savePreferences();

            }
        });
        topMenu.add(touchpadButton).prefSize(res, res);

        Button saveButton = new TextButton("Save\nGame", menuSkin);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.getGame().saveSnapshot();
            }
        });
        topMenu.add(saveButton).prefSize(res, res);

        Group plusButton = new Group();
        //plusButton.addActor(new Image(emptyButtonGfx));
        plusButton.addActor(new Image(Entities.menu_button_zoom_in.getTexture(res, 0)));
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.zoomPlus();
                play.savePreferences();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });

        topMenu.add(plusButton).prefSize(res, res);

        Group minusButton = new Group();
        //minusButton.addActor(new Image(emptyButtonGfx));
        minusButton.addActor(new Image(Entities.menu_button_zoom_out.getTexture(res, 0)));
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.zoomMinus();
                play.savePreferences();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });
        topMenu.add(minusButton).prefSize(res, res);

        Button symbolPlusButton = new TextButton("Smbl\n+", menuSkin);
        symbolPlusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.increaseSymbolResolution();
                play.savePreferences();
                gameMenu(); // TODO: check if this leaks too much memory
            }
        });
        topMenu.add(symbolPlusButton).prefSize(res, res);

        Button symbolMinusButton = new TextButton("Smbl\n-", menuSkin);
        symbolMinusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.decreaseSymbolResolution();
                play.savePreferences();
                gameMenu();
            }
        });
        topMenu.add(symbolMinusButton).prefSize(res, res);

        Group leaveButton = new Group();
        //leaveButton.addActor(new Image(emptyButtonGfx));
        leaveButton.addActor(new Image(Entities.menu_button_stop.getTexture(res, 0)));
        leaveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
                play.backToMenu();
            }
        });
        topMenu.add(leaveButton).prefSize(res,res);

        Group pauseButton = new Group();
        //pauseButton.addActor(new Image(emptyButtonGfx));
        pauseButton.addActor(new Image(Entities.menu_button_pause.getTexture(res, 0)));
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                removeDialog();
            }
        });
        topMenu.add(pauseButton).prefSize(res,res);

        Group continueButton = new Group();
        //continueButton.addActor(new Image(emptyButtonGfx));
        continueButton.addActor(new Image(Entities.menu_button_play.getTexture(res, 0)));
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deactivate();
                //super.clicked(event, x, y);
            }
        });
        topMenu.add(continueButton).prefSize(res, res).maxSize(res).left().fillX().expandX();

        ///// Fill statistics
        statisticsTable.add(new Label("Statistics", menuSkin)).top().colspan(2).center().padBottom(res / 4).row();
        // Levelname
        statisticsTable.add(new Label("Levelname: " + level.getName(),menuSkin)).colspan(2).left().row();
        // Zoomlevel + Resolution
        statisticsTable.add(new Label(new StringBuilder("Density: ").append((int)(Gdx.graphics.getDensity()*160)), menuSkin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Zoom Level: ").append(play.getGameResolutionCounter()), menuSkin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Sprite Size: ").append(playwindow.resolution), menuSkin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Resolution: ").append(Gdx.graphics.getWidth()).append("x").append(Gdx.graphics.getHeight()), menuSkin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Symbol Resolution: ").append(play.getSymbolResolution()), menuSkin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("Minimap Sprite Size: ").append(playwindow.virtual2MiniResolution) , menuSkin)).left().row();
        statisticsTable.add(new Label(new StringBuilder("FPS: ").append((int)(Gdx.graphics.getFramesPerSecond())), menuSkin)).left().row();
        // Remaining pills
        statisticsTable.add(new Image(Entities.pills_pill_default.getTexture(res, 0))).left();
        pillLabel = new Label(new StringBuilder(5).append(level.getPillsNumber()), menuSkin);
        statisticsTable.add(pillLabel).left();
        statisticsTable.row();
        // Remaining rockmes
        statisticsTable.add(new Image(Entities.extras_rock_me.getTexture(res, 0))).left();
        rockmeLabel = new Label(new StringBuilder(2).append(level.getRockmesNumber()), menuSkin);
        statisticsTable.add(rockmeLabel).left();
        statisticsTable.row();

        //// Fill the story

        storyTable.add(new Label("Story", menuSkin)).top().center().padBottom(res / 4).row();
        Label story = new Label("Here we will have at one point a beautiful " +
                "story explaing everything in this Level. " +
                "This is only some example text at this point.\n\n" +
                "Stay tuned\n\nUlrich + Andreas", menuSkin);
        story.setWrap(true);
        storyTable.add(story).top().left().width(d.getWidth() / 2);

//        table.setWidth(play.getSymbolResolution() + 4);
//        table.setHeight(playwindow.getHeightInPixels() + 4);

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

    public int addButton(ToolboxButton toolboxButton) {
        buttonList.add( toolboxButton );
        return buttonList.size() - 1;
    }

    public Skin getLevelSkin() {
        return levelSkin;
    }

    public boolean isActivated() {
        return activated;
    }

    public Table getTable() {
        return table;
    }

    public boolean isRebuildNecessary() {
        return rebuildNecessary;
    }
}
