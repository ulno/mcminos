package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;

/**
 * Created by ulno on 01.10.15.
 */
public class McMinosMover extends Mover {

    private final Game game;
    private final McMinos mcminos;
    private PlayWindow playwindow;
    private final Audio audio;
    private final Level level;
    private final Ghosts ghosts;
    private int keyDirections = 0;
    private int touchpadDirections = 0;


    public McMinosMover(Game game) {
        super(game.getMcMinos().getLevelObject(), Game.baseSpeed, true);
        this.game = game;
        audio = game.getAudio();
        mcminos = game.getMcMinos();
        this.playwindow = game.getPlayWindow();
        this.level = game.getLevel();
        ghosts = game.getGhosts();
        mcminos.setMover(this);
        mcminos.gfxNormal();
    }

    @Override
    public LevelBlock chooseDirection() {
        // this is only called, when on block boundaries
        int directions = getKeyDirections(); // direction bit field
        if ( directions == 0 && mcminos.isDestinationSet()) { // if no key, then try to get from destination
            LevelObject destination = mcminos.getDestination();
            // check screen distance
            int x = levelObject.getVX();
            int xdelta = x - destination.getVX(); // delta to center of destination (two centers substract)
            int xdiff = Math.abs(xdelta);
            if (xdiff <= PlayWindow.virtualBlockResolution >> 1 || xdiff >= playwindow.getVPixelsLevelWidth() - (PlayWindow.virtualBlockResolution >> 1))
                xdelta = 0;
            else {
                if( level.getScrollX() && xdiff >= playwindow.getVPixelsLevelWidth() / 2 )
                    xdelta = (int) Math.signum(xdelta);
                else
                    xdelta = -(int) Math.signum(xdelta);
            }
            int y = levelObject.getVY();
            int ydelta = y - destination.getVY(); // delta to center of destination (two centers substract)
            int ydiff = Math.abs(ydelta);
            if (ydiff <= PlayWindow.virtualBlockResolution >> 1 || ydiff >= playwindow.getVPixelsLevelHeight() - (PlayWindow.virtualBlockResolution >> 1))
                ydelta = 0;
            else {
                if( level.getScrollY() && ydiff >= playwindow.getVPixelsLevelHeight() / 2 )
                    ydelta = (int) Math.signum(ydelta);
                else
                    ydelta = -(int) Math.signum(ydelta);
            }

            if (ydelta > 0) directions += UP;
            if (ydelta < 0) directions += DOWN;
            if (xdelta > 0) directions += RIGHT;
            if (xdelta < 0) directions += LEFT;
            if (directions == 0) {
                mcminos.hideDestination();
            }

            // prefer longer distance (narrow down to one choice)
            if( (directions & (UP+DOWN)) > 0 && (directions & (LEFT+RIGHT)) > 0 ) {
                if (xdiff > ydiff) {
                    directions &= LEFT + RIGHT;
                } else {
                    directions &= UP + DOWN;
                }
            }
        }
        if( directions > 0) { // got something in directions
            // refine with possible directions
            directions = getUnblockedDirs(directions,true);

            LevelBlock nextBlock=null;

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
                    if((directions & UP) > 0) {
                        nextBlock = currentLevelBlock.up();
                        directions = UP;
                    }
                    else if((directions & RIGHT) > 0) {
                        nextBlock = currentLevelBlock.right();
                        directions = RIGHT;
                    }
                    else if((directions & DOWN) > 0) {
                        nextBlock = currentLevelBlock.down();
                        directions = DOWN;
                    }
                    else /* LEFT */ {
                        nextBlock = currentLevelBlock.left();
                        directions = LEFT;
                    }
                    break;
            }
            currentDirection = directions; // start moving there
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
                    RockMover mover = new RockMover(rock, getVPixelSpeed(), currentDirection, nextBlock2);
                    rock.setMover(mover);
                    game.addMover(mover);
                    //mover.move(); //small headstart to arrive early enough - not necessary
                    nextBlock.setRock(null);
                    nextBlock2.setRock(rock);
                    audio.soundPlay("moverock");
                } else if(  ! m.isMoving() ) {
                    // let it move again
                    m.triggerMove(currentDirection, getVPixelSpeed(), nextBlock2);
                    nextBlock.setRock(null);
                    nextBlock2.setRock(rock);
                    audio.soundPlay("moverock");
                }
            }
            return nextBlock;
        }
        // nothing found or no direction set so return currentblock and no direction possibility
        currentDirection = STOP;
        return currentLevelBlock;
    }

    /**
     * Check Mcminos'  collisions (mainly if mcminos found something and can collect it)
     * @return
     */
    @Override
    protected boolean checkCollisions() {
        // check if something can be collected (only when full on field)
        if(mcminos.fullOnBlock()) {
            LevelBlock currentBlock = game.getLevelBlockFromVPixel(mcminos.getVX(), mcminos.getVY());
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
                        lastBlock.getHole().increaseHole();
                    }
                    if (lastBlock.hasOneWay()) {
                        lastBlock.turnOneWay();
                    }
                    // check if here is max hole
                    if (currentBlock.hasHole() && currentBlock.getHole().holeIsMax()) {
                        audio.soundPlay("falling");
                        mcminos.fall();
                    }
                }
                // check the things lying here
                for( LevelObject b:currentBlock.getCollectibles()) {
                    switch( b.getType() ) {
                        case Chocolate:
                            audio.soundPlay("tools");
                            mcminos.increaseChocolates();;
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Bomb:
                            audio.soundPlay("tools");
                            mcminos.increaseBombs();;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case Dynamite:
                            audio.soundPlay("tools");
                            mcminos.increaseDynamites();;
                            currentBlock.removeItem(b);
                            b.dispose();
                            // no score as droppable increaseScore(10);
                            break;
                        case LandMine:
                            audio.soundPlay("tools");
                            mcminos.increaseLandmines();;
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
                            audio.soundPlay("tools");
                            mcminos.increaseKeys();;
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Umbrella:
                            audio.soundPlay("tools");
                            mcminos.increaseUmbrellas();;
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Live:
                            mcminos.increaseLives();
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseScore(10);
                            break;
                        case Power1:
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.setPowerPillValues(2, 1, 10);
                            // sound played in ppill method
                            break;
                        case Power2:
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.setPowerPillValues(1, 2, 10);
                            break;
                        case Power3:
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.setPowerPillValues(1, 1, 10);
                            break;
                        case SpeedUpField:
                            setSpeedFactor(2);
                            audio.soundPlay("speedup");
                            break;
                        case SpeedDownField:
                            setSpeedFactor(1);
                            audio.soundPlay("slowdown");
                            break;
                        case KillAllField:
                            ghosts.killall();
                            audio.soundPlay("killall");
                            break;
                        case KillAllPill:
                            currentBlock.removeItem(b);
                            b.dispose();
                            ghosts.killall();
                            audio.soundPlay("killall");
                            break;
                        case Bonus1:
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseScore(100);
                            audio.soundPlay("treasure");
                            break;
                        case Bonus2:
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseScore(200);
                            audio.soundPlay("treasure");
                            break;
                        case Bonus3:
                            currentBlock.removeItem(b);
                            b.dispose();
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
                            currentBlock.removeItem(b);
                            b.dispose();
                        case SkullField:
                            mcminos.kill("skullkill",Entities.mcminos_dying);
                            break;
                        case Poison:
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.poison();
                            break;
                        case Medicine:
                            audio.soundPlay("tools");
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.increaseMedicines();
                            break;
                        case Mirror:
                            audio.soundPlay("fade");
                            currentBlock.removeItem(b);
                            b.dispose();
                            mcminos.toggleMirrored();
                            break;
                    }
                }
            }
            lastBlock = currentBlock;
        }
        // check always if we met a ghost
        ArrayList<LevelObject> moveables = currentLevelBlock.getMovables();
        for( int i=moveables.size()-1; i>=0; i--) {
            LevelObject lo = moveables.get(i);
            int ghostnr = lo.getGhostNr();
            if(ghostnr != -1) {
                // check if ghost is really near enough
                if (Math.abs((playwindow.getVPixelsLevelWidth() + mcminos.getVX() - lo.getVX()) % playwindow.getVPixelsLevelWidth()) < (PlayWindow.virtualBlockResolution >> 1)
                        && Math.abs((playwindow.getVPixelsLevelHeight() + mcminos.getVY() - lo.getVY()) % playwindow.getVPixelsLevelHeight()) < (PlayWindow.virtualBlockResolution >> 1)) {
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
                    } else {
                        if (ghostnr == 3) { // jumping pill, can be eaten when not powered
                            game.removeMover(lo.getMover());
                            moveables.remove(i);
                            level.decreasePills();
                            ghosts.decreaseGhosts(ghostnr);
                            lo.dispose();
                            audio.soundPlay("knurps");
                            mcminos.increaseScore(30);
                        } else { // all others can be killed when powered
                            mcminos.kill("ghosts", Entities.mcminos_dying);
                        }
                    }
                }
            }

        }
        // check winning condition
        if(level.getPillsNumber() == 0 && level.getRockmesNumber() == 0) {
            mcminos.win();
        }
        return false; // don't remove mcminos
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
		case MEDICINE1-1:clearwall( x, y );
					snd_tool();
					inc_score( 10 );
					carry[CARRYANTIDOT]++;
					break;
		case CLOCKOBJ-1:clearwall( x, y );
					snd_tool();
					if(timeactiv) leveltime+=60;
					inc_score( 10 );
					break;
		case POISON1-1:clearwall( x, y );
					pacpoison();
					retwert = 0;
					break;
		case SKULL-1: retwert = 0; spec_action = 1;
					power = 0; kill_mcminos(); break;
		case SURPRISE-1:clearwall( x, y );
					choose_surprise( x, y );
					break;
		case LADDER-1: retwert = 0; pills_left=0; spec_action=1;
    inc_score(10); break;
    case TRUHE-1:clearwall( x, y );
    snd_tool();
    inc_score( 500 );
    break;
    case GELDSACK-1:clearwall( x, y );
    snd_tool();
    inc_score( 250 );
    break;
    case SPARSCHWEIN-1:clearwall( x, y );
    snd_tool();
    inc_score( 100 );
    break;
    case SPEEDUP-1: snd_speedup(); speedup = 1; break;
    case SLOWDOWN-1: snd_slowdown(); speedup = 0; break;
    case MIRROR-1:clearwall( x, y );
    inc_score( 10 );
    snd_mirror();
    mirrorflag = !mirrorflag;
    break;
    case WHISKEY-1:clearwall( x, y );
    snd_drunken();
    drunken += 16;
    inc_score( 5 );
    break;
    case KILLALL-1:clearwall( x, y );
    snd_killall();
    inc_score( goscount[0] * 10 );
    ghostkillflag = 1;
    spec_action = 1;
    break;
    case KILLALL2-1: snd_killall();
    inc_score( goscount[0] * 10 );
    ghostkillflag = 1;
    spec_action = 1;
    break;
    case SECRETLETTER-1:clearwall( x, y );
    //snd_letter();
    inc_score( 10 );
    found_letter = 1;
    spec_action = 1;
    break;
    case LOCH-1: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH+1: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH+2: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        change_field( x, y, levfield( y, x).type+1 );
        snd_hole();
    }
    break;
    case LOCH+3: if(!umbrflag) // Wenn kein Regenschirm aktiviert
    {
        McMinos_hole();
        retwert = 0;
    }
    break;
    case WARP-1:spec_action = 1;
    stop_moving = 1;
    do_warp = 1;
    retwert = 0;
    break;
    case MINEDOWN-1: mine_expl( x, y ); break;
    case MINEUP-1:change_field( x, y, levfield(y, x).extra);
    snd_tool();
    inc_score( 10 );
    carry[CARRYMINE]++;
    break;
}


*/