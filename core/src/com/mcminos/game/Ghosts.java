package com.mcminos.game;

/**
 * Created by ulno on 05.10.15.
 */
public class Ghosts {
    private final Game game;
    private final McMinos mcminos;
    private Level level;
    private int ghostSpeed[] = {Game.baseSpeed,Game.baseSpeed,Game.baseSpeed,Game.baseSpeed};
    private int ghostsActive[] = {0,0,0,0};
    private int[] ghostSpawnCounter = {-1,-1,-1,-1};
    private int ghostSpeedFactor = 1;

    public final LevelObject.Types ghostTypes[] = {
            LevelObject.Types.Ghost1, LevelObject.Types.Ghost2,
            LevelObject.Types.Ghost3, LevelObject.Types.Ghost4 };
    public final Graphics[] ghostEntities = {
            Entities.ghosts_hanky, Entities.ghosts_panky,
            Entities.ghosts_zarathustra, Entities.ghosts_jumpingpill };


    public Ghosts(Game game) {
        this.game = game;
        mcminos = game.getMcMinos();
        // usually still null here needs to be read in create level = game.getLevel();
        // call init to read it
    }

    public LevelObject create(LevelBlock block, int ghostnr) {
        LevelObject.Types ghosttype = ghostTypes[ghostnr];
        LevelObject lo = new LevelObject( block, ghostEntities[ghostnr], ghosttype );
        lo.animationStartRandom();
        block.addMovables(lo);
        Mover mover = new GhostMover(game, lo, ghostSpeed[ghostnr], ghostEntities[ghostnr]);
        lo.setMover(mover);
        game.addMover(mover);
        ghostsActive[ghostnr] ++;
        if(ghostnr == 3) {
            block.getLevel().increasePills(); // don't use level yet as it might be uninitialized
        }
        return lo;
    }

    public LevelObject spawn(int ghostnr) {
        return create( level.getRandomCastleBlock(), ghostnr );
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


    public void setGhostSpeedFactor(int gosNewFactor) {
        for(int i=0; i<4; i++)
        {
            ghostSpeed[i] /= ghostSpeedFactor;
            ghostSpeed[i] *= gosNewFactor;
            // TODO: set ghost speed in ghostmover
        }
        ghostSpeedFactor = gosNewFactor;

    }

    public boolean evalAgility(int ghostNr) {
        return level.ghostAgility[ghostNr] != 0 && game.random(level.ghostAgility[ghostNr]) == 0;
    }

    public void init() {
        level = game.getLevel();
    }
}