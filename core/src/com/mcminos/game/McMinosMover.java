package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by ulno on 01.10.15.
 */
public class McMinosMover extends Mover {

    private Game game;
    private McMinos mcminos;
    private Audio audio;
    private Level level;
    private Ghosts ghosts;
    private int keyDirections = 0;
    private int touchpadDirections = 0;
    private ArrayList<LevelObject> currentItemlist;

    public McMinosMover() {
        super();
    }

    public McMinosMover(Game game) {
        super(game.getMcMinos().getLevelObject(), game.getLevel().getLevelConfig().getMcMinosSpeed(),1, true, 0);
        this.game = game;
        initFromGame();
    }

    /**
     * Game needs to be set before calling this
     */
    private void initFromGame() {
        audio = game.getAudio();
        mcminos = game.getMcMinos();
        levelObject = mcminos.getLevelObject();
        this.level = game.getLevel();
        ghosts = game.getGhosts();
        mcminos.setMover(this);
        mcminos.gfxSelect();
        for (int y = 0; y < mazeSize; y++) {
            for (int x = 0; x < mazeSize; x++) {
                mazeBlocks[y][x] = new MazeBlock();
            }
        }

    }

    @Override
    public LevelBlock chooseDirection() {
        if(mcminos.getPoisonDuration() == 0) { // there can only be a direction if not poisoned
            // this is only called, when on block boundaries
            int directions = getKeyDirections(); // direction bit field
            int drunkLevel = mcminos.getDrunkLevel();
            if (drunkLevel > 0) /* if drunk, 3 bottles -> 30 seconds mean no control */
                if (level.random(Math.max(1, 30 - (drunkLevel >> Game.timeResolutionExponent))) == 0)
                    directions = level.random(15) + 1;

            if (directions == 0) { // if no key, then try to get from destination
                // the following includes the call to unblocked dirs already
                directions = getDirectionsFromDestination();
            } else { // got keyboard directions
                // refine with possible directions
                directions = getUnblockedDirs(directions, true, false);
            }
            if (directions > 0) { // got something in directions

                LevelBlock nextBlock = null;

                switch (directions) {
                    // do one direction fast
                    case STOP:
                        //nextBlock = currentLevelBlock;
                        // actually done here
                        currentDirection = STOP;
                        return currentLevelBlock;
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
                    default: // more than one given, select first possible
                        if ((directions & UP) > 0) {
                            nextBlock = currentLevelBlock.up();
                            directions = UP;
                        } else if ((directions & RIGHT) > 0) {
                            nextBlock = currentLevelBlock.right();
                            directions = RIGHT;
                        } else if ((directions & DOWN) > 0) {
                            nextBlock = currentLevelBlock.down();
                            directions = DOWN;
                        } else /* LEFT */ {
                            nextBlock = currentLevelBlock.left();
                            directions = LEFT;
                        }
                        break;
                }
                currentDirection = directions; // start moving there
                if (nextBlock.hasRock()) {
                    LevelBlock nextBlock2 = null;
                    switch (currentDirection) {
                        case UP:
                            nextBlock2 = currentLevelBlock.up2();
                            break;
                        case RIGHT:
                            nextBlock2 = currentLevelBlock.right2();
                            break;
                        case DOWN:
                            nextBlock2 = currentLevelBlock.down2();
                            break;
                        case LEFT:
                            nextBlock2 = currentLevelBlock.left2();
                            break;
                    }
                    LevelObject rock = nextBlock.getRock();
                    RockMover mover = (RockMover) rock.getMover();
                    if (mover == null) {
                        // also make rock in the speed we push it
                        mover = new RockMover(game, rock, getSpeedFactor(), isAccelerated(), currentDirection, nextBlock2);
                        rock.setMover(mover);
                        game.addMover(mover);
                    }
                    mover.triggerMove(currentDirection, getSpeedFactor(), isAccelerated(), nextBlock2);
                    //mover.move(); // small headstart to arrive early enough - necessary to prevent ghosts frolevel.increasePills();m running to destination - does not work, needs to jump ahead (see below)
                    // this would actually allow another ghost from the side to run in
                    rock.setLevelBlock(nextBlock2); // this is important to prevent monsters from running here
                    audio.soundPlay("moverock");
                }
                return nextBlock;
            }
        }
        // nothing found or no direction set so return currentblock and no direction possibility
        currentDirection = STOP;
        return currentLevelBlock;
    }

