package com.mcminos.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 10.09.15.
 */
public class Play implements Screen, GestureListener, InputProcessor, ControllerListener {
    public final static long doubleClickFrames = Game.timeResolution / 3;
    private OrthographicCamera camera;
    private Game game;
    private PlayWindow playwindow;
    private Skin skin;
    private McMinos mcminos;
    private Audio audio;
    private SpriteBatch stageBatch;
    private SpriteBatch gameBatch;
    private SpriteBatch backgroundBatch;
    private SpriteBatch miniBatch;
    private ShapeRenderer miniScreenBackground;
    private ShapeRenderer box;

    private Stage stage;
    private Main main;
    private Level level;
    private long lastZoomTime = 0;
    TextureRegion background;
    private Touchpad touchpad;

    private BitmapFont font;
    private StringBuilder score = new StringBuilder(8);
    private StringBuilder livesScore = new StringBuilder(2);
    private StringBuilder framerateScore = new StringBuilder(2);

    private Toolbox toolbox;
    private PlayDialogs dialogs;
    private boolean menusActivated = true;
    private long toolboxRebuildTimePoint = 0;
    public final static int offScreen=-0xffffff;
    private int destinationX = offScreen; // if this is >=0 a destination or door to open needs to be selected
    private int destinationY = offScreen;
    private final static long lastTouchInPast = -16 * doubleClickFrames; // too far in history to be noted;
    private long lastTouchDown = lastTouchInPast; // important to detect double click on door
    private long lastTouchUp = lastTouchInPast;
    private long panning = 0;
    static final long panScrollBackPause = 60; // wait how long until slowly scrolling back in pan-mode

    private boolean paused = true; // start paused
    private Preferences preferences;
    private Fader fader;
    private HotSpot hotSpotSelected = null;
    private Vector2 coords = new Vector2();
    private long lastControllerGameFrame;
    private int evaluateDirectionsLastDirs = 0;
    private BitmapFont pauseFont;


