package com.mcminos.game;

/**
 * Created by ulno on 01.10.15.
 */
public class McminosMover extends Mover {

    private int mcminosSpeedFactor = 1;

    public McminosMover( LevelObject mcminos ) {
        super(mcminos, Root.baseSpeed, true);
    }


    @Override
    public void chooseDirection() {
        // allow direction change when on block-boundaries
        if (levelObject.fullOnBlock() && Root.isDestinationSet()) {
            int directions = 0; // direction bit field
            LevelObject destination = Root.getDestination();
            // check screen distance
            int x = levelObject.getVX();
            int xdelta = x - destination.getVX(); // delta to center of destination (two centers substract)
            int xdiff = Math.abs(xdelta);
            if (xdiff <= Root.virtualBlockResolution >> 1 || xdiff >= Root.getVPixelsLevelWidth() - (Root.virtualBlockResolution >> 1))
                xdelta = 0;
            else {
                //also allow this in non-scrolled levels
                //if (getScrollX() && xdiff >= getVPixelsLevelWidth() >> 1)
                if (xdiff >= Root.getVPixelsLevelWidth() >> 1)
                    xdelta = (int) Math.signum(xdelta);
                else
                    xdelta = -(int) Math.signum(xdelta);
            }
            int y = levelObject.getVY();
            int ydelta = y - destination.getVY(); // delta to center of destination (two centers substract)
            int ydiff = Math.abs(ydelta);
            if (ydiff <= Root.virtualBlockResolution >> 1 || ydiff >= Root.getVPixelsLevelHeight() - (Root.virtualBlockResolution >> 1))
                ydelta = 0;
            else {
                // also in non-scroll levels
                //if( getScrollY() && ydiff >= getVPixelsLevelHeight() >> 1 )
                if (ydiff >= Root.getVPixelsLevelHeight() >> 1)
                    ydelta = (int) Math.signum(ydelta);
                else
                    ydelta = -(int) Math.signum(ydelta);
            }

            if (ydelta > 0) directions += UP;
            if (ydelta < 0) directions += DOWN;
            if (xdelta > 0) directions += RIGHT;
            if (xdelta < 0) directions += LEFT;
            if (directions == 0) {
                Root.unsetDestination();
            }

            // refine with possible directions
            directions = getUnblockedDirs(directions,true);

            // prefer longer distance (narrow down to one choice)
            if( (directions & (UP+DOWN)) > 0 && (directions & (LEFT+RIGHT)) > 0 ) {
                if (xdiff > ydiff) {
                    directions &= LEFT + RIGHT;
                } else {
                    directions &= UP + DOWN;
                }
            }

            LevelBlock nextBlock=null;

            // only one direction should be left now.
            currentDirection = directions; // start moving there
            switch (currentDirection) {
                case STOP:
                    //nextBlock = currentLevelBlock;
                    // actually done here
                    return;
                //break;
                case UP:
                    nextBlock = currentLevelBlock.up();
                    break;
                case RIGHT:
                    nextBlock = currentLevelBlock.right();
                    break;
                case DOWN:
                    nextBlock = currentLevelBlock.down();
                    break;
                case LEFT:
                    nextBlock = currentLevelBlock.left();
                    break;
            }
            if (nextBlock.hasRock()) {
                LevelBlock nextBlock2=null;
                switch (currentDirection) {
                    case UP:
                        nextBlock2 = nextBlock.up();
                        break;
                    case RIGHT:
                        nextBlock2 = nextBlock.right();
                        break;
                    case DOWN:
                        nextBlock2 = nextBlock.down();
                        break;
                    case LEFT:
                        nextBlock2 = nextBlock.left();
                        break;
                }
                LevelObject rock = nextBlock.getRock();
                RockMover m = (RockMover) rock.getMover();
                if( m == null ) {
                    // also make rock in the speed we push it
                    RockMover mover = new RockMover(rock, speed, currentDirection);
                    rock.setMover(mover);
                    Root.movables.add(mover);
                    //mover.move(); //small headstart to arrive early enough - not necessary
                    nextBlock.setRock(null);
                    nextBlock2.setRock(rock);
                    Root.soundPlay("moverock");
                } else if(  ! m.isMoving() ) {
                    // let it move again
                    m.triggerMove(currentDirection, speed);
                    nextBlock.setRock(null);
                    nextBlock2.setRock(rock);
                    Root.soundPlay("moverock");
                }
            }
        }
    }

