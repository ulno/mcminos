package com.mcminos.game;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;

/**
 * Created by ulno on 17.08.15.
 *
 * These are the actual blocks the field consists off.
 *
 */
public class LevelBlock implements KryoSerializable {
//    private final Game game;
//    private final Audio audio;
//    private final McMinos mcminos;
//    private final Ghosts ghosts;
    private int x,y; // Position in Level Field
    private Level level; // corresponding level
    private LevelObject wall=null; // The wall or door connected here
    private int wallIndex = -1; // Index of the wall
    private LevelObject rock=null; // a rock connected to this field
    private LevelObject pill=null; // a potential pill on this field
    private LevelObject castle=null; // a part of a castle
    private LevelObject hole = null;
    private LevelObject oneWay = null;
    private int oneWayType = -1; // -1, no oneway, 0 up, 1 right, 2 down, 3 left, +4 rotatable

    /**
     * for kryo-read
     */
    public LevelBlock() {
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output,x);
        kryo.writeObject(output,y);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        x = kryo.readObject(input,Integer.class);
        y = kryo.readObject(input,Integer.class);
    }
    /**
     *
     * @param nextBlock
     * @param nextBlock2
     * @return true if movement of current is possible in this direction, false if not
     */
    private boolean dirPossible( LevelBlock nextBlock, LevelBlock nextBlock2, boolean canMoveRocks, boolean transwall ) {
        if(nextBlock == null) return false;
        if(nextBlock.hasClosedDoor()) return false;
        if (nextBlock.hasRock()) { // then look forward
            if(canMoveRocks) {
                if(nextBlock2 == null) return false;
                return !nextBlock2.hasGhost() && !nextBlock2.hasRock() && !nextBlock2.hasWall() && !nextBlock2.hasClosedDoor();
            } else return false;
        }
        if( transwall ) { // only indestructable wall stops this guy now
            if (nextBlock.hasWall() )
                return nextBlock.getWall().getType() != LevelObject.Types.IndestructableWall;
            else return true;
        }
        return !nextBlock.hasWall();
    }

    public int getUnblockedDirs( int filterMask, boolean checkOneway, boolean canMoveRocks, boolean transwall ) {
        int unblocked = 0;
        LevelBlock lb = this;
        LevelBlock b1, b2;

        if( checkOneway && lb.hasOneWay() ) {
            filterMask &= 1 << (lb.getOneWayDir() - 1);
        }

        // Up
        if((filterMask & Mover.UP) > 0) {
            b1 = lb.up();
            b2 = lb.up2();
            if (dirPossible(b1, b2, canMoveRocks, transwall)) unblocked += Mover.UP;
        }
        // Right
        if((filterMask & Mover.RIGHT) > 0) {
            b1 = lb.right();
            b2 = lb.right2();
            if (dirPossible(b1, b2, canMoveRocks, transwall)) unblocked += Mover.RIGHT;
        }
        // Down
        if((filterMask & Mover.DOWN) > 0) {
            b1 = lb.down();
            b2 = lb.down2();
            if (dirPossible(b1, b2, canMoveRocks, transwall)) unblocked += Mover.DOWN;
        }
        // Up
        if ((filterMask & Mover.LEFT) > 0) {
            b1 = lb.left();
            b2 = lb.left2();
            if (dirPossible(b1, b2, canMoveRocks, transwall)) unblocked += Mover.LEFT;
        }

        return unblocked;

    }

    public int getUnblockedDirs(boolean canMoveRocks, boolean transwall) {
        return getUnblockedDirs(Mover.ALL, true, canMoveRocks, transwall);
    }

    public void removePill() {
        pill.dispose();
    }

    public int getOneWayType() {
        return oneWayType;
    }

    enum oneWayDir {FREE, UP, RIGHT, DOWN, LEFT};
    private final oneWayDir oneWayDirMap[] = {oneWayDir.FREE, oneWayDir.UP, oneWayDir.RIGHT, oneWayDir.DOWN, oneWayDir.LEFT};
    private ArrayList<LevelObject> movables=new ArrayList<>(); // ghosts, mcminos, explosions, rocks hovering here.
    private ArrayList<LevelObject> collectibles =new ArrayList<>(); // collectibles on the field
    private LevelObject door=null; // a potential door
    private boolean rockme = false;
    private Graphics[] walls = {
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

    /**
     * This is exposed to let others iterate and find all ghosts
     * @return
     */
    public ArrayList<LevelObject> getMovables() {
        return movables;
    }


    /**
     * @param lo check if this is in movable or collectible (connected with this field)
     * @return
     */
    public boolean has( LevelObject lo) {
        return hasAsMovable(lo) || hasAsItem(lo);
    }

    public boolean hasAsMovable(LevelObject lo) {
        return movables.contains(lo);
    }

    public boolean hasAsItem(LevelObject lo) {
        return collectibles.contains(lo);
    }

    /**
     *
     * @param lo remove this (either if in collectibles or movables or other references)
     * @return
     */
    public boolean remove( LevelObject lo)
    {
        boolean removed = false;
        // depending on type, this has to be removed from fields from the current levelblock
        switch(lo.getType()) {
            case Unspecified:
                break;
            case McMinos:
                movables.remove(lo);
                break;
            case Wall:
            case IndestructableWall:
            case InvisibleWall:
                if(wall == lo) {
                    wall = null;
                    removed = true;
                }
                break;
            case DoorClosed:
            case DoorOpened:
                if(door == lo) {
                    door = null;
                    removed = true;
                }
                break;
            case Pill:
                if(pill == lo) {
                    pill = null;
                    level.decreasePills();
                }
                break;
            case Rockme:
                level.decreaseRockmes();
                rockme = false;
                break;
            case Rock:
                if(rock == lo) {
                    if(isRockme()) level.increaseRockmes();
                    rock = null;
                    removed = true;
                    movables.remove(lo);
                }
                break;
            case Ghost1:
            case Ghost2:
            case Ghost3:
            case Ghost4:
                movables.remove(lo);
                removed = true;
                break;
            case Hole:
                if(hole == lo) {
                    hole = null;
                    removed = true;
                }
                break;
            case OneWay:
                if(oneWay == lo) {
                    oneWay = null;
                    removed = true;
                }
                break;
            default: // try to remove from collectibles or moveables
                removed = collectibles.remove(lo);
                if(!removed) {
                    removed = movables.remove(lo);
                }
        }
        return removed;
    }

    public void add(LevelObject lo) {
        // depending on type add it to the right fields
        switch(lo.getType()) {
            case Unspecified:
                break;
            case McMinos:
                movables.add(lo);
                break;
            case Wall:
            case IndestructableWall:
            case InvisibleWall:
                wall = lo;
                break;
            case DoorClosed:
            case DoorOpened:
                door = lo;
                break;
            case Pill:
                level.increasePills();
                pill = lo;
                break;
            case Rockme:
                if(!hasRock()) {
                    level.increaseRockmes();
                }
                rockme = true;
                break;
            case Rock:
                if(isRockme()) {
                    level.decreaseRockmes();
                }
                rock = lo;
                movables.add(lo);
                break;
            case Ghost1:
            case Ghost2:
            case Ghost3:
            case Ghost4:
                movables.add(lo);
                break;
            case Hole:
                hole = lo;
                break;
            case OneWay:
                oneWay = lo;
                oneWayType = lo.getInitOneWayType();
                break;
            default: // collectible
                collectibles.add(lo);
        }

    }

    public LevelBlock(Level level, int x, int y) {
        this.level = level;
//        game = level.getGame();
//        audio = game.getAudio();
//        mcminos = game.getMcMinos();
//        ghosts = game.getGhosts();
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
            wall.setGfx(walls[wallNr]);
            wallIndex = wallNr;
        }
    }

    public void updateDoor() {
        if( door != null ) { // There is a door here
            LevelBlock u = level.getUp(x, y);
            //LevelBlock r = level.getRight(x, y);
            LevelBlock d = level.getDown(x, y);
            //LevelBlock l = level.getLeft(x, y);
            boolean isopen = door.getDoorType() == LevelObject.DoorTypes.HorizontalOpened ||
                    door.getDoorType() == LevelObject.DoorTypes.VerticalOpened;
            if (u != null && d != null && u.hasWall() && d.hasWall()) {
                if(isopen) {
                    door.setGfx(Entities.walls_door_open_vertical);
                    door.setDoorType(LevelObject.DoorTypes.VerticalOpened);
                }
                else {
                    door.setGfx(Entities.walls_door_closed_vertical);
                    door.setDoorType(LevelObject.DoorTypes.VerticalClosed);
                }
            }
            else {// WE will assume then we take a horizontal door
                if(isopen) {
                    door.setGfx(Entities.walls_door_open_horizontal);
                    door.setDoorType(LevelObject.DoorTypes.HorizontalOpened);
                }
                else {
                    door.setGfx(Entities.walls_door_closed_horizontal);
                    door.setDoorType(LevelObject.DoorTypes.HorizontalClosed);
                }
            }
        }
    }

    public boolean hasWall() {
        return wall != null;
    }


    public void makeWall() {
        wall = new LevelObject(level,x,y,Entities.walls_default_00.getzIndex(),LevelObject.Types.Wall);
        updateWall();
    }

    public void makeIndestructableWall() {
        // TODO: need Andreas' graphics for indestructable wall
        wall = new LevelObject(level,x,y,Entities.walls_indestructable.getzIndex(),LevelObject.Types.IndestructableWall);
        wall.setGfx(Entities.walls_indestructable);
    }

    public void makeInvisibleWall() {
        wall = new LevelObject(level,x,y,Entities.walls_default_00.getzIndex(),LevelObject.Types.InvisibleWall);
    }

    public void makeMcMinos(McMinos mcminos) {
        mcminos.initLevelBlockAndObject(level,x,y); // creates levelobject and relation to levelblock
        mcminos.initDestination();
        movables.add(mcminos.getLevelObject());
    }

    public void makePill() {
        LevelObject lo = new LevelObject(level,x,y,Entities.pills_pill_default.getzIndex(),LevelObject.Types.Pill);
        lo.setGfx(Entities.pills_pill_default);
        // add(lo), already done in new LevelObject;
    }

    public void makePowerPill1() {
        LevelObject lo = new LevelObject(level,x,y,Entities.pills_power_pill_cookie.getzIndex(),LevelObject.Types.Power1);
        lo.setGfx(Entities.pills_power_pill_cookie);
        // add(lo), already done in new LevelObject;
    }

    public void makePowerPill2() {
        LevelObject lo = new LevelObject(level,x,y,Entities.pills_power_pill_milk.getzIndex(),LevelObject.Types.Power2);
        lo.setGfx(Entities.pills_power_pill_milk);
        // add(lo), already done in new LevelObject;
    }

    public void makePowerPill3() {
        LevelObject lo = new LevelObject(level,x,y,Entities.pills_power_pill_mushroom.getzIndex(),LevelObject.Types.Power3);
        lo.setGfx(Entities.pills_power_pill_mushroom);
        // add(lo), already done in new LevelObject;
    }

    public void makeCastle(Game game) {
        castle = new LevelObject(level,x,y,Entities.castle_default.getzIndex(),LevelObject.Types.Castle);
        updateCastle(game);
        level.addCastle(castle);
    }

    public void updateCastle(Game game) {
        if( castle != null) { // only update if this is castle
            // only add graphics, when lower button corner of a
            // castle (so there needs to be a castle up and one right)
            LevelBlock u = level.getUp(x, y);
            LevelBlock r = level.getRight(x, y);
            if (u != null && u.hasCastle()
                    && r != null && r.hasCastle()) {
                castle.setGfx(Entities.castle_default);
                castle.animationStartRandom(game); // make sure not all are animated the same
            }
        }
    }

    public boolean hasCastle() {
        return castle != null;
    }

    public void makeGhost(int typenr, Ghosts ghosts) {
        ghosts.create(this, typenr);
    }

    public void makeLive() {
        LevelObject lo = new LevelObject(level,x,y,Entities.pills_heart.getzIndex(),LevelObject.Types.Live);
        lo.setGfx(Entities.pills_heart);
        // add(lo), already done in new LevelObject;
    }

    public void makeLetter() {
        /*
        currently deactivated unitl thishas a function again
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_letter.getzIndex(),LevelObject.Types.Letter);
        lo.setGfx(Entities.extras_letter);
        */
        // add(lo), already done in new LevelObject;
    }

    public void makeSkull() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_skull.getzIndex(),LevelObject.Types.Skull);
        lo.setGfx(Entities.extras_skull);
        // add(lo), already done in new LevelObject;
    }

    public void makeSkullField() {
        LevelObject lo = new LevelObject(level,x,y,Entities.fields_field_skull.getzIndex(),LevelObject.Types.SkullField);
        lo.setGfx(Entities.fields_field_skull);
        // add(lo), already done in new LevelObject;
    }

    public void makeBomb() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_bomb_default.getzIndex(),LevelObject.Types.Bomb);
        lo.setGfx(Entities.extras_bomb_default);
        // add(lo), already done in new LevelObject;
    }

    public void makeDynamite() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_dynamite_default.getzIndex(),LevelObject.Types.Dynamite);
        lo.setGfx(Entities.extras_dynamite_default);
        // add(lo), already done in new LevelObject;
    }

    public void makeLandMine() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_land_mine_default.getzIndex(),LevelObject.Types.LandMine);
        lo.setGfx(Entities.extras_land_mine_default);
        // add(lo), already done in new LevelObject;
    }

    public void makeLandMineActivated() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_land_mine_active.getzIndex(),LevelObject.Types.LandMineActive);
        lo.setGfx(Entities.extras_land_mine_active);
        // add(lo), already done in new LevelObject;
    }

    public void makeKey() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_key.getzIndex(),LevelObject.Types.Key);
        lo.setGfx(Entities.extras_key);
        // add(lo), already done in new LevelObject;
    }

    public void makeUmbrella() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_umbrella.getzIndex(),LevelObject.Types.Umbrella);
        lo.setGfx(Entities.extras_umbrella);
        // add(lo), already done in new LevelObject;
    }


    public void makeRock() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_rock.getzIndex(),LevelObject.Types.Rock);
        lo.setGfx(Entities.extras_rock);
        // add(lo), already done in new LevelObject;
    }

    public void makeRockMe() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_rock_me.getzIndex(),LevelObject.Types.Rockme);
        lo.setGfx(Entities.extras_rock_me);
        // add(lo), already done in new LevelObject;
    }

    public void makeDoorClosed() {
        LevelObject lo = new LevelObject(level,x,y,Entities.walls_door_closed_horizontal.getzIndex(),LevelObject.Types.DoorClosed);
        // graphics comes in wall update
        lo.setDoorType(LevelObject.DoorTypes.HorizontalClosed);
        // add(lo), already done in new LevelObject;
    }

    public void makeDoorOpened() {
        LevelObject lo = new LevelObject(level,x,y,Entities.walls_door_closed_horizontal.getzIndex(),LevelObject.Types.DoorOpened);
        // graphics comes in wall update
        lo.setDoorType(LevelObject.DoorTypes.HorizontalOpened);
        // add(lo), already done in new LevelObject;
    }

    /*public void putMoveable(LevelObject lo) {
        movables.add(lo);
    }*/

    public boolean hasRock() {
        return rock != null;
    }

    public LevelObject getRock() {
        return rock;
    }

    /*public void setRock(LevelObject rock) {
        this.rock = rock;
    }*/

    public boolean hasPill() {
        return pill != null;
    }


    /**
     * Create a hole in the ground
     * @param i 0: smallest, 4: biggest
     */
    public void makeHole(int i) {
        LevelObject lo = new LevelObject(level,x,y,Entities.holes_0.getzIndex(),LevelObject.Types.Hole);
        lo.setHoleLevel(i);
        hole = lo;
    }

    public boolean hasClosedDoor() {
        if( door == null) return false;
        return door.getDoorType() == LevelObject.DoorTypes.HorizontalClosed || door.getDoorType() == LevelObject.DoorTypes.VerticalClosed;
    }

    public boolean hasDoor() {
        return door !=  null;
    }


    public void makeSpeedUpField() {
        LevelObject lo = new LevelObject(level,x,y,Entities.fields_field_speed_up.getzIndex(),LevelObject.Types.SpeedUpField);
        lo.setGfx(Entities.fields_field_speed_up);
        // add(lo), already done in new LevelObject;
    }

    public void makeSpeedDownField() {
        LevelObject lo = new LevelObject(level,x,y,Entities.fields_field_speed_down.getzIndex(),LevelObject.Types.SpeedDownField);
        lo.setGfx(Entities.fields_field_speed_down);
        // add(lo), already done in new LevelObject;
    }

    public void makeWarpHole() {
        LevelObject lo = new LevelObject(level,x,y,Entities.fields_warp_hole.getzIndex(),LevelObject.Types.WarpHole);
        lo.setGfx(Entities.fields_warp_hole);
        level.addWarpHole(this);
        // add(lo), already done in new LevelObject;
    }

    public void makeKillAllPill() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_kill_all_ghosts.getzIndex(),LevelObject.Types.KillAllPill);
        lo.setGfx(Entities.extras_kill_all_ghosts);
        // add(lo), already done in new LevelObject;
    }

    public void makeKillAllField() {
        LevelObject lo = new LevelObject(level,x,y,Entities.fields_field_kill_all_ghosts.getzIndex(),LevelObject.Types.KillAllField);
        lo.setGfx(Entities.fields_field_kill_all_ghosts);
        // add(lo), already done in new LevelObject;
    }

    public void makeExit() {
        LevelObject lo = new LevelObject(level,x,y,Entities.fields_field_level_up.getzIndex(),LevelObject.Types.Exit);
        lo.setGfx(Entities.fields_field_level_up);
        // add(lo), already done in new LevelObject;
    }

    public void makeBonus1() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_bonus1.getzIndex(),LevelObject.Types.Bonus1);
        lo.setGfx(Entities.extras_bonus1);
        // add(lo), already done in new LevelObject;
    }

    public void makeBonus2() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_bonus2.getzIndex(),LevelObject.Types.Bonus2);
        lo.setGfx(Entities.extras_bonus2);
        // add(lo), already done in new LevelObject;
    }

    public void makeBonus3() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_bonus3.getzIndex(),LevelObject.Types.Bonus3);
        lo.setGfx(Entities.extras_bonus3);
        // add(lo), already done in new LevelObject;
    }

    public void makeSurprise() {
        // TODO: add images - missing
        /*LevelObject lo = new LevelObject(level,x,y,Entities..getzIndex(),LevelObject.Types.Bonus3);
        lo.setGfx(Entities.extras_bonus_piggy_bank);
        // add(lo), already done in new LevelObject;*/
    }

    public void makeWhisky() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_whisky.getzIndex(),LevelObject.Types.Whisky);
        lo.setGfx(Entities.extras_whisky);
        // add(lo), already done in new LevelObject;
    }

    public void makeMirror() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_mirror.getzIndex(),LevelObject.Types.Mirror);
        lo.setGfx(Entities.extras_mirror);
        // add(lo), already done in new LevelObject;
    }

    public void makePoison() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_poison.getzIndex(),LevelObject.Types.Poison);
        lo.setGfx(Entities.extras_poison);
        // add(lo), already done in new LevelObject;
    }

    public void makeMedicine() {
        LevelObject lo = new LevelObject(level,x,y,Entities.extras_medicine.getzIndex(),LevelObject.Types.Medicine);
        lo.setGfx(Entities.extras_medicine);
        // add(lo), already done in new LevelObject;
    }


    /**
     * Make a one-way street. 0: up, 1: right, 2: down, 3: left, +4 rotatable
      * @param i
     */
    public void makeOneWay(int i) {
        LevelObject lo = new LevelObject(level,x,y,Entities.holes_0.getzIndex(),LevelObject.Types.OneWay);
        oneWayType = i;
        lo.setOneWayGfx(i);
        oneWay = lo;
    }

    public ArrayList<LevelObject> getCollectibles() {
        return collectibles;
    }

    public void makeChocolate() {
        LevelObject lo = new LevelObject(level,x,y,Entities.holes_0.getzIndex(),LevelObject.Types.Chocolate);
        lo.setGfx(Entities.pills_power_pill_chocolate);
        // add(lo), already done in new LevelObject;
    }

    public boolean isRockme() {
        return rockme;
    }

    public LevelBlock up() {
        return level.getUp(x, y);
    }

    public LevelBlock up2() {
        return level.getUp2(x, y);
    }

    public LevelBlock right() {
        return level.getRight(x, y);
    }

    public LevelBlock right2() {
        return level.getRight2(x, y);
    }

    public LevelBlock down() {
        return level.getDown(x, y);
    }

    public LevelBlock down2() {
        return level.getDown2(x, y);
    }

    public LevelBlock left() {
        return level.getLeft(x, y);
    }

    public LevelBlock left2() {
        return level.getLeft2(x, y);
    }

    public void toggleDoor() {
        if( hasClosedDoor() ) {
            if(door.getDoorType() == LevelObject.DoorTypes.HorizontalClosed)
                door.setDoorType(LevelObject.DoorTypes.HorizontalOpened);
            else door.setDoorType(LevelObject.DoorTypes.VerticalOpened);
        }
        else {
            if(door.getDoorType() == LevelObject.DoorTypes.HorizontalOpened)
                door.setDoorType(LevelObject.DoorTypes.HorizontalClosed);
            else door.setDoorType(LevelObject.DoorTypes.VerticalClosed);
        }
        updateDoor();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getVX() {
        return x << PlayWindow.virtualBlockResolutionExponent;
    }

    public int getVY() {
        return y << PlayWindow.virtualBlockResolutionExponent;
    }

    public LevelObject getWall() {
        return wall;
    }

    public void setWall(LevelObject wall) {
        this.wall = wall;
    }

    public int getWallIndex() {
        return wallIndex;
    }

    public LevelObject getDoor() {
        return door;
    }

    public void setDoor(LevelObject door) {
        this.door = door;
    }

    public boolean hasHole() {
        return hole != null;
    }

    public LevelObject getHole() {
        return hole;
    }

    public Level getLevel() {
        return level;
    }

    public boolean hasOneWay() {
        return oneWayType != -1;
    }

    public boolean turnOneWay(Audio audio) {
        if(oneWayType >=4) {
            oneWayType++;
            if(oneWayType >=8) oneWayType = 4;
            oneWay.setOneWayGfx(oneWayType);
            audio.soundPlay("hihat");
            return true;
        }
        return false;
    }

    public int getOneWayDir() {
//        return oneWayDirMap[(oneWayType%4)+1];
        return (oneWayType%4)+1;
    }

    public boolean hasGhost() {
        for( LevelObject m: movables ) {
            LevelObject.Types t = m.getType();
            if(t.ordinal() >= LevelObject.Types.Ghost1.ordinal() &&
                t.ordinal() <= LevelObject.Types.Ghost4.ordinal()) return true;
        }

        return false;
    }



}