    private class MazeBlock {
        public int directions;
        public int distance;
        public int mhDistanceToDestination;
        // from where was this field reached
        public int fromX = -1;
        public int fromY = -1;
        public int fromDir = 0;
    }

    private final int mazeSize = 11;
    private final int mazeCenter = mazeSize / 2;
    private final int mazeMaxDist = mazeSize * mazeSize;
    private MazeBlock mazeBlocks[][] = new MazeBlock[mazeSize][mazeSize];

    private int getDirectionsFromDestination() {
        int directions = STOP;

        if (mcminos.isDestinationSet()) {
            /* Try to find unobstructed path to destination */
            LevelObject destination = mcminos.getDestination();
            LevelBlock destBlock = destination.getLevelBlock();
            int destX = destBlock.getX();
            int destY = destBlock.getY();
            int mx = currentLevelBlock.getX();
            int my = currentLevelBlock.getY();
            int lw = level.getWidth();
            int lh = level.getHeight();
            if (destBlock.hasRock() && // special treatment for rocks as pathfinding is not as good then
                    (distanceWithScroll(level.getScrollX(), mx, destX, lw)
                            + distanceWithScroll(level.getScrollY(), my, destY, lh) == 1)) { // is on neighboring field
                // let's just try to go there
                if (my == destY) { // it's left or right
                    if ((mx + 1) % lw == destX) {
                        directions = RIGHT;
                    } else {
                        directions = LEFT;
                    }
                } else { // must be up or down
                    if ((my + 1) % lh == destY) {
                        directions = UP;
                    } else {
                        directions = DOWN;
                    }
                }
                directions = getUnblockedDirs( directions, true, false);
            } else { // ok, it's just normal pathfinding
                int shortestDestDistance = level.getHeight() * level.getHeight();
                int closestX = -1;
                int closestY = -1;
                int rockRadius = 1; // in which radius are you allowed to move rocks
                // select 11x11 field with mcminos being the center
                // fill the field with the unobstructed directions from each position and
                // distance travelled to 0
                for (int y = 0, ly = my - mazeCenter; y < mazeSize; y++, ly++) {
                    for (int x = 0, lx = mx - mazeCenter; x < mazeSize; x++, lx++) {
                        MazeBlock b = mazeBlocks[y][x];
                        LevelBlock lb = level.get(lx, ly);
                        if (lb == null) {
                            b.directions = 0;
                        } else {
                        /*
                            turned out to be bad, we need to do some extra if, we select a rock as destination
                        // check, if the dealing with rocks is good like this - seems ok

                        int xdist = distanceWithScroll(level.getScrollX(), lx, destX, lw);
                        int ydist = distanceWithScroll(level.getScrollY(), ly, destY, lh);
                        if( xdist + ydist <= rockRadius )
                            b.directions = lb.getUnblockedDirs(true,false);
                        else  */
                            b.directions = lb.getUnblockedDirs(false, false);
                        }
                        b.distance = mazeMaxDist;
                    }
                }
                // find shortest paths and compute reachability
                recurseMazeFromHere(mazeCenter, mazeCenter, -1, -1, 0, 0);

                // select closest reachable field to destination from search block
                int shortestDistance = mazeMaxDist;
                for (int y = 0, ly = my - mazeCenter; y < mazeSize; y++, ly++) {
                    for (int x = 0, lx = mx - mazeCenter; x < mazeSize; x++, lx++) {
                        MazeBlock b = mazeBlocks[y][x];
                        if (b.distance < mazeMaxDist) { // if reachable
                            int xdist = distanceWithScroll(level.getScrollX(), lx, destX, lw);
                            int ydist = distanceWithScroll(level.getScrollY(), ly, destY, lh);
                            b.mhDistanceToDestination = xdist + ydist;
                            if (b.mhDistanceToDestination <= shortestDestDistance) { // equal as there might be several destinations in small levels
                                if (b.mhDistanceToDestination < shortestDestDistance) { // really smaller
                                    shortestDistance = mazeMaxDist; // reset distance as we really want to go somewhere close
                                }
                                if (b.distance < shortestDistance) {
                                    shortestDestDistance = b.mhDistanceToDestination;
                                    shortestDistance = b.distance;
                                    closestX = x;
                                    closestY = y;
                                }
                            }
                        }
                    }
                }
                // trace way back to center and then select destination we were coming from
                int traceDir = 0;
                while (closestX != mazeCenter || closestY != mazeCenter) {
                    MazeBlock b = mazeBlocks[closestY][closestX];
                    closestX = b.fromX;
                    closestY = b.fromY;
                    traceDir = b.fromDir;
                }
                directions = traceDir;
                // consider ingnoring or giving panelty to rocks (or even doors?) in way calculation - solved with radius 1
            }
        }
        if (directions == STOP) {
            mcminos.hideDestination();
        }
        return directions;


//            // old direction selection code follows
//
//            // check screen distance
//            int x = animation.getVX();
//            int xdelta = x - destination.getVX(); // delta to center of destination (two centers substract)
//            int xdiff = Math.abs(xdelta);
//            if (xdiff <= PlayWindow.virtualBlockResolution >> 1 || xdiff >= playwindow.getVPixelsLevelWidth() - (PlayWindow.virtualBlockResolution >> 1))
//                xdelta = 0;
//            else {
//                if (level.getScrollX() && xdiff >= playwindow.getVPixelsLevelWidth() / 2)
//                    xdelta = (int) Math.signum(xdelta);
//                else
//                    xdelta = -(int) Math.signum(xdelta);
//            }
//            int y = animation.getVY();
//            int ydelta = y - destination.getVY(); // delta to center of destination (two centers substract)
//            int ydiff = Math.abs(ydelta);
//            if (ydiff <= PlayWindow.virtualBlockResolution >> 1 || ydiff >= playwindow.getVPixelsLevelHeight() - (PlayWindow.virtualBlockResolution >> 1))
//                ydelta = 0;
//            else {
//                if (level.getScrollY() && ydiff >= playwindow.getVPixelsLevelHeight() / 2)
//                    ydelta = (int) Math.signum(ydelta);
//                else
//                    ydelta = -(int) Math.signum(ydelta);
//            }
//
//            if (ydelta > 0) directions += UP;
//            if (ydelta < 0) directions += DOWN;
//            if (xdelta > 0) directions += RIGHT;
//            if (xdelta < 0) directions += LEFT;
//            if (directions == 0) {
//                mcminos.hideDestination();
//            }
//
//            // refine with possible directions
//            directions = getUnblockedDirs(directions,true);
//
//            // prefer longer distance (narrow down to one choice)
//            if ((directions & (UP + DOWN)) > 0 && (directions & (LEFT + RIGHT)) > 0) {
//                if (xdiff > ydiff) {
//                    directions &= LEFT + RIGHT;
//                } else {
//                    directions &= UP + DOWN;
//                }
//            }
//        }
//        return directions;
    }