    /**
     * Check Mcminos'  collisions (mainly if mcminos found something and can collect it)
     * @return
     */
    @Override
    protected boolean checkCollisions() {
        // check if something can be collected (only when full on field)
        if(Root.mcminos.fullOnBlock()) {
            LevelBlock currentBlock = Root.getLevelBlockFromVPixel(Root.mcminos.getVX(), Root.mcminos.getVY());
            if( currentBlock.hasPill() )
            {
                Root.soundPlay("knurps");
                currentBlock.removePill();
                Root.increaseScore(1);
            }
            // check, if mcminos actually moved or if it's the same field as last time
            if(currentBlock != lastBlock) {
                if(Root.umbrellaDuration == 0) { // no umbrellapower currently
                    // check if last block had a hole -> make it bigger
                    if (lastBlock.hasHole()) {
                        // TODO check umbrella
                        // try to increase
                        lastBlock.getHole().increaseHole();
                    }
                    if (lastBlock.hasOneWay()) {
                        lastBlock.turnOneWay();
                    }
                    // check if here is max hole
                    if (currentBlock.hasHole() && currentBlock.getHole().holeIsMax()) {
                        // fall in
                        // TODO: intiate kill sequence
                        Root.soundPlay("falling");
                    }
                }
                // check the things lying here
                for( LevelObject b:currentBlock.getCollectibles()) {
                    switch( b.getType() ) {
                        case Chocolate:
                            Root.soundPlay("tools");
                            Root.chocolates ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.increaseScore(10);
                            break;
                        case Bomb:
                            Root.soundPlay("tools");
                            Root.bombs ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case Dynamite:
                            Root.soundPlay("tools");
                            Root.dynamites ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMine:
                            Root.soundPlay("tools");
                            Root.landmines ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMineActive:
                            currentBlock.removeItem(b);
                            b.dispose();
                            new Explosion(currentBlock, LevelObject.Types.LandMine);
                            break;
                        case Key:
                            Root.soundPlay("tools");
                            Root.keys ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.increaseScore(10);
                            break;
                        case Umbrella:
                            Root.soundPlay("tools");
                            Root.umbrellas ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.increaseScore(10);
                            break;
                        case Live:
                            Root.soundPlay("life");
                            Root.lives ++;
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.increaseScore(10);
                            break;
                        case Power1:
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.setPowerPillValues(2, 1, 10);
                            // sound played in ppill method
                            Root.mcminosGfxPowered(); // turn mcminos into nice graphics
                            break;
                        case Power2:
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.setPowerPillValues(1, 2, 10);
                            Root.mcminosGfxPowered(); // turn mcminos into nice graphics
                            break;
                        case Power3:
                            currentBlock.removeItem(b);
                            b.dispose();
                            Root.setPowerPillValues(1, 1, 10);
                            Root.mcminosGfxPowered(); // turn mcminos into nice graphics
                            break;
                        case SpeedUpField:
                            setSpeedFactor(2);
                            Root.soundPlay("speedup");
                            break;
                        case SpeedDownField:
                            setSpeedFactor(1);
                            Root.soundPlay("slowdown");
                            break;
                    }
                }
            }
            lastBlock = currentBlock;
        }
        return false; // don't remove mcminos
    }

    public void setSpeedFactor(int mcmNewFactor) {
        speed /= mcminosSpeedFactor;
        speed *= mcmNewFactor;
        mcminosSpeedFactor = mcmNewFactor;
        setSpeed(speed);
    }

}
