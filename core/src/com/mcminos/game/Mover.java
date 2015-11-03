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
    private int speed = 1;
    private int vPixelSpeed = 2; // move how many pixels per frame (needs to be a power of two)
    private int pixelSpeedAnder = 0x1000000 - vPixelSpeed;
    private int speedFactor = 1;

    public final static int STOP=0, UP=1, RIGHT=2, DOWN=4, LEFT=8, ALL=15;
    protected int currentDirection = STOP;
    protected LevelObject levelObject; // corresponding LevelObject
    protected LevelBlock currentLevelBlock; // current associated LevelBlock
    protected boolean canMoveRocks = false; // This moveable can move rocks (Main for example)
    protected boolean canPassWalls = false; // Can this move through walls?
    protected LevelBlock lastBlock = null;
    private LevelBlock headingTo;  // Block this object is heading to


    /**
     *
     * @param blocksPerSecond move how many blocks per second?
     */
    protected void computeSpeeds(int blocksPerSecond) {
        vPixelSpeed = blocksPerSecond  * speedFactor * PlayWindow.virtualBlockResolution / Game.timeResolution;
        // set new speed as here it's allowed and should not cause problems
        speed = blocksPerSecond;
        pixelSpeedAnder = 0x1000000 - vPixelSpeed;
    }

    public int getVPixelSpeed() {
        return vPixelSpeed;
    }

    public void setSpeedFactor(int newFactor) {
        speedFactor = newFactor;
        computeSpeeds(speed); // apply speedfactor
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
        levelObject.setGfx(still); // make sure the first is active as it might else not be chosen
    }

    public void setGfx( Graphics allDirections) {
        setGfx( allDirections, allDirections, allDirections, allDirections, allDirections );
    }

    /**
     *
     * @param lo levelobject to move
     * @param speed mulitplier for Game.baseSpeed (usually 1.0)
     */
    public void init( LevelObject lo, int speed, boolean canMoveRocks) {
        levelObject = lo;
        computeSpeeds(speed);
        this.canMoveRocks = canMoveRocks;
        // obsolet lo.setMover(this);
        //this.currentLevelBlock = Game.getLevelBlockFromVPixel(lo.getVX(), lo.getVY());
        currentLevelBlock = lo.getLevelBlock();
        lastBlock = currentLevelBlock;
    }

    /**
     *
     * @param lo levelobject to move
     * @param speed mulitplier for Game.baseSpeed (usually 1.0)
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


    protected int getUnblockedDirs(int filterMask, boolean checkOneway) {
        return currentLevelBlock.getUnblockedDirs( filterMask, checkOneway, canMoveRocks);
    }

    protected int getUnblockedDirs( ) {
        return getUnblockedDirs(ALL, true);
    }

    /**
     * return true, when this needs to be disposed after this call
     */
    public boolean move() {
        // allow direction change when on block-boundaries
        if( levelObject.fullOnBlock() ) {
            headingTo = chooseDirection();
        }

        int x = levelObject.getVX();
        int y = levelObject.getVY();
        // do transformations for new direction
        switch (currentDirection) {
            case UP:
                y &= pixelSpeedAnder;
                y += vPixelSpeed;
                levelObject.setGfx(gfxUp);
                break;
            case RIGHT:
                x &= pixelSpeedAnder;
                x += vPixelSpeed;
                levelObject.setGfx(gfxRight);
                break;
            case DOWN:
                y &= pixelSpeedAnder;
                y -= vPixelSpeed;
                levelObject.setGfx(gfxDown);
                break;
            case LEFT:
                x &= pixelSpeedAnder;
                x -= vPixelSpeed;
                levelObject.setGfx(gfxLeft);
                break;
            // default case should not happen and will be treated as no movement
            default:
                levelObject.setGfx(gfxStill);
                break;
        }
        // update current block
        // does not work because of rounding errors currentLevelBlock = levelObject.moveTo(x, y); // finally move to new position
        currentLevelBlock = levelObject.moveTo(x, y, headingTo);

        return checkCollisions(); // check potential collisons at new position
    }

    /**
     * check collision of this with mcminos or other things
     * return: true, if this object needs to be removed
     */
    protected abstract boolean checkCollisions();

    /**
     * This needs to set currentDirection and return the LevelBlock where this is headed
     */
    protected abstract LevelBlock chooseDirection();

    public LevelObject getLevelObject() {
        return levelObject;
    }

    public void setLevelBlock(LevelBlock levelBlock) {
        this.currentLevelBlock = levelBlock;
    }

    public void stop() {
        vPixelSpeed = 0;
        pixelSpeedAnder = 0xffffff;
    }

    public void resume() {
        computeSpeeds(speed);
    }

    public LevelBlock getLastBlock() {
        return lastBlock;
    }
}
