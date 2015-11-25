package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by ulno on 05.10.15.
 */
public class McMinos implements Json.Serializable {
    private Game game;
    private Level level;
    private McMinosMover mover;
    private int powerDuration = 0;
    private int umbrellaDuration = 0;
    private int poisonDuration = 0;
    private int drunkLevel = 0;
    private int umbrellas=0; // number of umbrellas carried by mcminos
    private int chocolates; // number of chocolates carried by mcminos
    private int bombs=0; // number of bombs carried by mcminos
    private int dynamites=0; // number of dynamites carried by mcminos
    private int keys=0; // number of keys carried by mcminos
    private int landmines=0; // number of umbrellas carried by mcminos
    private int medicines=0; // number of medicines carried by mcminos
    private int lives=3; // number of lives left
    private int score=0; // current score
    private Audio audio;
    private LevelObject levelObject;
    private boolean killed = false;
    private boolean winning = false;
    private LevelObject destination;
    private boolean destinationSet; // was a destination set (and needs to be shown)
    private boolean falling;
    private LevelBlock startBlock = null;
    private boolean destinationEnabled = true;
    private boolean mirrored = false;

    @Override
    public void write(Json json) {
        // mover?
        json.writeValue("pd", powerDuration);
        json.writeValue("ud", umbrellaDuration);
        json.writeValue("td", poisonDuration);
        json.writeValue("dl", drunkLevel);
        json.writeValue("u", umbrellas);
        json.writeValue("c", chocolates);
        json.writeValue("b", bombs);
        json.writeValue("d", dynamites);
        json.writeValue("k", keys);
        json.writeValue("l", landmines);
        json.writeValue("m", medicines);
        json.writeValue("lv", lives);
        json.writeValue("s", score);
        json.writeValue("k", killed);
        json.writeValue("w", winning);
        json.writeValue("f", falling);
        json.writeValue("m", mirrored);
        json.writeValue("lo", levelObject);
        // destination?


    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        powerDuration = json.readValue("pd",Integer.class,jsonData);
        umbrellaDuration = json.readValue("ud",Integer.class,jsonData);
        poisonDuration = json.readValue("td",Integer.class,jsonData);
        drunkLevel = json.readValue("dl",Integer.class,jsonData);
        umbrellas = json.readValue("u",Integer.class,jsonData);
        chocolates = json.readValue("c",Integer.class,jsonData);
        bombs = json.readValue("b",Integer.class,jsonData);
        dynamites = json.readValue("d",Integer.class,jsonData);
        keys = json.readValue("k",Integer.class,jsonData);
        landmines = json.readValue("l",Integer.class,jsonData);
        medicines = json.readValue("m",Integer.class,jsonData);
        lives = json.readValue("lv",Integer.class,jsonData);
        score = json.readValue("s",Integer.class,jsonData);
        killed = json.readValue("k",Boolean.class,jsonData);
        winning = json.readValue("w",Boolean.class,jsonData);
        falling = json.readValue("f",Boolean.class,jsonData);
        mirrored = json.readValue("m",Boolean.class,jsonData);

        //tmpLevelObject = new LevelObject(game.getLevel(),0,0,0, LevelObject.Types.McMinos);
        levelObject = json.readValue("lo",LevelObject.class,jsonData);
        //levelObject.setXY(tmpLevelObject.getVX(),tmpLevelObject.getVY());
    }

    public void initFromTempMcMinos( McMinos tmpmcm ) {
        level = game.getLevel();
        powerDuration = tmpmcm.powerDuration;
        umbrellaDuration = tmpmcm.umbrellaDuration;
        poisonDuration = tmpmcm.poisonDuration;
        drunkLevel = tmpmcm.drunkLevel;
        umbrellas = tmpmcm.umbrellas;
        chocolates = tmpmcm.chocolates;
        bombs = tmpmcm.bombs;
        dynamites = tmpmcm.dynamites;
        keys = tmpmcm.keys;
        landmines = tmpmcm.landmines;
        medicines = tmpmcm.medicines;
        lives = tmpmcm.lives;
        score = tmpmcm.score;
        killed = tmpmcm.killed;
        winning = tmpmcm.winning;
        falling = tmpmcm.falling;
        mirrored = tmpmcm.mirrored;
        levelObject = tmpmcm.levelObject;
//        levelObject.setXY(tmpmcm.getVX(),tmpmcm.getVY());
    }


