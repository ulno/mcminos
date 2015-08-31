package com.mcminos.game;

import com.badlogic.gdx.Gdx;

/**
 * Created by ulno on 28.08.15.
 */
public class Mover {
    private GameGraphics gfxRight;
    private GameGraphics gfxUp;
    private GameGraphics gfxStill;
    private GameGraphics gfxDown;
    private GameGraphics gfxLeft;

    public enum directions {STOP,UP,RIGHT,DOWN,LEFT}
    private directions currentDirection = directions.STOP;
    private directions nextDirections[] = {directions.STOP};
    private LevelObject levelObject; // corresponding LevelObject
    private LevelBlock currentLevelBlock; // current associated LevelBlock
    private int currentPixelSpeed = 2; // move how many pixels per frame (needs to be a power of two)

    /**
     *
     * @param blocksPerSecond move how many blocks per second?
     */
    public void setCurrentSpeed(double blocksPerSecond) {
        this.currentPixelSpeed = (int) Math.round(blocksPerSecond * Game.baseSpeed * Game.virtualBlockResolution / Game.timeResolution);
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
    public void init( LevelObject lo, double speed, GameGraphics still, GameGraphics up, GameGraphics right, GameGraphics down, GameGraphics left) {
        levelObject = lo;
        setCurrentSpeed( speed );
        this.gfxStill = still;
        this.gfxUp = up;
        this.gfxRight = right;
        this.gfxDown = down;
        this.gfxLeft = left;
        lo.setMover(this);
        this.currentLevelBlock = Game.getLevelBlockFromVPixel(lo.getX(), lo.getY());
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
    public Mover( LevelObject lo, double speed, GameGraphics still, GameGraphics up, GameGraphics right, GameGraphics down, GameGraphics left) {
        init(lo, speed, still, up, right, down, left);
    }

    public Mover( LevelObject lo, double speed, GameGraphics gfx )
    {
        init(lo, speed, gfx, gfx, gfx, gfx, gfx);
    }

    public void move() {
        int x = levelObject.getX();
        int blockX = x >> Game.virtualBlockResolutionExponent;
        int y = levelObject.getY();
        int blockY = y >> Game.virtualBlockResolutionExponent;
        // old float calculation        double distance = Math.min(0.5, Gdx.graphics.getDeltaTime() * Game.baseSpeed * currentSpeed ); // max half block
        int distance = currentPixelSpeed;

        // allow direction change when on block-boundaries
        if (x % Game.virtualBlockResolution == 0 && y % Game.virtualBlockResolution == 0) {
            LevelBlock nextBlock = null;

            // check all direction choices
            for (directions dir : nextDirections) {
                // check if the new direction is actually not blocked
                switch (dir) {
                    case STOP: // no movement so no problem
                        nextBlock = Game.getLevelBlock(blockX, blockY);
                        break;
                    case UP:
                        nextBlock = Game.level.getUp(blockX, blockY);
                        break;
                    case RIGHT:
                        nextBlock = Game.level.getRight(blockX, blockY);
                        break;
                    case DOWN:
                        nextBlock = Game.level.getDown(blockX, blockY);
                        break;
                    case LEFT:
                        nextBlock = Game.level.getLeft(blockX, blockY);
                        break;
                }
                if (nextBlock != null && !nextBlock.hasWall()) {
                    currentDirection = dir;
                    break; // stop loop
                } else {
                    currentDirection = directions.STOP;
                }
            }
        }
        // do transformations for new direction
        switch (currentDirection) {
            case STOP:
                levelObject.setGfx(gfxStill);
                break;
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
        }
        levelObject.moveTo(x, y); // finally move to new position
    }


    public void move( directions dir[] ) {
        nextDirections = dir;
        move();
    }

}
