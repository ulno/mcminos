package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenu implements Screen {

    private boolean resumeRequested = false; // only resume, if resume-file detected
    private Skin levelSkin;
    private Skin menuSkin;
    private final Stage stage;
    private final Table table;
    private final Texture bg;
    private final Image bgimage;
    private final SelectBox sb;
    private final SpriteBatch batch;
    private final Main main;
    private final Table rootTable;
    private final Label versionStringActor;

    private boolean fullscreen = Game.preferencesHandle.getBoolean("fs");


    public MainMenu(final Main main, String levelPreselect) {
        final MainMenu thisScreen = this;
        this.main = main;
        batch = main.getBatch();
        int res = main.getSymbolResolution();
        menuSkin = main.getMenuSkin(res/2);
        levelSkin = main.getLevelSkin(res/2);


/*        menuSkin.remove("default-font",BitmapFont.class);
//        menuSkin.remove("font_liberation_sans-_regular_16pt",BitmapFont.class);
        menuSkin.add("default-font", main.getFont(128), BitmapFont.class);
//        menuSkin.add("font_liberation_sans-_regular_16pt", fontList.get(128));
        BitmapFont skinFont = menuSkin.getFont("default-font");
        skinFont.getData().setScale(4.0f);
//        skinFont = menuSkin.getFont("font_liberation_sans-_regular_16pt");
//        skinFont.getData().setScale(2.0f); */

//        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/myFont.ttf"));
//        BitmapFont font = generator.generateFont(14);
        bg = new Texture( Gdx.files.internal("images/loadscreen.png"));
        bgimage = new Image(bg);
        bgimage.setZIndex(0);
        bgimage.setScaling(Scaling.none);
        Util.scaleBackground(bgimage);

        stage = new Stage(new ScreenViewport(), batch);

        // root table covering the screen
        rootTable = new Table();
        rootTable.setPosition(0,0);
        // table for buttons
        table = new Table();
        rootTable.add(table).top().center();
//        table.setWidth(stage.getWidth());
//        table.align(Align.center | Align.top);

        TextButton startButton = new TextButton("Start", menuSkin);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                thisScreen.dispose();
                main.setScreen(new Play(main, (String) sb.getSelected(), 0, 3));
            }
        });

        TextButton resumeButton = new TextButton("Load", menuSkin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                thisScreen.dispose();
                main.setScreen(new Play(main,1)); // TODO: allow more slots
            }
        });

        TextButton endButton = new TextButton("End", menuSkin);
        endButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
/*
                thisScreen.dispose();
                main.dispose();*/
            }
        });

        /*Label text1 = new Label( "Level 1", menuSkin );
        Label text2 = new Label( "Level 2", menuSkin );
        Label text3 = new Label( "Level 3", menuSkin );

        Table scrollTable = new Table();
        scrollTable.add(text1).row();
        scrollTable.add(text2).row();
        scrollTable.add(text3).row();

        ScrollPane scroller = new ScrollPane( scrollTable );

        table.pad(32);
        table.add(scroller).fill().expand(); */

        sb = new SelectBox(menuSkin);


        sb.setItems(main.getLevelNames().toArray());
        if( levelPreselect != null && levelPreselect != "" )
            sb.setSelected(levelPreselect);

        table.add(sb)
                .top()
                .pad(res/4)
                .minSize(res*3, res*12/10)
                .row();
        table.add(startButton)
                .minSize(res*3, res*12/10)
                .row();
        table.add(resumeButton)
                .minSize(res*3, res*12/10)
                .row();
        table.add(endButton)
                .minSize(res*3, res*12/10);

        stage.addActor(bgimage);
        stage.addActor(rootTable);

        versionStringActor = new Label(main.getVersionString(), levelSkin);
        stage.addActor(versionStringActor);


        stage.addListener(new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                switch (character) {
                    case 'F': // only capital:
                        toggleFullscreen();
                        break;
                }
                return false;
            }

        });
        Gdx.input.setInputProcessor(stage);
        resize();

        // eventually continue a paused/suspended game
        if(Game.getSaveFileHandle(0).exists()) {
            resumeRequested = true;
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if( resumeRequested) {
            Play p = new Play(main,0);
            if(p.getGame() != null) { //load successfull
                dispose();
                main.setScreen(p); // resume
            } else { //if not just continue with this screen
                resumeRequested = false;
            }
        } else {
            // TODO: Create background-picture for loading screen
            stage.act(delta);
            stage.draw();
        }
    }

    public void resize() {
        resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        menuSkin = main.getMenuSkin(main.getSymbolResolution()/2);
        levelSkin = main.getLevelSkin(main.getSymbolResolution()/2);
        // TODO recreate menus, when changing size
        Util.scaleBackground(bgimage);
        rootTable.setSize(width,height);
        table.setBounds(0,0,width,height);
        stage.getViewport().update(width, height, true);
        versionStringActor.setPosition(width-4,0,Align.bottomRight); // TODO: check, why this is not lower on the screen
//        versionStringActor.setPosition(0,0);
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
        stage.dispose();
        bg.dispose();
    }

    private void toggleFullscreen() {
        if(!fullscreen) {
            fullscreen = true;
            //DesktopLauncher.run(false);

/*            com.badlogic.gdx.Graphics.DisplayMode m = null;
            for(com.badlogic.gdx.Graphics.DisplayMode mode: Gdx.graphics.getDisplayModes()) {
                Gdx.app.log("mode",mode.toString());
                if(m == null) {
                    m = mode;
                } else {
                    if(m.width < mode.width) {
                        m = mode;
                    }
                }
            }
            Gdx.graphics.setDisplayMode(m); */
            //Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
//            Gdx.graphics.setDisplayMode(1920, 1080, true);
//            Gdx.graphics.setDisplayMode(1280, 720, true);
//            Gdx.graphics.setVSync(true);
            //TODO: setting here to fs does not work
        } else {
            fullscreen = false;
            Gdx.graphics.setDisplayMode(1280, 900, false);
        }
        Game.preferencesHandle.putBoolean("fs", fullscreen);
        Game.preferencesHandle.flush();
    }
}
