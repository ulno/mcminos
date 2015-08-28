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
    private double currentSpeed = 1.0;

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
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
        this.currentSpeed = speed;
        this.gfxStill = still;
        this.gfxUp = up;
        this.gfxRight = right;
        this.gfxDown = down;
        this.gfxLeft = left;
        lo.setMover(this);
        this.currentLevelBlock = Game.getLevelBlock(lo.getX(), lo.getY());
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
        double x = levelObject.getX();
        double oldx = x;
        double y = levelObject.getY();
        double oldy = y;
        double distance = Math.min(0.5, Gdx.graphics.getDeltaTime() * Game.baseSpeed * currentSpeed ); // max half block

        // TODO: make decision to chose new direction first

        // move always until totally in a square level block, before a new direction is possible
        switch(currentDirection) {
            case STOP:
                levelObject.setGfx( gfxStill );
                break;
            case UP:
                y += distance;
                levelObject.setGfx( gfxUp );
                break;
            case RIGHT:
                x += distance;
                levelObject.setGfx( gfxRight );
                break;
            case DOWN:
                y -= distance;
                levelObject.setGfx( gfxDown );
                break;
            case LEFT:
                x -= distance;
                levelObject.setGfx( gfxLeft );
                break;
        }
        // check if new direction can be set,
        // for this to be true, distance must have passed a block-boundary or current direction has to be STOP
        int xi = (int) x;
        int yi = (int) y;
        if(currentDirection == directions.STOP || (Math.abs(xi -(int)oldx) + Math.abs(yi - (int)oldy)) >= 1) {
            //if( Math.abs(x%1) < Game.distanceEpsilon && Math.abs(y%1) < Game.distanceEpsilon )
            LevelBlock nextBlock = null;

            // check all direction choices
            for (directions dir : nextDirections) {
                // check if the new direction is actually not blocked
                switch (dir) {
                    case STOP: // no movement so no problem
                        nextBlock = Game.getLevelBlock(x, y);
                        //x = xi; // when we stop, forget fraction
                        //y = yi;
                        break;
                    case UP:
                        nextBlock = Game.level.getUp(xi, yi);
                        y += Math.abs(xi-x); // convert fractions in motion
                        break;
                    case RIGHT:
                        nextBlock = Game.level.getRight(xi, yi);
                        x += Math.abs(yi-y);
                        break;
                    case DOWN:
                        nextBlock = Game.level.getDown(xi, yi);
                        y -= Math.abs(xi - x); // convert fractions in motion
                        break;
                    case LEFT:
                        nextBlock = Game.level.getLeft(xi, yi);
                        x += Math.abs(yi - y);
                        break;
                }
                if (nextBlock != null && !nextBlock.hasWall()) {
                    currentDirection = dir;
                    break; // stop loop
                } else {
                    currentDirection = directions.STOP;
                    //x = xi; // when we stop, forget fraction
                    //y = yi;
                }
            }
        }
        levelObject.moveTo( x, y ); // finally move to new position
    }


    public void move( directions dir[] ) {
        nextDirections = dir;
        move();
    }

}
