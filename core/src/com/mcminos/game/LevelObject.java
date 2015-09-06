package com.mcminos.game;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ulno on 17.08.15.
 *
 * Actual objects of a Level. Walls, McMinos, Doors, Ghosts and pills are
 * created here. the corresponding graphics are in GameGraphics
 */
public class LevelObject implements  Comparable<LevelObject> {


    public enum Types {Unspecified, Power1, Power2, IndestructableWall, InvisibleWall, Rockme, Ghost1, Live, Letter, Skull, Bomb, Dynamite, Rock, Pill, Castle, McMinos, Wall, Background, Key, Umbrella, DoorClosed, DoorOpened, SpeedUpField, SpeedDownField, WarpHole, KillAllField, OneWay, Hole};
    public enum DoorTypes {None, HorizontalOpened,HorizontalClosed, VerticalOpened,VerticalClosed};
    public final static int maxzIndex=10000;
    private int x; // windowVPixelXPos-Position in level blocks * virtualBlockResolution
    private int y; // windowVPixelYPos-Position in level blocks * virtualBlockResolution
    private GameGraphics gfx; // actual Graphics for the object
    private int zIndex = maxzIndex; // by default it is too high
    private static ArrayList<LevelObject> all = new ArrayList<LevelObject>();
    private Mover mover = null;
    private LevelBlock levelBlock = null; // currently associated LevelBlock
    private int holeLevel;
    private Types type;
    private DoorTypes doorType = DoorTypes.None;

    /**
     *
     * @param x in block coordinates
     * @param y in block coordinates (movable objects can have fraction as coordinate)
     * @param zIndex need to know zIndex to allow correct drawing order later
     */
    LevelObject(int x, int y, int zIndex, Types type) {
        this.x = x << Game.virtualBlockResolutionExponent;
        this.y = y << Game.virtualBlockResolutionExponent;
        this.zIndex = zIndex;
        // add to static list
        int index = Collections.binarySearch(all, this);
        if(index<0)
            index = - index - 1;
        all.add(index, this);
    }

    /*LevelObject(int windowVPixelXPos, int windowVPixelYPos) {
        LevelObject(windowVPixelXPos,windowVPixelYPos,maxzIndex);
    }
*/
    public void setGfx(GameGraphics gfx) {
        this.gfx = gfx;
    }

    private void draw() {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.draw(x,y);
    }

    public static void drawAll() {
        for (LevelObject lo : all) {
            if( lo.zIndex >= maxzIndex)
                break; // can be stopped, as null is infinity and therefore only null in the end
            lo.draw();
        }
    }

    @Override
    public int compareTo(LevelObject lo) {
        return  zIndex - lo.zIndex;
    }

    public void moveTo(int x, int y) {
        LevelBlock from = levelBlock;
        // check and eventually fix coordinates
        // if(Game.getScrollX()) { allways allow
            if (x < 0.0) x += Game.getLevelWidth() << Game.virtualBlockResolutionExponent;
            if (x >= Game.getLevelWidth() << Game.virtualBlockResolutionExponent)
                x -= Game.getLevelWidth() << Game.virtualBlockResolutionExponent;
        //}
        //if(Game.getScrollY()) {
            if (y < 0.0) y += Game.getLevelHeight() << Game.virtualBlockResolutionExponent;
            if (y >= Game.getLevelHeight() << Game.virtualBlockResolutionExponent)
                y -= Game.getLevelHeight() << Game.virtualBlockResolutionExponent;
        //}

        LevelBlock to = Game.getLevelBlockFromVPixel(x, y);
        // needs to be updated to check for collisions via associations
        if(from != null)
            from.removeMovable(this);
        setXY(x,y);
        to.putMoveable(this);
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * assign a matching LevelBlock based on th ecurrent coordinates
     */
    public void assignLevelBlock() {
        levelBlock = Game.getLevelBlockFromVPixel(x, y);
    }


    public boolean hasGfx() {
        return gfx != null;
    }

    public void setMover(Mover mover) {
        this.mover = mover;
    }

    public Mover getMover() {
        return mover;
    }

    public void move() {
        if( mover != null) {
            mover.calculateDirection();
            mover.move();
        }
    }

    // make sure to remove yourself from list
    public void dispose() {
        all.remove(this);
        // TODO: think if we also have to remove from other things in level-block
    }

    public boolean isIndestructable() {
        return type == Types.IndestructableWall;
    }

    public boolean isInvisible() {
        return type == Types.InvisibleWall;
    }

    public boolean isRockme() {
        return type == Types.Rockme;
    }

    public void setHoleLevel(int holeLevel) {
        this.holeLevel = holeLevel;
        // set gfx
        GameGraphics[] holes =
                new GameGraphics[]{Entities.holes_0, Entities.holes_1,
                        Entities.holes_2, Entities.holes_3, Entities.holes_4};
        this.setGfx( holes[holeLevel]);
    }

    public int getVX() {
        return x;
    }
    public int getVY() {
        return y;
    }

    public void setDoorType(DoorTypes doorType) {
        this.doorType = doorType;
    }

    public DoorTypes getDoorType() {
        return doorType;
    }

}
