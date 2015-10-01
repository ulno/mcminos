package com.mcminos.game;

/**
 * Created by ulno on 01.10.15.
 */
public class GhostMover extends Mover {

    private LevelObject mcminos;

    public GhostMover(LevelObject ghost, LevelObject mcminos, int speed, Graphics gfx) {
        super(ghost, speed, false, gfx);
        this.mcminos = mcminos;
    }

    @Override
    protected void chooseDirection() {

        // ghost 1
        // usually follows mcminos (stupidly - doesn't know wraparound at level border)
        // never stops, never turns if there are two directions (continues in standard direction)
        // if there are at least 3 directions chose (when random doesn't fire) the best one to catch mcminos


        if (levelObject.fullOnBlock()) { // now we can chose a new direction
            // Let's get our possibilities
            int dirs = getUnblockedDirs(ALL,true);
            int[] dirList = {0,0,0,0};
            int dircounter = 0;
            int dir = 1;
            for(int i=0; i<4; i++) {
                if((dirs&dir)>0)
                    dirList[dircounter++] = dir;
                dir <<= 1;
            }
            int newdir = STOP;
            if( dircounter > 0) {
                if(dircounter > 1) {
                    int currentReverse = currentDirection << 2;
                    if( currentReverse > 15 ) currentReverse >>= 4;
                    if(dircounter > 2) {
                        // There is actually choice, so let's catch (or flee) from Mcminos
                        // Play out agility/stupidity(random)
                        int ghostNr = 0; // TODO: figure out ghost number
                        //Root.level.ghostAgility[ghostNr] = 0; // force intelligence
                        if(Root.level.ghostAgility[ghostNr] != 0 && Root.random(Root.level.ghostAgility[ghostNr]) == 0) {
                            newdir = dirList[Root.random(dircounter)]; // play stupid TODO: check if a turn around is allowed here
                        } else { // try to be "smart" and either go to mcminos or flee
                            // check screen distance
                            int x = mcminos.getVX();
                            int y = mcminos.getVY();
                            int gx = levelObject.getVX();
                            int gy = levelObject.getVY();
                            // remove reverse direction
                            dirs &= 15 - currentReverse;
                            if(Root.powerDuration > 0) {// flee
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
                        if(dirList[0] != currentReverse ) newdir = dirList[0];
                        else newdir = dirList[1];
                    }
                } else { // only 1 direction
                    newdir = dirList[0];
                }
            }
            currentDirection = newdir;
        }
    }

    @Override
    protected boolean checkCollisions() {
        return false;
    }

}
