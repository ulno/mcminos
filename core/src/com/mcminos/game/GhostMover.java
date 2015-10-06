package com.mcminos.game;

/**
 * Created by ulno on 01.10.15.
 */
public class GhostMover extends Mover {

    private final Game game;
    private final Ghosts ghosts;
    private final Audio audio;
    private final Level level;
    private McMinos mcminos;

    public GhostMover(Game game, LevelObject ghost, int speed, Graphics gfx) {
        super(ghost, speed, false, gfx);
        this.game = game;
        audio = game.getAudio();
        ghosts = game.getGhosts();
        mcminos = game.getMcMinos();
        level = game.getLevel();
    }

    @Override
    protected LevelBlock chooseDirection() {

        // ghost 1
        // usually follows mcminos (stupidly - doesn't know wraparound at level border)
        // never stops, never turns if there are two directions (continues in standard direction)
        // if there are at least 3 directions chose (when random doesn't fire) the best one to catch mcminos


        // Let's get our possibilities
        int dirs = getUnblockedDirs(ALL, true);
        int[] dirList = {0, 0, 0, 0};
        int dircounter = 0;
        int dir = 1;
        for (int i = 0; i < 4; i++) {
            if ((dirs & dir) > 0)
                dirList[dircounter++] = dir;
            dir <<= 1;
        }
        int newdir = STOP;
        if (dircounter > 0) {
            if (dircounter > 1) {
                int currentReverse = currentDirection << 2;
                if (currentReverse > 15) currentReverse >>= 4;
                if (dircounter > 2) {
                    // There is actually choice, so let's catch (or flee) from Mcminos
                    // Play out agility/stupidity(random)
                    int ghostNr = levelObject.getType().ordinal() - LevelObject.Types.Ghost1.ordinal();
                    //level.ghostAgility[ghostNr] = 0; // force intelligence
                    if (ghosts.evalAgility(ghostNr)) {
                        newdir = dirList[game.random(dircounter)]; // play stupid TODO: check if a turn around is allowed here
                    } else { // try to be "smart" and either go to mcminos or flee
                        // check screen distance
                        int x = mcminos.getVX();
                        int y = mcminos.getVY();
                        int gx = levelObject.getVX();
                        int gy = levelObject.getVY();
                        // remove reverse direction
                        dirs &= 15 - currentReverse;
                        if (mcminos.isPowered() ^ ghostNr == 3) {// flee - reverse bahavour for jumping pill (type 3)
                            // find best
                            if (y < gy && (dirs & UP) > 0) newdir = UP;
                            else if (x < gx && (dirs & RIGHT) > 0) newdir = RIGHT;
                            else if (y > gy && (dirs & DOWN) > 0) newdir = DOWN;
                            else if (x > gx && (dirs & LEFT) > 0) newdir = LEFT;
                            else if (dirList[0] != currentReverse) newdir = dirList[0];
                            else newdir = dirList[1];
                        } else { // follow
                            // find best
                            if (y > gy && (dirs & UP) > 0) newdir = UP;
                            else if (x > gx && (dirs & RIGHT) > 0) newdir = RIGHT;
                            else if (y < gy && (dirs & DOWN) > 0) newdir = DOWN;
                            else if (x < gx && (dirs & LEFT) > 0) newdir = LEFT;
                            else if (dirList[0] != currentReverse) newdir = dirList[0];
                            else newdir = dirList[1];
                        }
                    }
                } else { // Two options, just make sure we are not turning around
                    if (dirList[0] != currentReverse) newdir = dirList[0];
                    else newdir = dirList[1];
                }
            } else { // only 1 direction
                newdir = dirList[0];
            }
        }
        currentDirection = newdir;
        LevelBlock headingTo = currentLevelBlock;
        switch (newdir) {
            case UP:
                return currentLevelBlock.up();
            case RIGHT:
                return currentLevelBlock.right();
            case DOWN:
                return currentLevelBlock.down();
            case LEFT:
                return currentLevelBlock.left();
        }
        return currentLevelBlock;
    }

    @Override
    protected boolean checkCollisions() {
        if( levelObject.fullOnBlock() ) {
            // check the things lying here
            LevelBlock currentBlock = currentLevelBlock;
            // TODO: if this is the second ghost, drop pills
            for( LevelObject b:currentBlock.getCollectibles()) {
                switch( b.getType() ) {
                    case LandMineActive:
                        currentBlock.removeItem(b);
                        b.dispose();
                        new Explosion(currentBlock, LevelObject.Types.LandMine);
                        break;
                }
            }
            // check if here is max hole, because ghosts will fall in and die (even if they don't increase hole-sizes)
            if (currentBlock.hasHole() && currentBlock.getHole().holeIsMax()) {
                int ghostnr = levelObject.getGhostNr();
                if(ghostnr == 3 ) { // jumping pill
                    level.decreasePills();
                    audio.soundPlay("knurps");
                }
                ghosts.decreaseGhosts(ghostnr);
                // Todo: think about sound to play for mormal ghost
                return true; // remove me
            }
        }
        return false;
    }




}
