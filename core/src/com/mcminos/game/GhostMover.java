package com.mcminos.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

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
    int[] dirList = {0, 0, 0, 0};

    /**
     * needed for json-read
     */
    public GhostMover() {
        super();
    }

    public GhostMover(Game game, LevelObject ghost, int speed, int transwall, Graphics gfx) {
        super(ghost, speed, false, transwall, gfx);
        this.game = game;
        audio = game.getAudio();
        ghosts = game.getGhosts();
        mcminos = game.getMcMinos();
        level = ghost.getLevelBlock().getLevel(); // need to get it from here as else null
        this.rememberedBlock = currentLevelBlock;
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("rb",rememberedBlock);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        rememberedBlock = json.readValue("rb",LevelBlock.class,jsonData);
    }

    @Override
    public void initAfterJsonLoad(Game game, LevelObject lo) {
        super.initAfterJsonLoad(game, lo);
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
        if(transWall>0 && game.random(transWall) == 0) { // if transwall and lucky
            dirs = getUnblockedDirs(ALL, true, true);
        } else { // normal
            dirs = getUnblockedDirs(ALL, true, false);
        }
        for(int i= 3; i>=0; i--)
            dirList[i] = 0;
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
                    if(!currentBlock.hasPill() && game.random(level.getGhostPillFreq()) == 0) {
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
