package com.mcminos.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ulno on 17.08.15.
 *
 * Actual objects of a Level. Walls, McMinos, Doors, Ghosts and pills are
 * created here. the corresponding graphics are in GameGraphics
 */
public class LevelObject implements  Comparable<LevelObject> {
    public final static int maxzIndex=10000;
    private double x;  // x-Position in level blocks
    private double y; // y-Position in level blocks
    private GameGraphics gfx; // actual Graphics for the object
    int zIndex = maxzIndex; // by default it is too high
    private static ArrayList<LevelObject> all = new ArrayList<LevelObject>();

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     *
     * @param x in block coordinates
     * @param y in block coordinates (movable objects can have fraction as coordinate)
     * @param zIndex need to know zIndex to allow correct drawing order later
     */
    LevelObject(double x, double y, int zIndex) {
        this.x = x;
        this.y = y;
        this.zIndex = zIndex;
        // add to static list
        int index = Collections.binarySearch(all, this);
        if(index<0)
            index = - index - 1;
        all.add(index, this);
    }

    /*LevelObject(int x, int y) {
        LevelObject(x,y,maxzIndex);
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
}