    /**
     * Just for Json read
     */
    public McMinos() {

    }

    public McMinos(Game game) {
        this.game = game;
        this.audio = game.getAudio();
        // is null anyway at that point, that's why init needs to be called
        // this.level = game.getLevel();
    }

    /*
     This is called when levelobjects can be used.
      */
    public void initLevelBlock(Level level, int x, int y) {
        this.level = level;
        if(levelObject != null)
            levelObject.dispose();
        levelObject = new LevelObject(level, x, y, Entities.mcminos_default_front.getzIndex(), LevelObject.Types.McMinos);
        startBlock = levelObject.getLevelBlock();
        levelObject.setGfx(Entities.mcminos_default_front);
        initDestination();
    }

    public void increaseScore(int increment) {
        int old = score/5000;
        score += increment;
        if(score/5000 > old) { // just passed 5000
           increaseLives();
        }
    }

    public void gfxNormal() {
        mover.setGfx(Entities.mcminos_default_front, Entities.mcminos_default_up,
                Entities.mcminos_default_right, Entities.mcminos_default_down, Entities.mcminos_default_left);
    }

    public void gfxPowered() {
        mover.setGfx(Entities.mcminos_powered_front, Entities.mcminos_powered_up,
                Entities.mcminos_powered_right, Entities.mcminos_powered_down, Entities.mcminos_powered_left);
    }

    public void gfxPoisoned() {
        mover.setGfx(Entities.mcminos_poisoned_front);
    }

    public void gfxDrunk() {
        mover.setGfx(Entities.mcminos_drunk_front, Entities.mcminos_drunk_up,
                Entities.mcminos_drunk_right, Entities.mcminos_drunk_down, Entities.mcminos_drunk_left);
    }

    public LevelObject getLevelObject() {
        return levelObject;
    }

    public void updateDurations() {
        if (powerDuration > 1) {
            powerDuration--;
        } else {
            if (powerDuration == 1) { // power just ran out
                powerDuration = 0;
                setPowerPillValues(1, 1, 0); // back to normal, TODO: check, if this has to be adapted to level specifics
                gfxSelect();
            }
        }
        if(umbrellaDuration > 0) {
            umbrellaDuration --;
        } // no else necessary as umbrellapower is checked when necessary
        if(poisonDuration > 0) {
            poisonDuration --;
            if(poisonDuration == 0) {
                kill("skullkill", Entities.mcminos_dying);
            }
        }
        if(drunkLevel > 0) {
            drunkLevel -= 1;
            if(drunkLevel == 0) {
                gfxSelect();
            }
        }
    }

    /**
     *  consume powerpill
     */
    void setPowerPillValues(int mcmNewFactor, int gosNewFactor, int duration)
    {
        mover.setSpeedFactor(mcmNewFactor);
        game.getGhosts().setSpeedFactor(gosNewFactor);
        if(duration > 0) // something was actually consumed
        {
            powerDuration += duration << game.timeResolutionExponent;
            audio.soundPlay("power");
            gfxPowered(); // turn mcminos into nice graphics
            increaseScore(10);
        }
    }

    public void move() { // just escalate
        mover.move();
    }

    public boolean hasKey() {
        return keys > 0;
    }

    public void decreaseKeys() {
        if(keys > 0)
            keys --;
    }

    public void increaseKeys() {
        keys ++;
    }

    public boolean hasChocolate() {
        return chocolates > 0;
    }

    public void decreaseChocolates() {
        if(chocolates > 0)
            chocolates --;
    }

    public void increaseChocolates() {
        chocolates ++;
    }

    public boolean hasUmbrella() {
        return umbrellas > 0;
    }

    public void decreaseUmbrellas() {
        if(umbrellas > 0)
            umbrellas --;
    }

    public void increaseUmbrellas() {
        umbrellas ++;
    }

    public boolean hasBomb() {
        return bombs > 0;
    }

    public void decreaseBombs() {
        if(bombs > 0)
            bombs --;
    }

    public void increaseBombs() {
        bombs ++;
    }

    public boolean hasDynamite() {
        return dynamites > 0;
    }

    public void decreaseDynamites() {
        if(dynamites > 0)
            dynamites --;
    }

    public void increaseDynamites() {
        dynamites ++;
    }

    public boolean hasLandmine() {
        return landmines > 0;
    }

    public void decreaseLandmines() {
        if(landmines > 0)
            landmines --;
    }

    public void increaseLandmines() {
        landmines ++;
    }

