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
    private LevelObject pill=null; // a potential pill on this field
    private LevelObject castle=null; // a part of a castle
    private HashSet<LevelObject> movables=new HashSet<>(); // ghosts, mcminos, explosions hovering here.
    private HashSet<LevelObject> items=new HashSet<>(); // items on the field

    public LevelBlock(Level level, int x, int y) {
        this.level = level;
        this.x = x;
        this.y = y;

    }

    public void makeWall() {
        wall = new LevelObject(x,y,Entities.walls_default_00.getzIndex());
        updateWall();
    }

    public void updateWall() {
        if( wall != null ) { // only update, if this is a wall
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

    public void makeIndestructableWall() {
        // TODO: need graphics for indestructable wall
    }

    public void makeMcMinos() {
        LevelObject lo = new LevelObject(x,y,Entities.mcminos_default_front.getzIndex());
        lo.setGfx(Entities.mcminos_default_front);
        movables.add(lo);
        Game.mcminos=lo;
        Game.destination.setXY(x,y);
    }

    public void makePill() {
        LevelObject lo = new LevelObject(x,y,Entities.pills_pill_default.getzIndex());
        lo.setGfx(Entities.pills_pill_default);
        pill = lo;
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
        // TODO: add to right structure
    }
}