    private int distanceWithScroll(boolean scroll, int p1, int p2, int size) {
        if (scroll) {
            p1 = (p1 + (size >> 2)) % size;
            p2 = (p2 + (size >> 2)) % size;
            int delta = Math.abs(p1 - p2);
            if (delta >= (size >> 1)) {
                delta = size - delta;
            }
            return delta;
        }
        return  Math.abs(p1-p2);
    }

    private void recurseMazeFromHere(int x, int y, int fromX, int fromY, int fromDir, int distanceTravelled) {
        MazeBlock mb = mazeBlocks[y][x];
        if( distanceTravelled >= mb.distance ) // no need to continue
            return;
        mb.distance = distanceTravelled; // ok, we made it faster
        mb.fromX = fromX;
        mb.fromY = fromY;
        mb.fromDir = fromDir;
        distanceTravelled += 1;
        if((mb.directions & UP) > 0) {
            if(y<mazeSize-1) recurseMazeFromHere(x,y+1,x,y,UP,distanceTravelled);
        }
        if((mb.directions & RIGHT) > 0) {
            if(x<mazeSize-1) recurseMazeFromHere(x+1,y,x,y,RIGHT,distanceTravelled);
        }
        if((mb.directions & DOWN) > 0) {
            if(y>0) recurseMazeFromHere(x,y-1,x,y,DOWN,distanceTravelled);
        }
        if((mb.directions & LEFT) > 0) {
            if(x>0) recurseMazeFromHere(x-1,y,x,y,LEFT,distanceTravelled);
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, com.esotericsoftware.kryo.io.Input input) {
        super.read(kryo, input);
    }

    @Override
    public void initAfterKryoLoad(Game game,LevelObject lo) {
        super.initAfterKryoLoad(game,lo);
        this.game = game;
        initFromGame();
    }

    /**
     * Check Mcminos'  collisions (mainly if mcminos found something and can collect it)
     * @return
     */
    @Override
    protected boolean checkCollisions() {
        // check if something can be collected (only when full on field)
        if(mcminos.fullOnBlock()) {
            LevelBlock currentBlock = level.getLevelBlockFromVPixel(mcminos.getVX(), mcminos.getVY());
            if( currentBlock.hasPill() )
            {
                audio.soundPlay("knurps");
                currentBlock.removePill();
                mcminos.increaseScore(1);
            }
            // check, if mcminos actually moved or if it's the same field as last time
            if(currentBlock != lastBlock) {
                if(! mcminos.umbrellaActive()) { // no umbrellapower currently
                    // check if last block had a hole -> make it bigger
                    if (lastBlock.hasHole()) {
                        // try to increase
                        lastBlock.getHole().increaseHole(audio);
                    }
                    if (lastBlock.hasOneWay()) {
                        lastBlock.turnOneWay(audio);
                    }
                    // check if here is max hole
                    if (currentBlock.hasHole() && currentBlock.getHole().holeIsMax()) {
                        audio.soundPlay("falling");
                        mcminos.fall();
                    }
                }
                // check the things lying here
                currentItemlist = currentBlock.getCollectibles();
                for( int i=currentItemlist.size()-1; i>=0; i--) {
                    LevelObject item = currentItemlist.get(i);
                    switch(item.getType()) {
                        case Chocolate:
                            audio.soundPlay("tools");
                            mcminos.increaseChocolates();
                            item.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Bomb:
                            audio.soundPlay("tools");
                            mcminos.increaseBombs();;
                            item.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case Dynamite:
                            audio.soundPlay("tools");
                            mcminos.increaseDynamites();;
                            item.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMine:
                            audio.soundPlay("tools");
                            mcminos.increaseLandmines();;
                            item.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMineActive:
                            item.dispose();
                            game.schedule(EventManager.Types.ExplosionLight, currentBlock);
                            break;
                        case Key:
                            audio.soundPlay("tools");
                            mcminos.increaseKeys();;
                            item.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Umbrella:
                            audio.soundPlay("tools");
                            mcminos.increaseUmbrellas();;
                            item.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Live:
                            mcminos.increaseLives();
                            item.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Power1:
                            item.dispose();
                            mcminos.setPowerPillValues(2, 1, 10);
                            // sound played in ppill method
                            break;
                        case Power2:
                            item.dispose();
                            mcminos.setPowerPillValues(1, 2, 10);
                            break;
                        case Power3:
                            item.dispose();
                            mcminos.setPowerPillValues(1, 1, 10);
                            break;
                        case SpeedUpField:
                            mcminos.setSpeedAccelerated(true);
                            audio.soundPlay("speedup");
                            break;
                        case SpeedDownField:
                            mcminos.setSpeedAccelerated(false);
                            audio.soundPlay("slowdown");
                            break;
                        case KillAllField:
                            ghosts.killall();
                            audio.soundPlay("killall");
                            break;
                        case KillAllPill:
                            item.dispose();
                            ghosts.killall();
                            audio.soundPlay("killall");
                            break;
                        case Bonus1:
                            item.dispose();
                            mcminos.increaseScore(100);
                            audio.soundPlay("treasure");
                            break;
                        case Bonus2:
                            item.dispose();
                            mcminos.increaseScore(200);
                            audio.soundPlay("treasure");
                            break;
                        case Bonus3:
                            item.dispose();
                            mcminos.increaseScore(300);
                            audio.soundPlay("treasure");
                            break;
                        case WarpHole:
                            if (currentDirection != STOP) { // only teleport when moving onto field
                                audio.soundPlay("blub");
                                mcminos.teleportToBlock(level.getFreeWarpHole(currentBlock));
                            }
                            break;
                        case Skull:
                            item.dispose();
                        case SkullField:
                            mcminos.kill("skullkill",Entities.mcminos_dying,false);
                            break;
                        case Poison:
                            item.dispose();
                            mcminos.poison();
                            break;
                        case Whisky:
                            mcminos.increaseScore(5);
                            item.dispose();
                            mcminos.makeDrunk();
                            break;
                        case Medicine:
                            audio.soundPlay("tools");
                            item.dispose();
                            mcminos.increaseMedicines();
                            break;
                        case Mirror:
                            audio.soundPlay("fade");
                            item.dispose();
                            mcminos.toggleMirrored();
                            break;
                        case Exit:
                            mcminos.win();
                            break;
                    }
                }
            }
            lastBlock = currentBlock;

            // check winning condition (only when full on block to allow rocks to slide to the end)
            if(level.getPillsNumber() == 0 && level.getRockmesNumber() == 0) {
                mcminos.win();
            }

        }
        checkGhosts(currentLevelBlock);
        checkGhosts(currentLevelBlock.up());
        checkGhosts(currentLevelBlock.right());
        checkGhosts(currentLevelBlock.down());
        checkGhosts(currentLevelBlock.left());

        return false; // don't remove mcminos
    }

    private void checkGhosts(LevelBlock lb) {
        if (lb == null) return;
        // check always if we met a ghost
        ArrayList<LevelObject> moveables = lb.getMovables(); // TODO: check all ghosts
        for (int i = moveables.size() - 1; i >= 0; i--) {
            if (i < moveables.size()) {
                LevelObject lo = moveables.get(i);
                int ghostnr = lo.getGhostNr();
                if (ghostnr != -1) {
                    // check if ghost is really near enough
                    if (distanceWithScroll(level.getScrollX(), mcminos.getVX(), lo.getVX(), level.getVPixelsWidth())
                            + distanceWithScroll(level.getScrollY(), mcminos.getVY(), lo.getVY(),
                            level.getVPixelsHeight()) < (PlayWindow.virtualBlockResolution)) {
                        if (mcminos.isPowered()) {
                            if (ghostnr == 3) { // jumping pill, will poison when powered
                                mcminos.poison();
                            } else { // all others can be killed when powered
                                game.removeMover(lo.getMover());
                                moveables.remove(i);
                                ghosts.decreaseGhosts(ghostnr);
                                lo.dispose();
                                audio.soundPlay("gotyou");
                                mcminos.increaseScore(30);
                            }
                        } else { // all others can be killed when powered
                            if (ghostnr == 3) { // jumping pill, can be eaten when not powered
                                game.removeMover(lo.getMover());
                                moveables.remove(i);
                                level.decreasePills();
                                ghosts.decreaseGhosts(ghostnr);
                                lo.dispose();
                                audio.soundPlay("knurps");
                                mcminos.increaseScore(30);
                            } else {
                                if (ghostnr == 1) { // perry only poisons
                                    mcminos.poison();
                                } else { // zara kills
                                    mcminos.kill("ghosts", Entities.mcminos_dying,false);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Check status of arrow-keys and save it
     * if none is pressed, return false
     * @return
     */
    public boolean updateKeyDirections() {
        keyDirections = 0;
        if(Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP) ) keyDirections += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ) keyDirections += 2;
        if(Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ) keyDirections += 4;
        if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT) ) keyDirections += 8;
        if(keyDirections > 0) {
            mcminos.unsetDestination();
            return true;
        }
        return false;
    }

    private final int mirrorTransform[] = {0,4,8,12,1,5,9,13,2,6,10,14,3,7,11,15};
    public int getKeyDirections() {
        int returnDirections = keyDirections;
        if(returnDirections == 0) returnDirections = touchpadDirections;
        return mcminos.isMirrored() ? mirrorTransform[returnDirections] : returnDirections;
    }  // TODO: think if mirror should be better handled in chooseDirection

    public int updateTouchpadDirections(float knobPercentX, float knobPercentY) {
        int directions = 0;
        if(knobPercentX > 0.2) directions += RIGHT;
        else if(knobPercentX < -0.2) directions += LEFT;
        if(knobPercentY > 0.2) directions += UP;
        else if(knobPercentY < -0.2) directions += DOWN;
        touchpadDirections = directions;
        if(touchpadDirections > 0) {
            mcminos.unsetDestination();
        }
        return touchpadDirections;
    }
}

/*
		case CLOCKOBJ-1:clearwall( x, y );
					snd_tool();
					if(timeactiv) leveltime+=60;
					inc_score( 10 );
					break;
		case SURPRISE-1:clearwall( x, y );
					choose_surprise( x, y );
					break;
*/