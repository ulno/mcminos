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
    private final Preferences preferences;
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

    private HotSpot hotSpotRoot = null;


    private ArrayList<ToolboxButton> buttonList = new ArrayList<>();
    private boolean rebuildNecessary = true; // at the beginning it has to be rebuilt
    private boolean activatingTouchInProgress = false;
    private int activeButtons = 0;

    public Toolbox(final Play play) {
        this.play = play;
        this.main = play.getMain();
        this.preferences = main.getPreferences();
        this.stage = play.getStage();
        this.playwindow = play.getPlayWindow();

        Game game = play.getGame();
        this.mcminos = game.getMcMinos();
        this.audio = game.getAudio();
        this.level = game.getLevel();

        menuSkin = main.getMenuSkin(preferences.getSymbolResolution());
        levelSkin = main.getLevelSkin(preferences.getSymbolResolution()/2);

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
                    activatingTouchInProgress = ! play.isPaused();
                    play.pauseOn();
                    //table.fire(event);
                    //return super.touchDown(event, x, y, pointer, button);
                }
                return false;
            }
        });

        int res = preferences.getSymbolResolution();

        playPauseButton = new ToolboxButton( this, Entities.menu_symbol_pause, res, 0, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!activatingTouchInProgress ) {
                    if (play.isPaused()) {
                        play.pauseOff();
                    } else {
                        play.pauseOn();
                    }
                }
            }
        });
        menuButton = new ToolboxButton( this, Entities.menu_symbol_settings, res, 0, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(play.hasDialog()) play.closeDialog();
                else play.dialogGameMenu();
            }
        });
        chocolateButton = new ToolboxButton( this, Entities.pills_power_pill_chocolate, res, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateChocolate();
            }
        });
        keyButton = new ToolboxButton( this, Entities.extras_key, res, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                play.dialogDoorOpener();
            }
        });
        bombButton = new ToolboxButton( this, Entities.extras_bomb_default, res, 2, new ClickListener() {
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
        dynamiteButton = new ToolboxButton( this, Entities.extras_dynamite_default, res, 2, new ClickListener() {
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
        landmineButton = new ToolboxButton( this, Entities.extras_land_mine_default, res, 2, new ClickListener() {
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
        umbrellaButton = new ToolboxButton( this, Entities.extras_umbrella, res, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateUmbrella();
            }
        });
        medicineButton = new ToolboxButton( this, Entities.extras_medicine, res, 2, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activateMedicine();
            }
        });

        resize();
    }

    public void resize() {
        int res = play.getSymbolResolution();
        menuSkin = main.getMenuSkin(res);
        levelSkin = main.getLevelSkin(res/2);
        // adjust size
        rootTable.setWidth(res + 4); // 4 for border
        rootTable.setHeight(playwindow.getHeightInPixels());

/*        for(int i=buttonList.size()-1; i>=0; i--) {
            buttonList.get(i).rebuildButton(res);
        } */

        update();

        scheduleRebuild();
    }

    public void rebuild() {
        HotSpot hs;

        if( rebuildNecessary ) {
            Cell<Group> lastTB = null;
            boolean rowadded = true;
            int res = preferences.getSymbolResolution();
            hotSpotRoot = null;
            HotSpot lastHS = null;

            // make all visible
            table.clearChildren();

            activeButtons = 0;

            for (int i = 0; i < buttonList.size(); i++) {
                if (!rowadded) {
                    lastTB.row();
                    rowadded = true;
                }
                ToolboxButton tb = buttonList.get(i);
                if (tb.isVisible()) {
                    activeButtons ++;
                    tb.rebuildButton( res ); // TODO: check, if this creates memory leak
                    lastTB = tb.addToTable();
                    rowadded = false;
                    // also build hotspot list
                    hs = new HotSpot(tb.getActor(),toolboxScroller,100 + i);
                    if(lastHS == null) {
                        hotSpotRoot = hs;
                    } else {
                        lastHS.setDown(hs);
                        hs.setUp(lastHS);
                    }
                    lastHS = hs;
                }
            }
            if (lastTB != null)
                lastTB.expandY().top().left().row();
            rebuildNecessary = false;
        }
    }

    public void update() {
        boolean c = false;
        c = c || playPauseButton.setGraphics(play.isPaused()?Entities.menu_symbol_play:Entities.menu_symbol_pause); // set right image in pause-button
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


    public void activateMedicine() {
        if (mcminos.hasMedicine() && (mcminos.getPoisonDuration() > 0 || mcminos.getDrunkLevel() > 0)) {
            mcminos.consumeMedicine();
            mcminos.increaseScore(10);
            play.pauseOff(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateChocolate() {
        if (mcminos.hasChocolate() && mcminos.getPoisonDuration() == 0) {
            mcminos.decreaseChocolates();
            mcminos.setPowerPillValues(2, 1, 10);
            play.pauseOff(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateBomb() {
        if (mcminos.hasBomb()) {
            mcminos.decreaseBombs();
            level.getGame().schedule(EventManager.Types.FuseBomb, mcminos.getFromLevelBlock());
            play.pauseOff(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateDynamite() {
        if (mcminos.hasDynamite()) {
            mcminos.decreaseDynamites();
            level.getGame().schedule(EventManager.Types.FuseDynamite,mcminos.getFromLevelBlock());
            play.pauseOff(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateLandmine() {
        if (mcminos.hasLandmine()) {
            mcminos.decreaseLandmines();
            mcminos.getFromLevelBlock().makeLandMineActivated();
            play.pauseOff(); // close rootTable
        } else audio.soundPlay("error");
    }

    public void activateUmbrella() {
        if (mcminos.hasUmbrella()) {
            mcminos.consumeUmbrella();
            audio.soundPlay("wind");
            mcminos.increaseScore(10);
            play.pauseOff(); // close rootTable
        } else audio.soundPlay("error");
    }


    public int addButton(ToolboxButton toolboxButton) {
        buttonList.add( toolboxButton );
        return buttonList.size() - 1;
    }

    public Skin getLevelSkin() {
        return levelSkin;
    }

    public Table getTable() {
        return table;
    }

    public boolean isRebuildNecessary() {
        return rebuildNecessary;
    }

    public HotSpot getHotSpotRoot() {
        return hotSpotRoot;
    }

    public boolean hasItems() {
        return activeButtons > 2;
    }
}
