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
    private int x;  // x-Position on level block
    private int y; // y-Position on level block
    private GameGraphics gfx; // actual Graphics for the object
    int zIndex = maxzIndex; // by default it is too high
    private double offsetX; // current x-offset regarding the level block [-0.5..+0.5[
    private double offsetY; // current y-offset regarding the level block [-0.5..+0.5[
    private static ArrayList<LevelObject> all = new ArrayList<LevelObject>();

    /**
     *
     * @param x
     * @param y
     * @param zIndex need to know zIndex to allow correct drawing order later
     */
    LevelObject(int x, int y, int zIndex) {
        this.x = x;
        this.y = y;
        offsetX = 0.0;
        offsetY = 0.0;
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

    private void draw(SpriteBatch batch, long gametime) {
        if(gfx != null) // castle parts can be null or invisible things
            gfx.draw(batch,gametime,x,y,offsetX,offsetY);
    }

    public static void drawAll(SpriteBatch batch, long gametime) {
        for (LevelObject lo : all) {
            if( lo.zIndex >= maxzIndex)
                break; // can be stopped, as null is infinity and therefore only null in the end
            lo.draw(batch, gametime);
        }
    }

    @Override
    public int compareTo(LevelObject lo) {
        return  zIndex - lo.zIndex;
    }
}