    public LevelBlock getLevelBlock() {
        return levelObject.getLevelBlock();
    }

    public LevelBlock getFromLevelBlock() {
        return mover.getLastBlock();
    }

    public void setLevelBlock(LevelBlock levelBlock) {
        levelObject.setLevelBlock(levelBlock);
    }

    public void setXY(int x, int y) {
        levelObject.setXY(x, y);
    }

    public int getVX() {
        return levelObject.getVX();
    }

    public int getVY() {
        return levelObject.getVY();
    }

    public boolean isPowered() {
        return powerDuration > 0;
    }

    public boolean fullOnBlock() {
        return levelObject.fullOnBlock();
    }

    public boolean umbrellaActive() {
        return umbrellaDuration > 0;
    }

    public void increaseLives() {
        lives ++;
        audio.soundPlay("life");
    }

    public void consumeUmbrella() {
        decreaseUmbrellas();
        umbrellaDuration += 10 << Game.timeResolutionExponent;
    }

    public int getUmbrellas() {
        return umbrellas;
    }

    public int getChocolates() {
        return chocolates;
    }

    public int getBombs() {
        return bombs;
    }

    public int getDynamites() {
        return dynamites;
    }

    public int getKeys() {
        return keys;
    }

    public int getLandmines() {
        return landmines;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public int getPowerDuration() {
        return powerDuration;
    }

    public int getUmbrellaDuration() {
        return umbrellaDuration;
    }

    public void setMover(McMinosMover mover) {
        this.mover = mover;
        levelObject.setMover(mover);
    }

    public void teleportToBlock( LevelBlock block ) {
        unsetDestination();
        //setLevelBlock( block );
        levelObject.moveTo(block.getX() << PlayWindow.virtualBlockResolutionExponent,
                block.getY() << PlayWindow.virtualBlockResolutionExponent, block);
        mover.setLevelBlock(block);
    }

    /**
     * remove all association to this and the corresponding level-object
     */
    public void dispose() {
        levelObject.dispose();
        // rest should be handled by gc
    }

    public void decreaseLives() {
        lives --;
    }

    public void kill(String sound, Graphics gfx) {
        // don't multikill
        if( ! isKilled() ) {
            audio.soundPlay(sound);
            game.disableMovement();
            game.stopAllMovers();
            stop();
            killed = true;
            // show kill-animation
            mover.setGfx(null); // hide
            final LevelObject animation = new LevelObject(getLevelBlock(), gfx, LevelObject.Types.Unspecified);
            animation.setXY(getVX(), getVY());
            animation.animationStartNow(game);

            // schedule level-end and grave-stone setting after animation
            game.schedule(new FrameTimer.Task(animation) {
                @Override
                public void run() {
                    new LevelObject(animation.getLevelBlock(), Entities.walls_gravestone, LevelObject.Types.Unspecified);
                    animation.dispose();
                    decreaseLives();
                    if(getLives() > 0) {
                        level.killRestart();
                        killed = false;
                        resume();
                        // will be enabled at beginning of game: game.enableMovement();
                    } else {
                        level.finish();
                    }
                }
            }, gfx.getAnimationFramesLength());
        }
    }

    public void fall() {
        // don't multifall
        Graphics gfx = Entities.mcminos_frightened;
        if (!isFalling()) {
            audio.soundPlay("falling");
            //game.stopAllMovers();
            stop();
            falling = true;
            // show fall-animation
            mover.setGfx(null); // hide
            final LevelObject animation = new LevelObject(getLevelBlock(), gfx, LevelObject.Types.Unspecified);
            animation.animationStartNow(game);

            // schedule level-end and grave-stone setting after animation
            game.schedule(new FrameTimer.Task(animation) {
                @Override
                public void run() {
                    animation.dispose();
                    //decreaseLives();
                    teleportToBlock(startBlock);
                    falling = false;
                    gfxSelect();
                    resume();
                }
            }, gfx.getAnimationFramesLength());
        }
    }

    public void poison() {
        if (poisonDuration == 0) { // not already poisoned
            poisonDuration = 10 << Game.timeResolutionExponent;
            audio.soundPlay("poison");
            stop();
            gfxPoisoned();
        }
    }

    public void makeDrunk() {
        drunkLevel += 16 << Game.timeResolutionExponent;
        audio.soundPlay("ethanole");
        gfxDrunk();
    }


    private void gfxSelect() {
        if(poisonDuration > 0) {
            gfxPoisoned();
        }
        else if(drunkLevel > 0) {
            gfxDrunk();
        }
        else if(powerDuration > 0) gfxPowered();
        else gfxNormal();
    }

    /**
     * Stop all movement
     */
    private void stop() {
        mover.stop();

        // disable destination selection
        unsetDestination();
        destinationEnabled = false;
    }

    private void resume() {
        mover.resume();
        if( mover.getKeyDirections() == 0) destinationEnabled = true;
    }

    public boolean isKilled() {
        return killed;
    }

    public void win() {
        if( ! isWinning() ) {
            audio.soundPlay("applaus");
            game.disableMovement();
            game.stopAllMovers();
            stop();

            winning = true;
            // show kill-animation
            mover.setGfx(null); // hide
            // TODO: winning animation
            Graphics gfx = Entities.mcminos_cheering;
            final LevelObject animation = new LevelObject(getLevelBlock(), gfx, LevelObject.Types.Unspecified);
            animation.animationStartNow(game);

            // schedule level-end and grave-stone setting after animation
            game.schedule(new FrameTimer.Task(animation) {
                @Override
                public void run() {
                    animation.dispose();
                    level.finish();
                }
            }, gfx.getAnimationFramesLength());
        }
    }

    public boolean isWinning() {
        return winning;
    }

    public void setDestination( PlayWindow playwindow, int x, int y) {
        if(destinationEnabled) {
            if(isMirrored()) { // if this goes out of range it's corrected in moveto
                x = getVX() - (x - getVX());
                y = getVY() - (y - getVY());
            } // TODO: think if this shoudl be better handled in chooseDirection
            destination.setGfx(Entities.destination);
            LevelBlock lb = level.getLevelBlockFromVPixelRounded(x, y);
            destination.moveTo(lb.getX() << PlayWindow.virtualBlockResolutionExponent, lb.getY() << PlayWindow.virtualBlockResolutionExponent, lb);
            destinationSet = true;
        }
    }

    public boolean isDestinationSet() {
        return destinationSet;
    }

    public LevelObject getDestination() {
        return destination;
    }

    public void hideDestination() {
        destination.setGfx(null);
    }

    public void unsetDestination() {
        hideDestination();
        destinationSet = false; // TODO: or does it need to be still set?
    }

    /**
     * create graphical object for destination
     */
    public void initDestination() {
        destination = new LevelObject(level,getLevelBlock().getX(),getLevelBlock().getY(),
                Entities.destination.getzIndex(), LevelObject.Types.Unspecified);
        // playwindow.resize();
    }

    public boolean isFalling() {
        return falling;
    }

    public int getPoisonDuration() {
        return poisonDuration;
    }

    public boolean hasMedicine() {
        return medicines > 0;
    }

    public void increaseMedicines() {
        medicines ++;
    }

    public void decreaseMedicines() {
        if(medicines > 0)
            medicines --;
    }


    public int getMedicines() {
        return medicines;
    }

    public void consumeMedicine() {
        decreaseMedicines();
        audio.soundPlay("antidot");
        poisonDuration = 0;
        drunkLevel = 0;
        resume();
        gfxSelect();
    }

    public void clearInventory() {
        // Take away all things apart lives and score
        umbrellas = 0;
        chocolates =0;
        keys = 0;
        medicines = 0;
        bombs = 0;
        dynamites = 0;
        landmines = 0;
    }

    public void reset() {
        //TODO: check to eventually re-create start values
        mirrored = false;
        poisonDuration = 0;
        drunkLevel = 0;
        powerDuration = 0;
        umbrellaDuration = 0;
        setPowerPillValues(1,1,0);
        initLevelBlock(level,startBlock.getX(),startBlock.getY());
        gfxSelect();
    }

    public boolean updateKeyDirections() {
        return mover.updateKeyDirections();
    }

    public int getKeyDirections() {
        return mover.getKeyDirections();
    }

    public int updateTouchpadDirections(float knobPercentX, float knobPercentY) {
        return mover.updateTouchpadDirections( knobPercentX, knobPercentY );
    }

    public int getDrunkLevel() {
        return drunkLevel;
    }

    public void setSpeedAccelerated(boolean b) {
        mover.setSpeedAccelerated(b);
    }

    public boolean isMirrored() {
        return mirrored;
    }

    public void toggleMirrored() {
        mirrored = ! mirrored;
    }

}
