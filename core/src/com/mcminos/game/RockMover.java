package com.mcminos.game;

/**
 * Created by ulno on 01.10.15.
 */
public class RockMover extends Mover {

   /* public RockMover(LevelObject rock, int speed) {
        super(rock, speed, false, Entities.extras_rock);
    }
*/
    public RockMover(LevelObject rock, int speed, int currentDirection) {
        super(rock, speed, false, Entities.extras_rock);
        this.currentDirection = currentDirection;
    }

    @Override
    protected boolean checkCollisions() {
        boolean isOnField = levelObject.fullOnBlock();

        // check if on hole -> break hole and remove rock
        if (isOnField) {
            if (currentLevelBlock.hasHole()) {
                currentLevelBlock.getHole().setHoleLevel(LevelObject.maxHoleLevel);
                currentLevelBlock.removeMovable(levelObject);
                currentLevelBlock.setRock(null); // remove rock
                levelObject.dispose();
                Root.soundPlay("rumble");
                return true;
            } else {
                for( LevelObject lo: currentLevelBlock.getCollectibles() ) {
                    if( lo.getType() == LevelObject.Types.LandMineActive ) { // rock triggers active landmine
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
    protected void chooseDirection() {
        // direction is already set in constructor
    }

    public boolean isMoving() {
        return currentDirection != STOP;
    }


    public void triggerMove(int dir, int speed) {
        currentDirection = dir;
        this.speed = speed;
    }
}
