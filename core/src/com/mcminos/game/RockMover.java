package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by ulno on 01.10.15.
 */
public class RockMover extends Mover {

    private Audio audio;
//    private LevelBlock headingTo;
    private LevelBlock lastBlockChecked = null;
    private Game game;


    public RockMover(Game game, LevelObject rock, int speed, boolean accelerated, int currentDirection, LevelBlock headingToNew) {
        super(rock, speed, false, 0, Entities.extras_rock);
        setSpeedAccelerated(accelerated);
        this.currentDirection = currentDirection;
        headingTo = headingToNew;
        this.game = game;
        audio = game.getAudio();
    }

    /**
     * for json-read
     */
    public RockMover() {
        super();
    }

    @Override
    public void write(Json json) {
        super.write(json);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }

    @Override
    public void initAfterJsonLoad(Game game, LevelObject lo) {
        super.initAfterJsonLoad(game, lo);
        audio = game.getAudio();
        this.game = game;
        // TODO: add lastBlockChecked
    }

    @Override
    protected boolean checkCollisions() {
        if (levelObject.fullOnBlock() && lastBlockChecked != currentLevelBlock) {
            lastBlockChecked = currentLevelBlock;
            /* ishandled automatically in moving and levelblock association
            if(currentLevelBlock.isRockme()) {
                currentLevelBlock.getLevel().decreaseRockmes();
            } */
            // check if on hole -> break hole and remove rock
            if(currentLevelBlock.hasHole()) {
                currentLevelBlock.getHole().setHoleLevel(LevelObject.maxHoleLevel);
                currentLevelBlock.remove(levelObject);
                //handled in remove currentLevelBlock.setRock(null); // remove rock
                levelObject.dispose();
                audio.soundPlay("rumble");
                return true;
            } else {
                for (LevelObject lo : currentLevelBlock.getCollectibles()) {
                    if (lo.getType() == LevelObject.Types.LandMineActive) { // rock triggers active landmine
                        lo.dispose();
                        game.schedule(EventManager.Types.ExplosionLight, currentLevelBlock);
                        break;
                    }
                }
            }
            // mover is done here
            currentDirection = STOP;
        }
        return false;
    }

    @Override
    protected LevelBlock chooseDirection() {
        // direction is already set in constructor
        return headingTo;
    }

    public boolean isMoving() {
        return currentDirection != STOP;
    }


    public void triggerMove(int dir, int speed, boolean accelerated, LevelBlock headingTo) {
        currentDirection = dir;
        this.setSpeedFactor(speed);
        this.setSpeedAccelerated(accelerated);
        this.headingTo = headingTo;
    }

}
