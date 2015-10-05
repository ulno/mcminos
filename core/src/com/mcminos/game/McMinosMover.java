package com.mcminos.game;

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
    private int mcminosSpeedFactor = 1;

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
        if ( playwindow.isDestinationSet()) {
            int directions = 0; // direction bit field
            LevelObject destination = playwindow.getDestination();
            // check screen distance
            int x = levelObject.getVX();
            int xdelta = x - destination.getVX(); // delta to center of destination (two centers substract)
            int xdiff = Math.abs(xdelta);
            if (xdiff <= PlayWindow.virtualBlockResolution >> 1 || xdiff >= playwindow.getVPixelsLevelWidth() - (PlayWindow.virtualBlockResolution >> 1))
                xdelta = 0;
            else {
                //also allow this in non-scrolled levels
                //if (getScrollX() && xdiff >= getVPixelsLevelWidth() >> 1)
                if (xdiff >= playwindow.getVPixelsLevelWidth() >> 1)
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
                // also in non-scroll levels
                //if( getScrollY() && ydiff >= getVPixelsLevelHeight() >> 1 )
                if (ydiff >= playwindow.getVPixelsLevelHeight() >> 1)
                    ydelta = (int) Math.signum(ydelta);
                else
                    ydelta = -(int) Math.signum(ydelta);
            }

            if (ydelta > 0) directions += UP;
            if (ydelta < 0) directions += DOWN;
            if (xdelta > 0) directions += RIGHT;
            if (xdelta < 0) directions += LEFT;
            if (directions == 0) {
                playwindow.unsetDestination();
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
                    RockMover mover = new RockMover(rock, speed, currentDirection, nextBlock2);
                    rock.setMover(mover);
                    game.addMover(mover);
                    //mover.move(); //small headstart to arrive early enough - not necessary
                    nextBlock.setRock(null);
                    nextBlock2.setRock(rock);
                    audio.soundPlay("moverock");
                } else if(  ! m.isMoving() ) {
                    // let it move again
                    m.triggerMove(currentDirection, speed, nextBlock2);
                    nextBlock.setRock(null);
                    nextBlock2.setRock(rock);
                    audio.soundPlay("moverock");
                }
            }
            return nextBlock;
        }
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
                        audio.soundPlay("falling");
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
                if( mcminos.isPowered() ) {
                    if(ghostnr == 3) { // jumping pill, will poison when powered
                        // TODO: poison
                        audio.soundPlay("poison");
                    } else { // all others can be killed when powered
                        game.removeMover(lo.getMover());
                        moveables.remove(i);
                        ghosts.decreaseGhosts(ghostnr);
                        lo.dispose();
                        audio.soundPlay("gotyou");
                        // TODO: do score
                    }
                } else {
                    if(ghostnr == 3) { // jumping pill, can be eaten when not powered
                        game.removeMover(lo.getMover());
                        moveables.remove(i);
                        level.decreasePills();
                        ghosts.decreaseGhosts(ghostnr);
                        lo.dispose();
                        audio.soundPlay("knurps");
                    } else { // all others can be killed when powered
                        // TODO: kill McMinos
                        audio.soundPlay("ghosts");
                    }

                }
            }

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