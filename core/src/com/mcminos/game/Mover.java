package com.mcminos.game;

/**
 * Created by ulno on 28.08.15.
 */
public class Mover {
    private Graphics gfxRight = null;
    private Graphics gfxUp = null;
    private Graphics gfxStill = null;
    private Graphics gfxDown = null;
    private Graphics gfxLeft = null;
    private int speed;


    public enum directions {STOP,UP,RIGHT,DOWN,LEFT}
    private directions currentDirection = directions.STOP;
    private directions nextDirections[] = {directions.STOP};
    private LevelObject levelObject; // corresponding LevelObject
    private LevelBlock currentLevelBlock; // current associated LevelBlock
    private int currentPixelSpeed = 2; // move how many pixels per frame (needs to be a power of two)
    private boolean canMoveRocks = false; // This moveable can move rocks (Main for example)
    private boolean canPassWalls = false; // Can this move through walls?
    private MoverDirectionChooser directionChooser;

    /**
     *
     * @param blocksPerSecond move how many blocks per second?
     */
    public void setCurrentSpeed(int blocksPerSecond) {
        this.currentPixelSpeed = blocksPerSecond * Root.virtualBlockResolution / Root.timeResolution;
        this.speed = blocksPerSecond;
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
    public void init( LevelObject lo, int speed, boolean canMoveRocks, MoverDirectionChooser dirChooser) {
        levelObject = lo;
        setCurrentSpeed(speed);
        lo.setMover(this);
        this.currentLevelBlock = Root.getLevelBlockFromVPixel(lo.getVX(), lo.getVY());
        this.canMoveRocks = canMoveRocks;
        this.directionChooser = dirChooser;
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
    public Mover( LevelObject lo, int speed, boolean canMoveRocks, MoverDirectionChooser dirChooser, Graphics still, Graphics up, Graphics right, Graphics down, Graphics left) {
        init(lo, speed, canMoveRocks, dirChooser);
        setGfx(still, up, right, down, left);
    }

    public Mover( LevelObject lo, int speed, boolean canMoveRocks, MoverDirectionChooser dirChooser, Graphics gfx )
    {
        init(lo, speed, canMoveRocks, dirChooser);
        setGfx(gfx);
    }

    public Mover( LevelObject lo, int speed, boolean canMoveRocks, MoverDirectionChooser dirChooser )
    {
        init(lo, speed, canMoveRocks, dirChooser);
    }

    public void move() {
        int x = levelObject.getVX();
        int blockX = x >> Root.virtualBlockResolutionExponent;
        int y = levelObject.getVY();
        int blockY = y >> Root.virtualBlockResolutionExponent;
        // old float calculation        double distance = Math.min(0.5, Gdx.graphics.getDeltaTime() * Root.baseSpeed * currentSpeed ); // max half block
        int distance = currentPixelSpeed;

        // allow direction change when on block-boundaries
        if (x % Root.virtualBlockResolution == 0 && y % Root.virtualBlockResolution == 0) {
            LevelBlock nextBlock = null, nextBlock2 = null;

            // check all direction choices
            for (directions dir : nextDirections) {
                // check if the new direction is actually not blocked
                switch (dir) {
                    case STOP: // no movement so no problem
                        nextBlock = Root.getLevelBlock(blockX, blockY);
                        nextBlock2 = nextBlock;
                        break;
                    case UP:
                        nextBlock = Root.level.getUp(blockX, blockY, true);
                        nextBlock2 = Root.level.getUp2(blockX, blockY); // for figuring out, if rocks are movable
                        break;
                    case RIGHT:
                        nextBlock = Root.level.getRight(blockX, blockY, true);
                        nextBlock2 = Root.level.getRight2(blockX, blockY);
                        break;
                    case DOWN:
                        nextBlock = Root.level.getDown(blockX, blockY, true);
                        nextBlock2 = Root.level.getDown2(blockX, blockY);
                        break;
                    case LEFT:
                        nextBlock = Root.level.getLeft(blockX, blockY, true);
                        nextBlock2 = Root.level.getLeft2(blockX, blockY);
                        break;
                }
                if (nextBlock != null && !nextBlock.hasWall() && !nextBlock.hasClosedDoor()) {
                    if(nextBlock.hasRock()) {
                        // TODO: check that there are no ghosts
                        if(canMoveRocks) { // check if this rock could actually be moved (not if there is rock or wall)
                            if(dir != directions.STOP && !(nextBlock2.hasWall() || nextBlock2.hasRock() || nextBlock2.hasClosedDoor()))
                            {
                                currentDirection = dir; // start moving there
                                // also make rock in the speed we push it
                                LevelObject rock = nextBlock.getRock();
                                Mover mover = new Mover(rock,this.speed,false, Root.moverRock,Entities.extras_rock);
                                rock.setMover(mover);
                                Root.soundPlay("moverock");
                                mover.move(currentDirection);
                                // move the rock to next field
                                nextBlock.setRock( null );
                                nextBlock2.setRock( rock );
                                break; // stop loop
                            }
                        }
                    }
                    else {
                        currentDirection = dir;
                        break; // stop loop
                    }
                }
                currentDirection = directions.STOP; // if we come here and don't break, then we must stop
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

    public void calculateDirection() {
        nextDirections = directionChooser.chooseDirection(levelObject);
    }


    public void move( directions dir[] ) {
        nextDirections = dir;
        move();
    }

    public void move( directions dir ) {
        nextDirections = new directions[]{dir};
        move();
    }

}
