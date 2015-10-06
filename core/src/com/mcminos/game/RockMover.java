package com.mcminos.game;

/**
 * Created by ulno on 01.10.15.
 */
public class RockMover extends Mover {

    private final Audio audio;
    private LevelBlock headingTo;

    /* public RockMover(LevelObject rock, int speed) {
            super(rock, speed, false, Entities.extras_rock);
        }
    */
    public RockMover(LevelObject rock, int speed, int currentDirection, LevelBlock headingTo) {
        super(rock, speed, false, Entities.extras_rock);
        this.currentDirection = currentDirection;
        this.headingTo = headingTo;
        audio = rock.getLevelBlock().getLevel().getGame().getAudio();
    }

    @Override
    protected boolean checkCollisions() {
        // check if on hole -> break hole and remove rock
        if (levelObject.fullOnBlock()) {
            if (currentLevelBlock.hasHole()) {
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


    public void triggerMove(int dir, int speed, LevelBlock headingTo) {
        // TODO: check speed is applied correctly
        currentDirection = dir;
        this.setSpeed( speed );
        this.headingTo = headingTo;
    }

}
