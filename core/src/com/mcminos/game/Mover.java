package com.mcminos.game;

/**
 * Created by ulno on 28.08.15.
 */
public abstract class Mover {
    private Graphics gfxRight = null;
    private Graphics gfxUp = null;
    private Graphics gfxStill = null;
    private Graphics gfxDown = null;
    private Graphics gfxLeft = null;
    protected int speed;

    public final int STOP=0, UP=1, RIGHT=2, DOWN=4, LEFT=8, ALL=15;
    protected int currentDirection = STOP;
    //private int nextDirections = STOP; // This is actually a bit field
    protected LevelObject levelObject; // corresponding LevelObject
    protected LevelBlock currentLevelBlock; // current associated LevelBlock
    private int currentPixelSpeed = 2; // move how many pixels per frame (needs to be a power of two)
    protected boolean canMoveRocks = false; // This moveable can move rocks (Main for example)
    protected boolean canPassWalls = false; // Can this move through walls?
    protected LevelBlock lastBlock = null;

    /**
     *
     * @param blocksPerSecond move how many blocks per second?
     */
    public void setSpeed(int blocksPerSecond) {
        this.currentPixelSpeed = blocksPerSecond * Root.virtualBlockResolution / Root.timeResolution;
        this.speed = blocksPerSecond;
    }

    public int getSpeed() {
        return this.speed;
    }


    /**
     *
     * @param still graphics for standing still
     * @param up graphics for moving up
     * @param right graphics for moving right
     * @param down graphics for moving down
     * @param left  graphics for moving left
     */
    public void setGfx( Graphics still, Graphics up, Graphics right, Graphics down, Graphics left) {
        this.gfxStill = still;
        this.gfxUp = up;
        this.gfxRight = right;
        this.gfxDown = down;
        this.gfxLeft = left;
    }

    public void setGfx( Graphics allDirections) {
        setGfx( allDirections, allDirections, allDirections, allDirections, allDirections );
    }

    /**
     *
     * @param lo levelobject to move
     * @param speed mulitplier for Root.baseSpeed (usually 1.0)
     */
    public void init( LevelObject lo, int speed, boolean canMoveRocks) {
        levelObject = lo;
        setSpeed(speed);
        this.canMoveRocks = canMoveRocks;
        // obsolet lo.setMover(this);
        //this.currentLevelBlock = Root.getLevelBlockFromVPixel(lo.getVX(), lo.getVY());
        currentLevelBlock = lo.getLevelBlock();
        lastBlock = currentLevelBlock;
    }

    /**
     *
     * @param lo levelobject to move
     * @param speed mulitplier for Root.baseSpeed (usually 1.0)
     * @param still graphics for standing still
     * @param up graphics for moving up
     * @param right graphics for moving right
     * @param down graphics for moving down
     * @param left  graphics for moving left
     */
    public Mover( LevelObject lo, int speed, boolean canMoveRocks, Graphics still, Graphics up, Graphics right, Graphics down, Graphics left) {
        init(lo, speed, canMoveRocks);
        setGfx(still, up, right, down, left);
    }

    public Mover( LevelObject lo, int speed, boolean canMoveRocks, Graphics gfx )
    {
        init(lo, speed, canMoveRocks);
        setGfx(gfx);
    }

    public Mover( LevelObject lo, int speed, boolean canMoveRocks )
    {
        init(lo, speed, canMoveRocks);
    }

    /**
     *
     * @param nextBlock
     * @param nextBlock2
     * @return true if movement of current is possible in this direction, false if not
     */
    private boolean dirPossible( LevelBlock nextBlock, LevelBlock nextBlock2 ) {
        // TODO: respect the ghost which can walk through walls dependent on transwall
        if(nextBlock == null) return false;
        if(nextBlock2 == null) return false;
        if (nextBlock.hasRock()) { // then look forward
            if(canMoveRocks) {
                return !nextBlock2.hasGhost() && !nextBlock2.hasWall() && !nextBlock2.hasClosedDoor();
            } else return false;
        }
        return !nextBlock.hasWall() && !nextBlock.hasClosedDoor();
    }

    protected int getUnblockedDirs( int filterMask, boolean checkOneway) {
        int unblocked = 0;
        LevelBlock lb = currentLevelBlock;
        LevelBlock b1, b2;

        if( checkOneway && lb.hasOneWay() ) {
            filterMask &= 1 << (lb.getOneWayDir() - 1);
        }

        // Up
        if((filterMask & UP) > 0) {
            b1 = lb.up();
            b2 = b1.up();
            if (dirPossible(b1, b2)) unblocked += UP;
        }
        // Right
        if((filterMask & RIGHT) > 0) {
            b1 = lb.right();
            b2 = b1.right();
            if (dirPossible(b1, b2)) unblocked += RIGHT;
        }
        // Down
        if((filterMask & DOWN) > 0) {
            b1 = lb.down();
            b2 = b1.down();
            if (dirPossible(b1, b2)) unblocked += DOWN;
        }
        // Up
        if ((filterMask & LEFT) > 0) {
            b1 = lb.left();
            b2 = b1.left();
            if (dirPossible(b1, b2)) unblocked += LEFT;
        }

        return unblocked;
    }

    protected int getUnblockedDirs( ) {
        return getUnblockedDirs(ALL,true);
    }

    /**
     * return true, when this needs to be disposed after this call
     */
    public boolean move() {
        chooseDirection();

        int distance = currentPixelSpeed;
        int x = levelObject.getVX();
        int y = levelObject.getVY();
        // do transformations for new direction
        switch (currentDirection) {
            case UP:
                y += distance;
                levelObject.setGfx(gfxUp);
                break;
            case RIGHT:
                x += distance;
                levelObject.setGfx(gfxRight);
                break;
            case DOWN:
                y -= distance;
                levelObject.setGfx(gfxDown);
                break;
            case LEFT:
                x -= distance;
                levelObject.setGfx(gfxLeft);
                break;
            // default case should not happen ad will be treated as no movement
            default:
                levelObject.setGfx(gfxStill);
                break;
        }
        // update current block
        currentLevelBlock = levelObject.moveTo(x, y); // finally move to new position
        return checkCollisions(); // check potential collisons at new position
    }

    /**
     * check collision of this with mcminos or other things
     * return: true, if this object needs to be removed
     */
    protected abstract boolean checkCollisions();

    /**
     * This needs to set currentDirection
     */
    protected abstract void chooseDirection();

    public LevelObject getLevelObject() {
        return levelObject;
    }
}
