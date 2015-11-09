package com.mcminos.game;

import java.util.ArrayList;

/**
 * Created by ulno on 05.10.15.
 */
public class Ghosts {
    private final Game game;
    private final McMinos mcminos;
    private final Audio audio;
    private Level level;
    private int ghostSpeed[] = {1,1,1,1};
    private int ghostsActive[] = {0,0,0,0};
    private int[] ghostSpawnCounter = {-1,-1,-1,-1};

    public final LevelObject.Types ghostTypes[] = {
            LevelObject.Types.Ghost1, LevelObject.Types.Ghost2,
            LevelObject.Types.Ghost3, LevelObject.Types.Ghost4 };
    public final Graphics[] ghostEntities = {
            Entities.ghosts_hanky, Entities.ghosts_perry,
            Entities.ghosts_zarathustra, Entities.ghosts_jumpingpill };


    public Ghosts(Game game) {
        this.game = game;
        mcminos = game.getMcMinos();
        audio = game.getAudio();
        // usually still null here needs to be read in create level = game.getLevel();
        // call init to read it
    }

    public LevelObject create(LevelBlock block, int ghostnr) {
        LevelObject.Types ghosttype = ghostTypes[ghostnr];
        LevelObject lo = new LevelObject( block, ghostEntities[ghostnr], ghosttype );
        lo.animationStartRandom();
        block.addMovables(lo);
        level = block.getLevel();
        Mover mover = new GhostMover(game, lo, ghostSpeed[ghostnr],
                level.ghostTranswall[ghostnr], ghostEntities[ghostnr]);
        lo.setMover(mover);
        game.addMover(mover);
        ghostsActive[ghostnr] ++;
        if(ghostnr == 3) {
            level.increasePills(); // don't use level yet as it might be uninitialized
        }
        return lo;
    }

    public LevelObject spawn(int ghostnr) {
        LevelBlock randomBlock = level.getRandomCastleBlock();
        if(randomBlock != null ) return create( randomBlock, ghostnr );
        return null;
    }

    public void checkSpawn() {
        // check, if ghosts in castles need to be spawned
        for( int i=0; i<level.ghostMax.length; i++) {
            if(ghostsActive[i] < level.ghostMax[i] ) {
                if(ghostSpawnCounter[i] == -1) { // nothing counting right now
                    // initialize new spawn counter
                    ghostSpawnCounter[i] = level.ghostTime[i] << Game.timeResolutionExponent;
                }
                if(ghostSpawnCounter[i] >= 0) {
                    ghostSpawnCounter[i] --;
                    if( ghostSpawnCounter[i] == 0) {
                        // Time to spawn a new ghost
                        spawn(i);
                    }
                }
            }
        }
    }

    public void decreaseGhosts(int ghostnr) {
        ghostsActive[ghostnr] --;
    }


    public void setSpeedFactor(int gosNewFactor) {
        /*for(int i=0; i<4; i++)
        {
            ghostSpeed[i] /= ghostSpeedFactor;
            ghostSpeed[i] *= gosNewFactor;
        }
        ghostSpeedFactor = gosNewFactor;*/
        ArrayList<Mover> movers = game.getMovers();
        for(Mover m: movers) {
            if(m.getLevelObject().getGhostNr() != -1) {
                ((GhostMover)m).setSpeedFactor(gosNewFactor);
            }
        }
    }

    public boolean evalAgility(int ghostNr) {
        return level.ghostAgility[ghostNr] != 0 && game.random(level.ghostAgility[ghostNr]) == 0;
    }

    public void init() {
        level = game.getLevel();
    }

    public void removeAll(boolean score) {
        ArrayList<Mover> movers = game.getMovers();
        for( int i=movers.size()-1; i>=0; i--) {
            Mover m = movers.get(i);
            LevelObject lo = m.getLevelObject();
            int ghostnr = lo.getGhostNr();
            if( ghostnr != -1) {
                if (ghostnr == 3) { // jumping pill
                    level.decreasePills();
                }
                decreaseGhosts(ghostnr);
                movers.remove(i);
                lo.getLevelBlock().removeMovable(lo);
                lo.dispose();
                if(score) mcminos.increaseScore(30);
            }
        }
    }

    public void killall() {
        removeAll(true);
    }

    public void dispose() {
        removeAll(false);
    }
}
