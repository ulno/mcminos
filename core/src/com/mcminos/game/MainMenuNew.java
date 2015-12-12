package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

/**
 * Created by ulno on 11.09.15.
 */
public class MainMenuNew implements Screen {
    private final LevelsConfig levelsConfig;
    private boolean resumeRequested = false; // only resume, if resume-file detected
    private Skin bigMenuSkin;
    private Skin levelSkin;
    private Skin textSkin;
    private Skin bigLevelSkin;
    private final Stage stage;
    private final Table table;
    private final TextureRegion bg;
    private final SpriteBatch batch;
    private final Main main;
    private final Table rootTable;
    private Label versionStringActor;

    private ArrayList<Texture> categoryButtonImages = new ArrayList();

    private boolean fullscreen = Game.preferencesHandle.getBoolean("fs");
    private int levelCategory;


    public MainMenuNew(final Main main, String levelPreselect) {
        final MainMenuNew thisScreen = this;
        this.main = main;
        batch = main.getBatch();
        levelsConfig = main.getLevelConfig();
        bg = Entities.backgrounds_amoeboid_01.getTexture(128,0); // can be fixed as bg is not so critical

        stage = new Stage(new ScreenViewport(), batch);

        // root table covering the screen
        rootTable = new Table();
        stage.addActor(rootTable);
        rootTable.setPosition(0,0);

        // table for menu
        table = new Table();
        rootTable.add(table).top().center().fill().expand();

        // read images for the category buttons
        for(int i=0; i < levelsConfig.size(); i++) {
            String path = levelsConfig.get(i).getPath();
            FileHandle fh= Gdx.files.internal("levels/"+path+"/gfx.png");
            if(fh.exists()) {
                categoryButtonImages.add(new Texture(fh));
            } else {
                categoryButtonImages.add(null);
            }
        }

        initMenu();

        // eventually continue a paused/suspended game
        if(Game.getSaveFileHandle(0).exists()) {
            resumeRequested = true;
        }
    }

    private void initMenu() {
        int res = main.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        textSkin = main.getMenuSkin(res/2);
        levelSkin = main.getLevelSkin(res/2);
        bigLevelSkin = main.getLevelSkin(res);

        table.clear();

        Label title = new Label("McMinos", bigLevelSkin);
        table.add(title).colspan(2).center().top().row();

        Table folderSelectorTable = new Table();
        // add the buttons for the differnetlevel categories to the toolbar
        class MyClickListener extends ClickListener {

            public int catecory;

            MyClickListener(int c) {
                catecory = c;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchLevelCategory(catecory);
                initMenu();
            }
        }

        for( int i=0; i< levelsConfig.size(); i++) {
            Group g = new Group();
            g.setHeight(res);
            Button b = new Button(bigMenuSkin);
            if(categoryButtonImages.get(i) != null) {
                Image img = new Image(categoryButtonImages.get(i));
                img.setSize(res,res);
                b.add(img);
            }
            b.setSize(res,res);
            g.addActor(b);
            b.addListener(new MyClickListener(i) );
            folderSelectorTable.add(g).size(res).padLeft(2);
        }

        ScrollPane folderSelector = new ScrollPane(folderSelectorTable);
        table.add(folderSelector).colspan(2).row();

        Label l = new Label(levelsConfig.get(levelCategory).getName(),levelSkin);
        table.add(l).colspan(2).left().row();

        table.add( new TextButton("left",bigMenuSkin));
        table.add( new TextButton("right",bigMenuSkin));

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
    }

    private void switchLevelCategory(int catecory) {
        levelCategory = catecory;
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
            batch.begin();
            for(int x=0; x<Gdx.graphics.getWidth(); x+=bg.getRegionWidth())
                for(int y=0; y<Gdx.graphics.getHeight(); y+=bg.getRegionHeight())
                    batch.draw(bg,x,y);
            batch.end();
            stage.act(delta);
            stage.draw();
        }
    }

    public void resize() {
        resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        int res = main.getSymbolResolution();
        bigMenuSkin = main.getMenuSkin(res);
        bigLevelSkin = main.getLevelSkin(res);
        levelSkin = main.getLevelSkin(res/2);
        textSkin = main.getMenuSkin(res/2);

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
