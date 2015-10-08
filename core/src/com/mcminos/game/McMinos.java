package com.mcminos.game;

/**
 * Created by ulno on 05.10.15.
 */
public class McMinos {
    private final Game game;
    private Level level;
    private McMinosMover mover;
    private int powerDuration = 0;
    private int umbrellaDuration = 0;
    private int umbrellas=0; // number of umbrellas carried by mcminos
    private int chocolates; // number of chocolates carried by mcminos
    private int bombs=0; // number of bombs carried by mcminos
    private int dynamites=0; // number of dynamites carried by mcminos
    private int keys=0; // number of keys carried by mcminos
    private int landmines=0; // number of umbrellas carried by mcminos
    private int lives=3; // number of lives left
    private int score=0; // current score
    private final Audio audio;
    private LevelObject levelObject;
    private boolean killed = false;
    private boolean winning = false;
    private LevelObject destination;
    private boolean destinationSet; // was a destination set (and needs to be shown)
    private boolean falling;
    private LevelBlock startBlock = null;
    private boolean destinationEnabled = true;


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
        mover.setGfx(Entities.mcminos_doped_front, Entities.mcminos_doped_up,
                Entities.mcminos_doped_right, Entities.mcminos_doped_down, Entities.mcminos_doped_left);
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
                gfxNormal();
            }
        }
        if(umbrellaDuration > 0) {
            umbrellaDuration --;
        } // no else necessary as umbrellapower is checked when necessary
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
            game.stopAllMovers();
            stop();
            killed = true;
            // show kill-animation
            mover.setGfx(null); // hide
            final LevelObject animation = new LevelObject(getLevelBlock(), gfx, LevelObject.Types.Unspecified);
            animation.animationStartNow();

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
                    } else {
                        game.getPlayScreen().backToMenu();
                    }
                }
            }, gfx.getAnimationFramesLength());
        }
    }

    public void fall() {
        // don't multifall
        Graphics gfx = Entities.mcminos_dying; // TODO: replace with falling
        if (!isFalling()) {
            audio.soundPlay("falling");
            //game.stopAllMovers();
            stop();
            falling = true;
            // show fall-animation
            mover.setGfx(null); // hide
            final LevelObject animation = new LevelObject(getLevelBlock(), gfx, LevelObject.Types.Unspecified);
            animation.animationStartNow();

            // schedule level-end and grave-stone setting after animation
            game.schedule(new FrameTimer.Task(animation) {
                @Override
                public void run() {
                    animation.dispose();
                    //decreaseLives();
                    teleportToBlock(startBlock);
                    falling=false;
                    gfxSelect();
                    resume();
                }
            }, gfx.getAnimationFramesLength());
        }
    }

    private void gfxSelect() {
        if(powerDuration > 0) gfxPowered();
        else gfxNormal();
    }

    /**
     * Stop all movement
     */
    private void stop() {
        mover.setSpeed(0);
        // disable destination selection
        unsetDestination();
        destinationEnabled = false;
    }

    private void resume() {
        mover.setSpeed(Game.baseSpeed);
        destinationEnabled = true;
    }

    public boolean isKilled() {
        return killed;
    }

    public void win() {
        if( ! isWinning() ) {
            audio.soundPlay("applaus");
            game.stopAllMovers();
            stop();
            winning = true;
            // show kill-animation
            mover.setGfx(null); // hide
            // TODO: winning animation
            Graphics gfx = Entities.mcminos_doped_front;
            final LevelObject animation = new LevelObject(getLevelBlock(), gfx, LevelObject.Types.Unspecified);
            animation.animationStartNow();

            // schedule level-end and grave-stone setting after animation
            game.schedule(new FrameTimer.Task(animation) {
                @Override
                public void run() {
                    animation.dispose();
                    game.getPlayScreen().backToMenu();
                }
            }, gfx.getAnimationFramesLength());
        }
    }

    public boolean isWinning() {
        return winning;
    }

    public void setDestination(int x, int y) {
        if(destinationEnabled) {
            destination.setGfx(Entities.destination);
            destination.moveTo(x, y, game.getLevelBlockFromVPixelRounded(x, y));
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
    private void initDestination() {
        destination = new LevelObject(level,getLevelBlock().getX(),getLevelBlock().getY(),
                Entities.destination.getzIndex(), LevelObject.Types.Unspecified);
        // playwindow.resize();
    }

    public boolean isFalling() {
        return falling;
    }
}
