package com.mcminos.game;

import java.util.HashSet;

/**
 * Created by ulno on 17.08.15.
 *
 * These are the actual blocks the field consists off.
 *
 */
public class LevelBlock {
    private int x,y; // Position in Level Field
    private Level level; // corresponding level
    private LevelObject wall=null; // The wall or door connected here
    private LevelObject rock=null; // a rock connected to this field
    private LevelObject pill=null; // a potential pill on this field
    private LevelObject castle=null; // a part of a castle
    private HashSet<LevelObject> movables=new HashSet<>(); // ghosts, mcminos, explosions, rocks hovering here.
    private HashSet<LevelObject> items=new HashSet<>(); // items on the field


    /**
     * @param lo check if this is in movable or items (connected with this field)
     * @return
     */
    public boolean has( LevelObject lo) {
        return hasAsMovable(lo) || hasAsItem(lo);
    }

    public boolean hasAsMovable(LevelObject lo) {
        return movables.contains(lo);
    }

    public boolean hasAsItem(LevelObject lo) {
        return items.contains(lo);
    }

    /**
     *
     * @param lo remove this (either if in items or movables)
     * @return
     */
    public boolean remove( LevelObject lo)
    {
        return removeMovable(lo) || removeItem(lo);
    }

    public boolean removeMovable(LevelObject lo) {
        return movables.remove(lo);
    }

    public boolean removeItem(LevelObject lo) {
        return items.remove(lo);
    }

    public LevelBlock(Level level, int x, int y) {
        this.level = level;
        this.x = x;
        this.y = y;

    }

    public void updateWall() {
        // TODO: think about impact of neighborrelation when there are invisible walls -> now the neighbors know!
        if( wall != null && ! wall.isIndestructable() && ! wall.isInvisible()) { // only update, if this is a normal wall
            // create wall number, based on neighbors
            int wallNr = 0;
            LevelBlock u = level.getUp(x, y);
            LevelBlock r = level.getRight(x, y);
            LevelBlock d = level.getDown(x, y);
            LevelBlock l = level.getLeft(x, y);

            if (u != null && u.hasWall())
                wallNr += 1;
            if (r != null && r.hasWall())
                wallNr += 2;
            if (d != null && d.hasWall())
                wallNr += 4;
            if (l != null && l.hasWall())
                wallNr += 8;
            GameGraphics[] walls = { // TODO: consider how to switch to different walls (for example castle)!
                    Entities.walls_default_00,
                    Entities.walls_default_01,
                    Entities.walls_default_02,
                    Entities.walls_default_03,
                    Entities.walls_default_04,
                    Entities.walls_default_05,
                    Entities.walls_default_06,
                    Entities.walls_default_07,
                    Entities.walls_default_08,
                    Entities.walls_default_09,
                    Entities.walls_default_10,
                    Entities.walls_default_11,
                    Entities.walls_default_12,
                    Entities.walls_default_13,
                    Entities.walls_default_14,
                    Entities.walls_default_15
            };
            wall.setGfx(walls[wallNr]);
        }
    }

    public boolean hasWall() {
        return wall != null;
    }


    public void makeWall() {
        wall = new LevelObject(x,y,Entities.walls_default_00.getzIndex());
        updateWall();
    }

    public void makeIndestructableWall() {
        // TODO: need Andreas' graphics for indestructable wall
        wall = new LevelObject(x,y,Entities.walls_indestructable.getzIndex());
        wall.setGfx(Entities.walls_indestructable);
        wall.setIndestructable(true);
    }

    public void makeInvisibleWall() {
        wall = new LevelObject(x,y,Entities.walls_default_00.getzIndex());
        wall.setInvisible(true);
    }

    public void makeMcMinos() {
        LevelObject lo = new LevelObject(x,y,Entities.mcminos_default_front.getzIndex());
        lo.setGfx(Entities.mcminos_default_front);
        movables.add(lo);
        Game.mcminos=lo;
        Game.destination.setXY(x, y);
    }

    public void makePill() {
        putPill();
    }

    public void makePowerPill1() {
        LevelObject lo = new LevelObject(x,y,Entities.pills_power_pill_apple.getzIndex());
        lo.setGfx(Entities.pills_power_pill_apple);
        items.add(lo);
    }

    public void makeCastle() {
        castle = new LevelObject(x,y,Entities.castle_default.getzIndex());
        updateCastle();
    }

    public void updateCastle() {
        if( castle != null) { // only update if this is castle
            // only add graphics, when lower button corner of a
            // castle (so there needs to be a castle up and one right)
            LevelBlock u = level.getUp(x, y);
            LevelBlock r = level.getRight(x, y);
            if (u != null && u.hasCastle()
                    && r != null && r.hasCastle())
                castle.setGfx(Entities.castle_default);
        }
    }

    public boolean hasCastle() {
        return castle != null;
    }

    public void makeGhost1() {
        LevelObject lo = new LevelObject(x,y,Entities.ghosts_hanky.getzIndex());
        lo.setGfx(Entities.ghosts_hanky);
        movables.add(lo);
    }

    public void makeLive() {
        LevelObject lo = new LevelObject(x,y,Entities.pills_heart.getzIndex());
        lo.setGfx(Entities.pills_heart);
        items.add(lo);
    }

    public void makeLetter() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_letter.getzIndex());
        lo.setGfx(Entities.extras_letter);
        items.add(lo);
    }

    public void makeSkull() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_skull.getzIndex());
        lo.setGfx(Entities.extras_skull);
        items.add(lo);
    }

    public void makeBomb() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_bomb_default.getzIndex());
        lo.setGfx(Entities.extras_bomb_default);
        items.add(lo);
    }

    public void makeRock() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_rock.getzIndex());
        lo.setGfx(Entities.extras_rock);
        rock = lo;
        movables.add(lo);
        Game.movables.add(lo);
    }

    public void makeRockMe() {
        LevelObject lo = new LevelObject(x,y,Entities.extras_rock_me.getzIndex());
        lo.setGfx(Entities.extras_rock_me);
        lo.setRockme(true);
        level.increaseRockmes();
    }

    public void putMoveable(LevelObject lo) {
        movables.add(lo);
    }

    public void putItem(LevelObject lo) {
        items.add(lo);
    }

    public boolean hasRock() {
        return rock != null;
    }

    public LevelObject getRock() {
        return rock;
    }

    public void setRock(LevelObject rock) {
        this.rock = rock;
    }

    public boolean hasPill() {
        return pill != null;
    }


    public void removePill() {
        level.decreasePills();
        pill.dispose();
        pill = null;
    }

    public void putPill() {
        level.increasePills();
        LevelObject lo = new LevelObject(x,y,Entities.pills_pill_default.getzIndex());
        lo.setGfx(Entities.pills_pill_default);
        pill = lo;
    }

    /**
     * Create a hole in the ground
     * @param i 0: smallest, 4: biggest
     */
    public void makeHole(int i) {
        LevelObject lo = new LevelObject(x,y,Entities.holes_0.getzIndex());
        lo.setHoleLevel( i );
    }
}
