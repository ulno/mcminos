package com.mcminos.game;

/**
 * Created by ulno on 01.10.15.
 */
public class RockMover extends Mover {

    private final Audio audio;
    private LevelBlock headingTo;
    private LevelBlock lastBlockChecked = null;

    /* public RockMover(LevelObject rock, int speed) {
            super(rock, speed, false, Entities.extras_rock);
        }
    */
    public RockMover(LevelObject rock, int speed, boolean accelerated, int currentDirection, LevelBlock headingTo) {
        super(rock, speed, false, 0, Entities.extras_rock);
        setSpeedAccelerated(accelerated);
        this.currentDirection = currentDirection;
        this.headingTo = headingTo;
        audio = rock.getLevelBlock().getLevel().getGame().getAudio();
    }

    @Override
    protected boolean checkCollisions() {
        if (levelObject.fullOnBlock() && lastBlockChecked != currentLevelBlock) {
            lastBlockChecked = currentLevelBlock;
            // check if on hole -> break hole and remove rock
            if(currentLevelBlock.isRockme()) {
                currentLevelBlock.getLevel().decreaseRockmes();
            }
            if(currentLevelBlock.hasHole()) {
                currentLevelBlock.getHole().setHoleLevel(LevelObject.maxHoleLevel);
                currentLevelBlock.removeMovable(levelObject);
                currentLevelBlock.setRock(null); // remove rock
                levelObject.dispose();
                audio.soundPlay("rumble");
                return true;
            } else {
                for (LevelObject lo : currentLevelBlock.getCollectibles()) {
                    if (lo.getType() == LevelObject.Types.LandMineActive) { // rock triggers active landmine
                        currentLevelBlock.removeItem(lo);
                        lo.dispose();
                        new Explosion(currentLevelBlock, LevelObject.Types.LandMine);
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
        // TODO: check speed is applied correctly
        currentDirection = dir;
        this.setSpeedFactor(speed);
        this.setSpeedAccelerated(accelerated);
        this.headingTo = headingTo;
    }

}
