package com.mcminos.game;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;

/**
 * Created by ulno on 01.10.15.
 */
public class GhostMover extends Mover {

    private Game game;
    private Ghosts ghosts;
    private Audio audio;
    private Level level;
    private McMinos mcminos;
    private LevelBlock rememberedBlock;
    private ArrayList<LevelObject> currentItems;

    /**
     * needed for kryo-read
     */
    public GhostMover() {
        super();
    }

    public GhostMover(Game game, LevelObject ghost, int speed, int transwall, Graphics gfx) {
        super(ghost, ghost.getLevelBlock().getLevel().getLevelConfig().getGhostSpeed(ghost.getGhostNr()),speed, false, transwall, gfx);
        this.game = game;
        audio = game.getAudio();
        ghosts = game.getGhosts();
        mcminos = game.getMcMinos();
        level = ghost.getLevelBlock().getLevel(); // need to get it from here as else null
        this.rememberedBlock = currentLevelBlock;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        kryo.writeObjectOrNull(output, rememberedBlock, LevelBlock.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        rememberedBlock = kryo.readObjectOrNull(input, LevelBlock.class);
    }

    @Override
    public void initAfterKryoLoad(Game game, LevelObject lo) {
        super.initAfterKryoLoad(game, lo);
        this.game = game;
        audio = game.getAudio();
        ghosts = game.getGhosts();
        mcminos = game.getMcMinos();
        level = game.getLevel();
        rememberedBlock = level.get(rememberedBlock.getX(),rememberedBlock.getY());
    }

    @Override
    protected LevelBlock chooseDirection() {

        // ghost 1
        // usually follows mcminos (stupidly - doesn't know wraparound at level border)
        // never stops, never turns if there are two directions (continues in standard direction)
        // if there are at least 3 directions chose (when random doesn't fire) the best one to catch mcminos


        // Let's get our possibilities
        int dirs;
        if(transWall>0 && level.random(transWall) == 0) { // if transwall and lucky
            dirs = getUnblockedDirs(ALL, true, true);
        } else { // normal
            dirs = getUnblockedDirs(ALL, true, false);
        }
        int dircounter = 0;
        int dir = 1;
        for (int i = 0; i < 4; i++) {
            if ((dirs & dir) > 0)
                dircounter++;
            dir <<= 1;
        }
        int currentReverse = currentDirection << 2;
        if (currentReverse > 15) currentReverse >>= 4;
        int newDir = STOP;
        if (dircounter > 0) {
            if (dircounter > 1) { // 2-4 directions left
                if ((dirs & currentReverse) > 0) { // there is choice -> make sure to never turn back and remove reverse dir
                    dirs -= currentReverse;
                    dircounter--;
                }
            }
            if (dircounter > 1) { // Still 2 or 3 left
                // There is actually choice, so let's catch (or flee) from Mcminos
                // Play out agility/stupidity(random)
                int ghostNr = levelObject.getType().ordinal() - LevelObject.Types.Ghost1.ordinal();
                //level.ghostAgility[ghostNr] = 0; // force intelligence
                if (ghosts.evalAgility(ghostNr)) {
                    newDir = findDirForNr(level.random(dircounter), dirs); // play stupid
                } else { // try to be "smart" and either go to mcminos or flee
                    // check screen distance
                    int x = mcminos.getVX();
                    int y = mcminos.getVY();
                    int gx = levelObject.getVX();
                    int gy = levelObject.getVY();
                    // remove reverse direction
                    dirs &= 15 - currentReverse;
                    if (mcminos.isPowered() ^ ghostNr == 3) {// flee - reverse behaviour for jumping pill (type 3)
                        // find best
                        if (y < gy && (dirs & UP) > 0) newDir = UP;
                        else if (x < gx && (dirs & RIGHT) > 0) newDir = RIGHT;
                        else if (y > gy && (dirs & DOWN) > 0) newDir = DOWN;
                        else if (x > gx && (dirs & LEFT) > 0) newDir = LEFT;
                        else newDir = findDirForNr(0, dirs);
                    } else { // follow
                        // find best
                        if (y > gy && (dirs & UP) > 0) newDir = UP;
                        else if (x > gx && (dirs & RIGHT) > 0) newDir = RIGHT;
                        else if (y < gy && (dirs & DOWN) > 0) newDir = DOWN;
                        else if (x < gx && (dirs & LEFT) > 0) newDir = LEFT;
                        else newDir = findDirForNr(0, dirs);
                    }
                }
            } else { // only 1 direction, this might for example be, when you need to turn back
                newDir = findDirForNr(0, dirs);
            }
        }
        currentDirection = newDir;
        LevelBlock headingTo = currentLevelBlock;
        switch (newDir) {
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

    private int findDirForNr(int nr, int dirField) {
        for(int i=3; i>=0; i--) {
            if((dirField&1)==1) {
                if(nr==0) {
                    return 8>>i;
                }
                nr --;
            }
            dirField /= 2;
        }
        return STOP;
    }

    @Override
    protected boolean checkCollisions() {
        if( levelObject.fullOnBlock() ) {
            int ghostnr = levelObject.getGhostNr();
            if (currentLevelBlock != rememberedBlock) {
                if (rememberedBlock.hasOneWay()) { // let ghosts turn the oneways
                    rememberedBlock.turnOneWay(audio);
                }
            }
            rememberedBlock = currentLevelBlock;
            // check the things lying here
            LevelBlock currentBlock = currentLevelBlock;
            if(ghostnr == 1 ) { // pill dropper
                if(level.getGhostPillDrop() > 0) {
                    if(!currentBlock.hasPill() && !currentBlock.hasOneWay() &&
                            !currentBlock.hasCollectibles() && !currentBlock.hasHole() &&
                            level.random(level.getGhostPillFreq()) == 0) {
                        currentBlock.makePill();
                        level.decreaseGhostPillDrop();
                    }
                }
            }
            // check if here is max hole, because ghosts will fall in and die (even if they don't increase hole-sizes)
            if (currentBlock.hasHole() && currentBlock.getHole().holeIsMax()) {
                if(ghostnr == 3 ) { // jumping pill
                    level.decreasePills();
                    audio.soundPlay("knurps");
                }
                ghosts.decreaseGhosts(ghostnr);
                // Todo: think about sound to play for normal ghost
                return true; // remove me
            }
            // check the things lying here
            currentItems = currentBlock.getCollectibles();
            for( int i = currentItems.size()-1; i>=0; i--) {
                LevelObject b = currentItems.get(i);
                switch( b.getType() ) {
                    case LandMineActive:
                        b.dispose();
                        game.schedule(EventManager.Types.ExplosionLight, currentBlock);
                        break;
                    case WarpHole:
                        if (currentDirection != STOP) { // only teleport when moving onto field
                            teleportToBlock(level.getFreeWarpHole(currentBlock));
                        }
                        break;
                }
            }
        }
        return false;
    }

    public void teleportToBlock( LevelBlock block ) {
        levelObject.moveTo(block.getX() << PlayWindow.virtualBlockResolutionExponent,
                block.getY() << PlayWindow.virtualBlockResolutionExponent, block);
        setLevelBlock(block);
    }

}