    private void preInit(final Main main) {
        this.main = main;
        this.preferences = main.getPreferences();
        camera = new OrthographicCamera();
        skin = main.getLevelSkin(getSymbolResolution());
        audio = main.getAudio();
        // don't conflict with gameBatch
        gameBatch = new SpriteBatch();
        stageBatch = new SpriteBatch();
        backgroundBatch = new SpriteBatch();
        miniBatch = new SpriteBatch();
        miniBatch.enableBlending();
        miniBatch.setColor(1,1,1,0.7f);
        miniScreenBackground = new ShapeRenderer();
        box = new ShapeRenderer();
//        background = Entities.backgrounds_punched_plate_03;
        background = Entities.backgrounds_amoeboid_01.getTexture(Preferences.MAXRES,0);
        fader = new Fader(main,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
    }

    public Play(Main main, LevelConfig levelConfig, int score, int lives ) {
        if(levelConfig==null) {
            backToMenu();
            return;
        }
        preInit(main);
        loadLevel(levelConfig);
        initAfterLevel();
        mcminos.setScore( score );
        mcminos.setLives( lives );
    }

    /**
     * no levelName-> resume from saved state
     *
     * @param main
     */
    public Play(final Main main, int resumeSlot) {
        preInit(main);
        if(!resumeLevel(resumeSlot)) {
            game = null;
            mcminos = null;
        } else {
            initAfterLevel();
        }
    }

    private boolean resumeLevel(int resumeSlot) {
        game = new Game(main, this);
        if( ! game.loadGame(resumeSlot) ) {
            return false;
        }
        level = game.getLevel();
        // start the own timer (which triggers also the movement)
        game.initEventManager();
        return true;
    }

    public void loadLevel(LevelConfig levelConfig) {
        // Prepare the control layer
        game = new Game(main, this);
        level = game.levelNew(levelConfig);
        // start the own timer (which triggers also the movement)
        game.initEventManager();
    }

    public void initAfterLevel() {
        lastControllerGameFrame = 0;

        triggerFade();

        audio.musicRandom();

        mcminos = game.getMcMinos(); // only works after level has been loaded/initialized

        // prepare stuff for graphics output
        playwindow = new PlayWindow(main, gameBatch, camera, level, mcminos);

        // init resolution
        setGameResolution(preferences.getGameResolution());

        stage = new Stage(new ScreenViewport(), stageBatch); // Init stage
        toolbox = new Toolbox(this);
        dialogs = new PlayDialogs(this);

        // virtual joystick (called touchpad in libgdx)
        touchpad = new Touchpad(32, skin);
        Color tpColor = touchpad.getColor();
        touchpad.setColor(tpColor.r, tpColor.g, tpColor.b, 0.7f);
        touchpadResize();
        touchpad.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!isPaused()) {
                    mcminos.updateTouchpadDirections(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());
                    if (mcminos.getKeyDirections() > 0 && !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                        game.startMovement();
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

        Controllers.clearListeners();
        Controllers.addListener(this);


        pauseOn(); // make sure it's active and game is paused

        // activate level
        main.getStatistics().activate(level.getLevelConfig());
        dialogs.openLevelStory();
    }

    public boolean isTouchpadActive() {
        return touchpad !=null && touchpad.hasParent();
    }

    public boolean toggleTouchpad() {
        if (touchpad.hasParent()) {
            touchpad.remove();
            preferences.setTouchpadActive(false);
            return false; // it's gone
        } else {
            touchpadResize();
            stage.addActor(touchpad);
            preferences.setTouchpadActive(true);
            return true; // now it's visible
        }
    }


    private void touchpadResize() {
        int width = Gdx.graphics.getWidth();
        int tpwidth = width / 4;
        int height = Gdx.graphics.getHeight();
        touchpad.setSize(tpwidth, tpwidth);
        touchpad.setDeadzone(tpwidth / 5);
        touchpad.setPosition(width * 3 / 4, 0);
    }

    public void advanceToNextLevel() {
        // save what we want to carry over
        int score = mcminos.getScore();
        int lives = mcminos.getLives();
        LevelConfig currentLevelConfig = level.getLevelConfig();
        LevelConfig nextLevelConfig = currentLevelConfig.getNextLevel();
        if(nextLevelConfig != null) { // there is a next level
            this.dispose();
            main.setScreen(new Play(main, nextLevelConfig, score, lives));
        } else {
            LevelCategory c = currentLevelConfig.getCategory();
            int categoryNr = c.getNr();
            if(categoryNr < main.getLevelsConfig().size()-1 ) {
                // one is following -> unlock
                LevelCategory nc = main.getLevelsConfig().get(categoryNr+1);
                nextLevelConfig = nc.get(0);
                main.getStatistics().activate(nextLevelConfig);
            }
            this.dispose();
            Game.getSaveFileHandle(0).delete();
            main.levelEndCongrats(currentLevelConfig);
        }
    }

    public void backToMenu() {
        this.dispose();
        Game.getSaveFileHandle(0).delete();
        main.activateMainMenu( level.getLevelConfig() );
    }

    @Override
    public void show() {

    }


    @Override
    public void render(float delta) {
        /// check if single click occurred
        long gameFrame = game.getRealFrame();
        if (destinationX != offScreen) {
            if( gameFrame - lastTouchDown > doubleClickFrames) { // there was a single click
                // attention: this is called every frame until touchUp
                //Gdx.app.log("render","singleclick occured lastTouchDown="+lastTouchDown+" gameFrame="+gameFrame);

                setDestination();
                //destinationX = offScreen; // will be lifted by touchUp, needs not to be relased to allow setting when still pressed
            }
            if(lastTouchUp > lastTouchDown) {
                int x = windowToGameX(destinationX);
                int y = windowToGameY(destinationY);
                if(level.getLevelBlockFromVPixelRounded(x,y).hasDoor()) {
                    if (gameFrame > lastTouchDown + doubleClickFrames) { // give grace period of double click
                        // reset monitoring
                        lastTouchUp = lastTouchInPast;
                        destinationX = offScreen;
                        //Gdx.app.log("render","touchup delayed lifted at gameFrame="+gameFrame);
                    }
                } else {
                    lastTouchUp = lastTouchInPast;
                    destinationX = offScreen;
                    //Gdx.app.log("render","touchup directly lifted at gameFrame="+gameFrame);
                }
            }
        }
        /////// Handle timing events (like moving and events)
        if(fader.isActive()) { // if this is the fading out handle exit events in the end

        } else { // only handle when we are not transitioning
            dialogs.updateTimer();
            if (!game.updateTime()) { // update and exit, if game finished
                if (mcminos.isWinning()) { // This level was actually won
                    advanceToNextLevel();
                } else { // lost
                    backToMenu();
                }
                return;
            }
        }

        //////// Handle drawing
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (menusActivated) {
            if (toolbox.isRebuildNecessary()) {
                if (toolboxRebuildTimePoint > 0) { // already scheduled
                    if (game.getRealFrame() >= toolboxRebuildTimePoint) {
                        toolboxRebuildTimePoint = 0;
                        toolbox.rebuild();
                    }
                } else { // set schedule time
                    toolboxRebuildTimePoint = game.getRealFrame() + Game.timeResolution / 8;
                }
            }

            backgroundBatch.begin();
            int xoffset = background.getRegionWidth();
            int yoffset = background.getRegionHeight();
            for (int x = 0; x < playwindow.getWidthInPixels() + playwindow.resolution; x += xoffset) {
                for (int y = 0; y < playwindow.getHeightInPixels() + playwindow.resolution; y += yoffset) {
                    backgroundBatch.draw(background, x, y);
                }
            }
            backgroundBatch.end();
        }

        if (!isPaused()) {
            panning = 0;
            playwindow.updateCoordinates(Math.max(1,mcminos.getSpeed()), getSymbolResolution()); // fix coordinates and compute scrolling else coordinates come from panning
        } else if ( panning == 0 ) { // if nobody is currently looking
            playwindow.updateCoordinates(1, getSymbolResolution()); //slowly scroll back
        } else {
            panning --;
        }

        gameBatch.begin();

        gameBatch.flush();
        ScissorStack.pushScissors(playwindow.getScissors());

        playwindow.draw(menusActivated);

        gameBatch.flush();
        ScissorStack.popScissors();


        gameBatch.end(); // must end before other layers

        if (menusActivated) {
            renderScore(); // score etc.

            // only draw minimap if parts of map are not visible (be one half block tolerant in favor of not drawing)
            if(level.getVPixelsWidth() > playwindow.getVisibleWidthInVPixels() + PlayWindow.virtualBlockResolution/2
                    || level.getVPixelsHeight() > playwindow.getVisibleHeightInVPixels() + PlayWindow.virtualBlockResolution/2 ) {
                // draw a dark transparent rectangle to have some background for mini screen
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                miniScreenBackground.begin(ShapeRenderer.ShapeType.Filled);
                miniScreenBackground.setColor(0, 0, 0, 0.4f); // a quite transparent
                miniScreenBackground.rect(playwindow.getMiniX(),
                        playwindow.getMiniY(),
                        (level.getWidth() + 2) * playwindow.virtual2MiniResolution,
                        (level.getHeight() + 2) * playwindow.virtual2MiniResolution);
                miniScreenBackground.end();

                // mini screen
                miniBatch.begin();
                playwindow.drawMini(miniBatch);
                miniBatch.end();

                drawVisibleMarker();
            }

            toolbox.update(); // update toolbox based on inventory

            // add stage and menu
            stage.draw();
            stage.act(delta); // evaluate interaction with menu
            evaluateDirections();

            // draw selected hotspot
            if(hotSpotSelected != null) {
                Actor a = hotSpotSelected.getActor();
                if(a != null) {
                    int res = preferences.getSymbolResolution();
                     stageBatch.setColor(1, 1, 1, 1); // restore batch color
//                Vector2 coords = new Vector2(a.getX(),a.getY());
                    coords.x = 0;
                    coords.y = 0;
                    a.localToStageCoordinates(/*in/out*/coords);
                    //stage.stageToScreenCoordinates(/*in/out*/coords);
                    stageBatch.begin();
                    stageBatch.draw(Entities.destination.getTexture(res, gameFrame), coords.x, coords.y);
                    stageBatch.end();
                }
            }

            // add fader shade
            fader.render();
        }
    }

    private void setDestination() {
        if (!mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
            pauseOff();
        }
        int x = windowToGameX(destinationX);
        int y = windowToGameY(destinationY);
        mcminos.setDestination(playwindow, x, y);
    }

    /**
     * draw score, energy bars and time used or time left
     */
    static final int livesMaxDraw = 5;
    static final int maxBars = 4;
    int[] barLengths = new int[maxBars]; // power, umbrella, poison/alcohol
    Color[] barColors = new Color[maxBars];
    private void renderScore() {
        SpriteBatch batch = stageBatch;
        int v; // amount of specific energy
        int bars = 0; // how many energy bars to draw;
        int barHeight = 0; // height of one bar
        int res = preferences.getSymbolResolution()/2;
        int x = res * 2 + (res/4);
        int yText = Gdx.graphics.getHeight() - res/8;
        int ySymbol = yText - res + res/16;
        boolean addDiff;
        GlyphLayout layout;

        batch.begin();

        batch.setColor(1,1,1,0.7f);

//        score.setLength(0);
        /*score.append(mcminos.getScore());
        batch.draw(Entities.level_score.getTexture(res,0),x,ySymbol);
        x += res + res/8;
        layout = font.draw(batch, score, x, yText );
        x += layout.width + res/4;*/

//        framerateScore.setLength(0);
//        framerateScore.append("F");
//        framerateScore.append(Gdx.graphics.getFramesPerSecond());
//        layout = font.draw(batch, framerateScore, x, yText );
//        x += layout.width + res/4;

        // avoid too much moving, due to variable font size
////        x = Math.max(x, res * (2 + Math.max(5,score.length() + framerateScore.length()) ));
//        x = Math.max(x, res * 5);

        livesScore.setLength(0);
        int lives = mcminos.getLives();
        int livesDraw = Math.min(lives,livesMaxDraw);
        if(lives>livesMaxDraw) {
            livesScore.append(lives);
        } else if(lives>99) {
            livesScore.append("++");
        }
        for(int i=0; i<livesDraw; i++) {
            batch.draw(Entities.pills_heart.getTexture(res,0),x+res*i/4,ySymbol);
        }
        x += res/2;
        layout = font.draw(batch, livesScore, x, yText );
        x += Math.max( layout.width, (livesDraw-2)*res/4 + res );

        if (mcminos.isMirrored()) {
            batch.draw(Entities.extras_mirror.getTexture(res,0),x,ySymbol);
            x+= res + res/4;
        }

        addDiff = false;
        if(mcminos.getMover().isAccelerated()) {
            batch.draw(Entities.fields_field_speed_up.getTexture(res,0),x,ySymbol);
            x+= res/4;
            addDiff = true;
        }
        if( level.getLevelConfig().getMcMinosSpeed() > 1 ) {
            batch.draw(Entities.fields_field_speed_up.getTexture(res,0),x,ySymbol);
            x+= res/4;
            addDiff = true;
        }

        if(addDiff) x += res;

        if(isPaused()) {
            if(((game.getRealFrame()/60)%2)==0){
                int w = Gdx.graphics.getWidth();
                pauseFont.draw(batch, "P A U S E", 0, stage.getHeight()/2+res, w, Align.center, false);
            }
        }

        batch.end();

        v = mcminos.getPowerDuration();
        if (v > 0) {
            barLengths[bars] = v;
            barColors[bars] = Color.ORANGE;
            bars ++;
        }
        v = mcminos.getUmbrellaDuration();
        if (v > 0) {
            barLengths[bars] = v;
            barColors[bars] = Color.BLUE;
            bars ++;
        }
        v = mcminos.getDrunkLevel();
        if (v > 0) {
            barLengths[bars] = v;
            barColors[bars] = Color.PURPLE;
            bars ++;
        }
        v = mcminos.getPoisonDuration();
        if (v > 0) {
            barLengths[bars] = v;
            barColors[bars] = Color.GREEN;
            bars ++;
        }

        if(bars>0) barHeight = res / bars;


        int yBars = ySymbol;
        box.begin(ShapeRenderer.ShapeType.Filled);
        for(int i=bars-1; i >= 0; i--) {
            box.setColor(barColors[i]); // a little transparent
            box.rect(x,
                    yBars,
                    res*barLengths[i] / Game.timeResolution / 5, // fill two fileds for two seconds
                    barHeight);
            yBars += barHeight;
        }
        box.end();
    }

    private void drawVisibleMarker() {

        // visible area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        miniScreenBackground.begin(ShapeRenderer.ShapeType.Filled);
        miniScreenBackground.setColor(255, 128, 0, 0.5f); // orange transparent

        // These are up to 8 lines (4 corners) to draw
        int t = playwindow.virtual2MiniResolution / 2; // line thickness
        int t2 = t * 2;
        int thickness = 1 + t;
        // compute the visible area lower left corner
        int x0 = Graphics.virtualToMiniX(playwindow, level, playwindow.windowVPixelXPos, 0);
        int y0 = Graphics.virtualToMiniY(playwindow, level, playwindow.windowVPixelYPos, 0);
        // compute the upper right corner
        int x1 = Graphics.virtualToMiniX(playwindow, level, ((playwindow.windowVPixelXPos + playwindow.getVisibleWidthInVPixels() - 1) % level.getVPixelsWidth()), 0);
        int y1 = Graphics.virtualToMiniY(playwindow, level, ((playwindow.windowVPixelYPos + playwindow.getVisibleHeightInVPixels() - 1) % level.getVPixelsHeight()), 0);
        // lower left corner of mini-screen
        int mx0 = Graphics.virtualToMiniX(playwindow, level, 0, 0);
        int my0 = Graphics.virtualToMiniY(playwindow, level, 0, 0);
        // upper right corner of mini-screen
        int mx1 = Graphics.virtualToMiniX(playwindow, level, level.getVPixelsWidth() - 1, 0);
        int my1 = Graphics.virtualToMiniY(playwindow, level, level.getVPixelsHeight() - 1, 0);

        if (x0 < x1) { // normal, no split
            miniScreenBackground.rect(x0 - t, y0 - t, x1 - x0 + t2 + 1, thickness);
            miniScreenBackground.rect(x0 - t, y1, x1 - x0 + t2 + 1, thickness);
        } else { // split necessary x1 < x0
            miniScreenBackground.rect(mx0 - t, y0 - t, x1 - mx0 + t2 + 1, thickness);
            miniScreenBackground.rect(x0 - t, y0 - t, mx1 - x0 + t2, thickness);
            miniScreenBackground.rect(mx0 - t, y1, x1 - mx0 + t2 + 1, thickness);
            miniScreenBackground.rect(x0 - t, y1, mx1 - x0 + t2, thickness);
        }
        if (y0 < y1) { // normal, no split
            miniScreenBackground.rect(x0 - t, y0 + 1, thickness, y1 - y0 - 1);
            miniScreenBackground.rect(x1, y0 + 1, thickness, y1 - y0 - 1);
        } else { // split necessary y1 < y0
            miniScreenBackground.rect(x0 - t, my0 + 1, thickness, y1 - my0 - 1);
            miniScreenBackground.rect(x0 - t, y0 + 1, thickness, my1 - y0 - 1);
            miniScreenBackground.rect(x1, my0 + 1, thickness, y1 - my0 - 1);
            miniScreenBackground.rect(x1, y0 + 1, thickness, my1 - y0 - 1);
        }

        miniScreenBackground.end();

    }

    @Override
    public void resize(int width, int height) {
        skin = main.getLevelSkin(getSymbolResolution());
        panning = 0; // stop panning
        Matrix4 matrix = new Matrix4();
        matrix.setToOrtho2D(0, 0, width, height);
        backgroundBatch.setProjectionMatrix(matrix);
        miniBatch.setProjectionMatrix(matrix);
        stageBatch.setProjectionMatrix(matrix);
        miniScreenBackground.setProjectionMatrix(matrix);

        playwindow.resize(width, height, preferences.getSymbolResolution());
        fontResize();
        //menuTable.setBounds(0, 0, width, height);
        //toolboxTable.setBounds(0, 0, width, height); no these are fixed in little window
        stage.getViewport().update(width, height, true);
        //toolboxTable.setSize(width / 3, height * 4 / 5);
        toolbox.resize();
        touchpadResize();
        fader.resize(width, height);
    }

    public void resize() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void fontResize() {
        int fontRes = playwindow.resolution / 2;
        if (fontRes < 32) fontRes = 32;
        if (fontRes > 128) fontRes = 128;
        font = main.getLevelFont(fontRes);
        font.setColor(1, 1, 1, 0.8f);
        int pauseRes = playwindow.resolution;
        pauseFont = main.getLevelFont(pauseRes);
        pauseFont.setColor(1,1,1,0.8f);
    }

    @Override
    public void pause() {
        game.saveGame(0);
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        fader.dispose();
        game.dispose();
        stage.dispose();
        gameBatch.dispose();
        backgroundBatch.dispose();
        miniBatch.dispose();
        stageBatch.dispose();
        miniScreenBackground.dispose();
        box.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        //Gdx.app.log("keyDown","keyCode: " + keycode);
        evaluateDirections();
        return false;
    }

    private void evaluateDirections() {
        mcminos.updateKeyDirections();
        if (isPaused()) {
            long gameFrame = game.getRealFrame();
            if (gameFrame - lastControllerGameFrame > 40) {
                int dirs = mcminos.getKeyDirections();
                if ( !mcminos.isWinning() && !mcminos.isKilled() && !mcminos.isFalling()) {
                    if(dirs > 0) {
                        if (hotSpotSelected == null) {
                            if(!dialogs.active()) pauseOff();
                        }
                        else {
                            if ((dirs & Mover.UP) > 0)
                                moveCursor(Mover.UP);
                            else if ((dirs & Mover.RIGHT) > 0)
                                moveCursor(Mover.RIGHT);
                            else if ((dirs & Mover.DOWN) > 0)
                                moveCursor(Mover.DOWN);
                            else if ((dirs & Mover.LEFT) > 0)
                                moveCursor(Mover.LEFT);
                        }
                        evaluateDirectionsLastDirs = dirs;
                    } else { // just became 0
                        if(evaluateDirectionsLastDirs>0) {
                            dialogs.checkDoorKey(evaluateDirectionsLastDirs);
                            evaluateDirectionsLastDirs = 0;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode) {
            case 23: //amazon fire remote select
                triggerAction();
                return true;
            case 82: // amazon fire menu
                toggleGameMenu();
                return true;
            case 85: // amazon fire play/pause
                togglePause();
                return true;
            case 89: // amazon fire wind back
                break;
            case 90: // amazon fire wind forward
                break;
            default:
                evaluateDirections();
        }

//        dialogs.checkDoorKey(keycode); // TODO: check if this can be done in evaluate
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        //Gdx.app.log("keyTyped"," character: " + character);
        switch (character) {
            case '+':
                zoomPlus();
                resize();
                break;
            case '-':
                zoomMinus();
                resize();
                break;
            case '<':
                decreaseSymbolResolution();
                break;
            case '>':
                increaseSymbolResolution();
                break;
            case '1':
                pauseOn();
                toolbox.activateChocolate();
                break;
            case '2':
                pauseOn();
                dialogs.openDoorOpener();
                break;
            case '3':
                pauseOn();
                toolbox.activateBomb();
                break;
            case '4':
                pauseOn();
                toolbox.activateDynamite();
                break;
            case '5':
                pauseOn();
                toolbox.activateLandmine();
                break;
            case '6':
                pauseOn();
                toolbox.activateUmbrella();
                break;
            case '7':
                pauseOn();
                toolbox.activateMedicine();
                break;
            case '9':
                ScreenshotFactory.saveScreenshot();
                break;
            case '0':
                menusActivated = !menusActivated;
                break;
            case 27: // Escape
            case 't':
            case 'T':
            case 'm':
            case 'M':
                toggleGameMenu();
                break;
            case ' ':
            case 13:
                triggerAction();
                break;
            case 'p':
            case 'P':
                togglePause();
                break;
            default:
//                evaluateDirections();
                break;
        }
        return false;
    }

    private void toggleGameMenu() {
        if(hasDialog()) togglePause();
        else {
            pauseOn();
            dialogs.openGameMenu();
            hotSpotSelected = dialogs.getHotSpotRoot();
        }
    }

    private void togglePause() {
        if (isPaused()) {
            if(hasDialog()) {
                closeDialog(); // first close dialog
                hotSpotSelected = toolbox.getHotSpotRoot().getDown();
            }
            else pauseOff();
        } else {
            pauseOn();
            hotSpotSelected = toolbox.getHotSpotRoot();
        }
    }

    private void triggerAction() {
        if(!isPaused()) {
            togglePause();
        } else {
            if(hotSpotSelected != null ) { // something is selected
                int hint = hotSpotSelected.getActivateHint();
                if(hint < 100) {
                    dialogs.triggerAction(hint);
                } else {
                    switch (hint) {
                        case 100:
                            togglePause();
                            break;
                        case 101:
                            toggleGameMenu();
                            break;
                        case 102:
                            toolbox.activateChocolate();
                            break;
                        case 103:
                            hideHotSpot();
                            dialogs.openDoorOpener();
                            break;
                        case 104:
                            toolbox.activateBomb();
                            break;
                        case 105:
                            toolbox.activateDynamite();
                            break;
                        case 106:
                            toolbox.activateLandmine();
                            break;
                        case 107:
                            toolbox.activateUmbrella();
                            break;
                        case 108:
                            toolbox.activateMedicine();
                            break;
                    }
                }
            } else {
                togglePause();
            }
        }
    }

    public void zoomPlus() {
        playwindow.setResolution(preferences.getGameResolution()*2, preferences.getSymbolResolution());
        resize();
    }

    public void zoomMinus() {
        playwindow.setResolution(preferences.getGameResolution()/2, preferences.getSymbolResolution());
        resize();
    }

    private boolean destinationDown(int screenX, int screenY, int button, boolean detectDoorDoubleClick) {
        if (!isPaused()) { // just pan in this case or wait for a registered click -> see tap
            if (button > 0) return false;
            destinationX = screenX;
            destinationY = screenY;
            if (detectDoorDoubleClick) {
                int x = windowToGameX(screenX);
                int y = windowToGameY(screenY);
                LevelBlock lb = level.getLevelBlockFromVPixelRounded(x, y);
                if (lb.hasDoor() && blockDistance(lb, mcminos.getLevelBlock()) <= 2) { // ok, be careful, somebody clicked on a nearby door
                    long gameFrame = game.getRealFrame();
                    if (gameFrame - lastTouchDown > doubleClickFrames) { // this is not part of a double click
                        lastTouchDown = gameFrame;
                        //Gdx.app.log("destinationDown","lastTouchDown="+lastTouchDown);
                    }
                    return true;
                }
            }
            // it's not a near door or doubleclick is not monitored
            lastTouchDown = lastTouchInPast;
            return true; // handled
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return destinationDown(screenX, screenY, button, true);
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
        return windowToGame(Gdx.graphics.getHeight() - screenY, level.getVPixelsHeight(),
                playwindow.getProjectionY(), playwindow.windowVPixelYPos, level.getScrollY());
    }

    public int windowToGameX(int screenX) {
        return windowToGame(screenX, level.getVPixelsWidth(),
                playwindow.getProjectionX(), playwindow.windowVPixelXPos, level.getScrollX());
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        lastTouchUp = game.getRealFrame();
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        //return destinationDown(screenX,screenY,pointer,0);
        return false;
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
        return destinationDown((int) x, (int) y, button, true);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (dialogs.active()) return false; //here we don't handle these events
        if (button > 0) {
            if (isPaused()) {
                pauseOff();
            } else {
                pauseOn();
            }
            return true;
        }
        if (count > 1) { // Double click
            // only register, when in own double-click time
            if (game.getRealFrame() - lastTouchDown <= doubleClickFrames) {
                return tryDoor((int) x, (int) y);
            }
            return false;
        }
        // this was a single tap
        if (isPaused()) {
            pauseOff(); // this exits eventually toolbox-mode
            destinationDown((int) x, (int) y, button, false);
            setDestination();
            destinationX = offScreen;
            //Gdx.app.log("tap","single tap at gameFrame="+game.getAnimationFrame());

            return true;
        }
        // else will have been registered in touchdown and handled there
        return false;
    }

    private boolean tryDoor(int x, int y) {
        int vx = windowToGameX(x);
        int vy = windowToGameY(y);
        LevelBlock lb = level.getLevelBlockFromVPixelRounded(vx, vy);
        if (lb.hasDoor()) {
            //Gdx.app.log("tryDoor","trying to open door"+game.getAnimationFrame());
            destinationX = offScreen; // cancel destination
            // TODO: does the radius need to be better checked to allow only neighboring doors?
            int delta = blockDistance(lb, mcminos.getLevelBlock());
            if (delta <= 2 && delta > 0) {
                dialogs.toggleDoor(lb);
                return true;
            }
        }
        return false;
    }

    private int blockDistance(LevelBlock lb1, LevelBlock lb2) {
        int dx = Math.abs(lb2.getX() - lb1.getX());
        if(level.getScrollX() && dx > 2) dx=level.getWidth()-dx;
        int dy = Math.abs(lb2.getY() - lb1.getY());
        if(level.getScrollY() && dy > 2) dy=level.getWidth()-dy;
        return dx+dy;
    }

    @Override
    public boolean longPress(float x, float y) {
        return tryDoor((int) x, (int) y);
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
        if (isPaused()) {
            panning = panScrollBackPause; // approx two seconds delay before moving back
            int dxi = Util.shiftLeftLogical((int) deltaX, PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent);
            int dyi = Util.shiftLeftLogical((int) deltaY, PlayWindow.virtualBlockResolutionExponent - playwindow.resolutionExponent);

            if(level.getScrollX()) {
                playwindow.windowVPixelXPos = (playwindow.windowVPixelXPos + level.getVPixelsWidth() - dxi) % level.getVPixelsWidth();
            } else {
                playwindow.windowVPixelXPos = Math.max(Math.min(playwindow.windowVPixelXPos - dxi, level.getVPixelsWidth() - playwindow.getVisibleWidthInVPixels()), 0);
            }
            if(level.getScrollY()) {
                playwindow.windowVPixelYPos = (playwindow.windowVPixelYPos + level.getVPixelsHeight() + dyi) % level.getVPixelsHeight();
            } else {
                playwindow.windowVPixelYPos = Math.max(Math.min(playwindow.windowVPixelYPos + dyi, level.getVPixelsHeight() - playwindow.getVisibleHeightInVPixels()), 0);
            }
            return true;
        } else {
            return destinationDown((int) screenX, (int) screenY, 0, false);
        }
        //return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        // will count down itself panning = false;
        lastTouchUp = game.getRealFrame();
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (game.getRealGameTime() - lastZoomTime > 500) { // ignore some events
            if (initialDistance > distance + playwindow.visibleHeightInPixels / 4) {
                zoomMinus();
            } else if (initialDistance < distance - playwindow.visibleHeightInPixels / 4) {
                zoomPlus();
            }
            lastZoomTime = game.getRealGameTime();
        }
        return false; // consume event
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    public int getGameResolution() {
        return playwindow.resolution;
    }

    public void setGameResolution( int resolution ) {
        playwindow.setResolution(resolution, preferences.getSymbolResolution());
    }

    public Game getGame() {
        return game;
    }

    public void activateToolbox() {
        pauseOn();
    }

    public int getSymbolResolution() {
        return preferences.getSymbolResolution();
    }

    public void setSymbolResolution(int symbolResolution) {
        preferences.setSymbolResolution(symbolResolution);
        resize();
    }

    public void increaseSymbolResolution() {
        setSymbolResolution(getSymbolResolution() * 2);
    }

    public void decreaseSymbolResolution() {
        setSymbolResolution(getSymbolResolution()/2);
    }

    public Main getMain() {
        return main;
    }

    public void pauseOn() {
        game.stopTimer();
        if(!paused) {
            paused = true;
            toolbox.rebuild();
        }
    }

    public void pauseOff() {
        if( paused ) {
            paused = false;
            dialogs.close();
            toolbox.rebuild();
            hotSpotSelected = null;
            evaluateDirectionsLastDirs = 0;
        }
        game.startTimer();
    }

    public boolean isPaused() {
        return paused;
    }

    public PlayWindow getPlayWindow() {
        return playwindow;
    }

    public Stage getStage() {
        return stage;
    }

    public void dialogGameMenu() {
        dialogs.openGameMenu();
    }

    public void dialogDoorOpener() {
        dialogs.openDoorOpener();
    }

    public boolean hasDialog() {
        return dialogs.active();
    }

    public void closeDialog() {
        dialogs.close();
    }

    public void triggerFade() {
        fader.fadeOutIn();
    }

    @Override
    public void connected(Controller controller) {
        Gdx.app.log("connected","Controller: "+controller.getName());
    }

    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("disconnected","Controller: "+controller.getName());
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        //Gdx.app.log("buttonDown","Controller: "+controller.getName()
        //        + " buttonCode: " + buttonCode);
        triggerAction();
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        //Gdx.app.log("buttonUp", "Controller: " + controller.getName()
        //        + " buttonCode: " + buttonCode);
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        //Gdx.app.log("axisMoved","Controller: "+controller.getName()
        //        + " axisCode: " + axisCode
        //        + " value: " + value);
        evaluateDirections();
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        //Gdx.app.log("povMoved","Controller: "+controller.getName()
        //        + " povCode: " + povCode
        //        + " PovDirection: " + value);
        evaluateDirections();
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        //Gdx.app.log("xSliderMoved","Controller: "+controller.getName()
        //        + " slidercode: " + sliderCode
        //        + " boolval: " + value);
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        //Gdx.app.log("ySliderMoved","Controller: "+controller.getName()
        //        + " slidercode: " + sliderCode
        //        + " boolval: " + value);
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        //Gdx.app.log("accelerometerMoved","Controller: "+controller.getName()
        //        + " accelerometerCode: " + accelerometerCode
        //        + " Vector3: " + value);
        return false;
    }

    private void moveCursor(int direction) {
        HotSpot newHS = hotSpotSelected;
        switch(direction) {
            case Mover.UP:
                newHS = hotSpotSelected.getUp();
                break;
            case Mover.RIGHT:
                newHS = hotSpotSelected.getRight();
                break;
            case Mover.DOWN:
                newHS = hotSpotSelected.getDown();
                break;
            case Mover.LEFT:
                newHS = hotSpotSelected.getLeft();
                break;
        }
        if(newHS != null) {
            lastControllerGameFrame = game.getRealFrame();
            hotSpotSelected = newHS;
            // scroll underlying scrollpane
            ScrollPane pane = hotSpotSelected.getScrollPane();
            if(pane != null) {
                Actor a = hotSpotSelected.getActor();
                pane.setScrollX(a.getX() - pane.getScrollWidth() / 2);
                pane.setScrollY(pane.getMaxY() + pane.getScrollHeight() / 2 - a.getY());
            }
        }
    }

    public void setHotSpotSelected(HotSpot hotSpotSelected) {
        this.hotSpotSelected = hotSpotSelected;
    }

    public void setHotSpotSettings() {
        hotSpotSelected = toolbox.getHotSpotRoot().getDown();
    }

    public void hideHotSpot() {
        hotSpotSelected = null;
    }
}
