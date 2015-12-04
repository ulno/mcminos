package com.mcminos.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by ulno on 28.08.15.
 */
public abstract class Mover implements Json.Serializable {
    private Graphics gfxRight = null;
    private Graphics gfxUp = null;
    private Graphics gfxStill = null;
    private Graphics gfxDown = null;
    private Graphics gfxLeft = null;
    private int vPixelSpeed = 2; // move how many pixels per frame (needs to be a power of two)
    private int pixelSpeedAnder = 0x1000000 - vPixelSpeed;
    private int speedFactor = 1;

    public final static int STOP=0, UP=1, RIGHT=2, DOWN=4, LEFT=8, ALL=15;
    protected int currentDirection = STOP;
    protected LevelObject levelObject; // corresponding LevelObject
    protected LevelBlock currentLevelBlock; // current associated LevelBlock
    protected boolean canMoveRocks = false; // This moveable can move rocks (Main for example)
    protected int transWall; // Can this move through walls, then this is > 0
    protected LevelBlock lastBlock = null;
    protected LevelBlock headingTo;  // Block this object is heading to
    private boolean accelerated = false;

    /**
     *
     * compute from factor set
     */
    protected void computeSpeeds() {
        int accelerator = accelerated ? 2 : 1;
        vPixelSpeed = Game.baseSpeed * accelerator * speedFactor * PlayWindow.virtualBlockResolution / Game.timeResolution;
        pixelSpeedAnder = 0x1000000 - vPixelSpeed;
    }

    public int getVPixelSpeed() {
        return vPixelSpeed;
    }

    public void setSpeedFactor(int newFactor) {
        speedFactor = newFactor;
        computeSpeeds(); // apply speedfactor
    }

    public void setSpeedAccelerated( boolean accelerated) {
        this.accelerated = accelerated;
        computeSpeeds();
    }

    public boolean isAccelerated() {
        return accelerated;
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
        if(! levelObject.hasGfx() || still == null)
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
    public void init( LevelObject lo, int speed, boolean canMoveRocks, int transwall) {
        levelObject = lo;
        speedFactor = speed;
        this.transWall = transwall;
        computeSpeeds();
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
    public Mover( LevelObject lo, int speed, boolean canMoveRocks, int transWall, Graphics still, Graphics up, Graphics right, Graphics down, Graphics left) {
        init(lo, speed, canMoveRocks, transWall);
        setGfx(still, up, right, down, left);
    }

    public Mover( LevelObject lo, int speed, boolean canMoveRocks, int transWall, Graphics gfx )
    {
        init(lo, speed, canMoveRocks, transWall);
        setGfx(gfx);
    }

    public Mover( LevelObject lo, int speed, boolean canMoveRocks, int transWall )
    {
        init(lo, speed, canMoveRocks, transWall);
    }

    // empty for create by json (values are written in read andinitialized by InitAfterJSonLoad
    public Mover() {

    }


    protected int getUnblockedDirs(int filterMask, boolean checkOneway, boolean transwall) {
        return currentLevelBlock.getUnblockedDirs( filterMask, checkOneway, canMoveRocks, transwall);
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
        // does not work because of rounding errors currentLevelBlock = animation.moveTo(x, y); // finally move to new position
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
        computeSpeeds();
    }

    public LevelBlock getLastBlock() {
        return lastBlock;
    }

    public int getSpeedFactor() {
        return speedFactor;
    }

    public void setLevelObject(LevelObject levelObject) {
        this.levelObject = levelObject;
    }

    @Override
    public void write(Json json) {
        json.writeValue("u",gfxUp==null?-1:gfxUp.getAllGraphicsIndex());
        json.writeValue("r",gfxRight==null?-1:gfxRight.getAllGraphicsIndex());
        json.writeValue("d",gfxDown==null?-1:gfxDown.getAllGraphicsIndex());
        json.writeValue("l",gfxLeft==null?-1:gfxLeft.getAllGraphicsIndex());
        json.writeValue("s",gfxStill==null?-1:gfxStill.getAllGraphicsIndex());
        json.writeValue("cb",currentLevelBlock);
        json.writeValue("lb",lastBlock);
        json.writeValue("ht",headingTo);
        json.writeValue("sf",speedFactor);
        json.writeValue("a",accelerated);
        json.writeValue("cd",currentDirection);
        json.writeValue("mr",canMoveRocks);
        json.writeValue("tw",transWall);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        gfxUp = Graphics.getByIndex(json.readValue("u",Integer.class,jsonData));
        gfxRight = Graphics.getByIndex(json.readValue("r",Integer.class,jsonData));
        gfxDown = Graphics.getByIndex(json.readValue("d",Integer.class,jsonData));
        gfxLeft = Graphics.getByIndex(json.readValue("l",Integer.class,jsonData));
        gfxStill = Graphics.getByIndex(json.readValue("s",Integer.class,jsonData));
        currentLevelBlock = json.readValue("cb",LevelBlock.class,jsonData);
        lastBlock = json.readValue("lb",LevelBlock.class,jsonData);
        headingTo = json.readValue("ht",LevelBlock.class,jsonData);
        speedFactor =  json.readValue("sf",Integer.class,jsonData);
        accelerated =  json.readValue("a",Boolean.class,jsonData);
        currentDirection = json.readValue("cd",Integer.class,jsonData);
        canMoveRocks = json.readValue("mr",Boolean.class,jsonData);
        transWall = json.readValue("tw",Integer.class,jsonData);
        computeSpeeds();
    }

    public void initAfterJsonLoad( Game game, LevelObject lo ) {
        Level level = game.getLevel();
        levelObject = lo;
        if(currentLevelBlock != null)
            currentLevelBlock = level.get(currentLevelBlock.getX(),currentLevelBlock.getY());
        if(lastBlock != null)
            lastBlock = level.get(lastBlock.getX(),lastBlock.getY());
        if(headingTo != null)
            headingTo = level.get(headingTo.getX(), headingTo.getY());
        switch(levelObject.getType()) {
            case Ghost1:
            case Ghost2:
            case Ghost4:
                game.addMover(this);
                break;
            case Ghost3:
                game.addMover(this);
                level.increasePills();
                break;
            case Rock:
                game.addMover(this);
                break;
        }
    }

    public void remove(LevelObject lo) {
        if( levelObject == lo ) {
            levelObject = null;
        } else {
            Gdx.app.log("remove in Mover", "trying to remove illigal levelobject");
        }
    }
}
