package com.mcminos.game;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ulno on 25.09.15.
 */
public class EventManager implements KryoSerializable {
    long lastFrame = -1;
    long nowFrame = 0;
    ArrayList<Task> tasks = new ArrayList<>();
    private Game game;
    private McMinos mcminos;
    private Audio audio;
    private Level level;
    private Ghosts ghosts;


    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output,lastFrame);
        kryo.writeObject(output,nowFrame);
        kryo.writeObject(output,tasks);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        lastFrame = kryo.readObject(input,Long.class);
        nowFrame = kryo.readObject(input,Long.class);
        tasks = kryo.readObject(input,ArrayList.class);
    }

    public void init(Game game) {
        this.game = game;
        mcminos = game.getMcMinos();
        audio = game.getAudio();
        level = game.getLevel();
        ghosts = game.getGhosts();
    }

    public void initAfterKryoLoad(Game game) {
        init(game);
        for(int i=tasks.size()-1; i>=0; i-- ) {
            Task t = tasks.get(i);
            if(t.animation != null) {
                t.animation.initAfterKryoLoad(game);
                level.addToAllLevelObjects(t.animation);
            }
        }
    }

    public static enum Types {FuseDynamite, FuseBomb, ExplosionLight, ExplosionHeavy, Death, Win, DeathRestart, Fall}

    /**
     * for kryo-read
     */
    EventManager() {

    }


    /**
     * @param game
     * @param event to schedule
     * @param center the levelblock this animation belongs to
     * @param posX where to place the animation (virtual X coordinate)
     * @param posY where to place the animation (virtual Y coordinate)
     */
    public void schedule(Game game, Types event, LevelBlock center, int posX, int posY) {
        LevelObject lo = null;
        int animationLength = 0;
        switch( event ) {
            case FuseBomb:
                lo = new LevelObject(center, Entities.extras_bomb_fused, LevelObject.Types.BombFused);
                animationLength = Entities.extras_bomb_fused.getAnimationFramesLength();
                break;
            case FuseDynamite:
                lo = new LevelObject(center, Entities.extras_dynamite_fused, LevelObject.Types.DynamiteFused);
                animationLength = Entities.extras_dynamite_fused.getAnimationFramesLength();
                break;
            case ExplosionLight:
                lo = new LevelObject(center, Entities.extras_bomb_exploding, LevelObject.Types.BombExplosion);
                animationLength  = Entities.extras_bomb_exploding.getAnimationFramesLength();
                game.getAudio().soundPlay("explosio");
                destroyLiving(center);
                break;
            case ExplosionHeavy:
                lo = new LevelObject(center, Entities.extras_bomb_exploding, LevelObject.Types.BombExplosion);
                animationLength = Entities.extras_bomb_exploding.getAnimationFramesLength();
                game.getAudio().soundPlay("explosio");
                destroyLiving(center);
                destroyWalls(center);
                break;
            case Death:
                lo = new LevelObject(center, Entities.mcminos_dying, LevelObject.Types.McMinosDying);
                animationLength = Entities.mcminos_dying.getAnimationFramesLength();
                lo.setXY(posX,posY);
                break;
            case DeathRestart:
                lo = new LevelObject(center, Entities.mcminos_dying, LevelObject.Types.McMinosDying);
                animationLength = Entities.mcminos_dying.getAnimationFramesLength();
                lo.setXY(posX,posY);
                break;
            case Fall:
                lo = new LevelObject(center, Entities.mcminos_frightened, LevelObject.Types.McMinosFalling);
                animationLength = Entities.mcminos_frightened.getAnimationFramesLength();
                lo.setXY(posX,posY);
                break;
            case Win:
                lo = new LevelObject(center, Entities.mcminos_cheering, LevelObject.Types.McMinosWinning);
                animationLength = Entities.mcminos_cheering.getAnimationFramesLength();
                lo.setXY(posX,posY);
                break;
        }
        Task task = new Task(event,lo);
        task.scheduleFrame = nowFrame + animationLength; // set search-key
        int index = Collections.binarySearch(tasks, task); // make sure it's sorted TODO: here could be a race
        if(index<0)
            index = -index - 1;
        tasks.add(index, task);
        lo.animationStartNow(game);
    }

    public void update() {
        long timerFrame = game.getTimerFrame(); // just use tiemrframes not animationframes, as this here is not about images
        nowFrame = timerFrame;
        while (tasks.size() > 0 && tasks.get(0).scheduleFrame <= nowFrame) {
            Task t = tasks.get(0);
            tasks.remove(0);
            executeAfterAnimation(t); // this can create new tasks, so make sure this task has been removed from list before
        }
        lastFrame = timerFrame;
    }

    private void executeAfterAnimation(Task t) {
        LevelObject animation = t.animation;
        McMinos mcminos = game.getMcMinos();
        Level level = game.getLevel();
        LevelBlock center = animation.getLevelBlock();
        animation.dispose();
        switch( t.type ) {
            case FuseBomb:
                game.schedule(Types.ExplosionLight,center);
                break;
            case FuseDynamite:
                game.schedule(Types.ExplosionHeavy,center);
                break;
            case ExplosionLight:
                break;
            case ExplosionHeavy:
                break;
            case Death:
                mcminos.executeDeath(false);
                break;
            case DeathRestart:
                mcminos.executeDeath(true);
                break;
            case Fall:
                mcminos.executeFall();
                break;
            case Win:
                level.finish();
                break;
        }
    }

    static public class Task implements Comparable<Task> {
        Types type;
        long scheduleFrame=-1; // The frame where this should be executed
        LevelObject animation;

        /**
         * for kryo-read
         */
        Task () {

        }

        public Task(Types event, LevelObject lo) {
            animation = lo;
            type = event;
        }

        @Override
        public int compareTo(Task task) {
            long delta = (scheduleFrame - task.scheduleFrame);
            if(delta != 0) delta /= Math.abs(delta);
            return (int)delta;
        }

        public void dispose() {
            animation.dispose();
        }
    }

    /**
     * Get all neighboring blocks affected by an explosion
     * @return
     */
    private LevelBlock[] computeArea(LevelBlock center) {
        LevelBlock[] area = new LevelBlock[9];
        area[1] = center.up();
        area[0] = area[1].left();
        area[2] = area[1].right();
        area[4] = center;
        area[3] = center.left();
        area[5] = center.right();
        area[7] = center.down();
        area[6] = area[7].left();
        area[8] = area[7].right();
        return area;
    }

    private Graphics[] destroyedWalls = {
            Entities.walls_destroyed_00,
            Entities.walls_destroyed_01,
            Entities.walls_destroyed_02,
            Entities.walls_destroyed_03,
            Entities.walls_destroyed_04,
            Entities.walls_destroyed_05,
            Entities.walls_destroyed_06,
            Entities.walls_destroyed_07,
            Entities.walls_destroyed_08,
            Entities.walls_destroyed_09,
            Entities.walls_destroyed_10,
            Entities.walls_destroyed_11,
            Entities.walls_destroyed_12,
            Entities.walls_destroyed_13,
            Entities.walls_destroyed_14,
            Entities.walls_destroyed_15
    };
    /**
     * remove all walls (also invisible ones), doors, and rocks affected by explosion
     */
    private void destroyWalls(LevelBlock center) {
        LevelBlock[] area = computeArea(center);
        for( int i=8; i>=0; i-- ) {
            LevelBlock lb = area[i];
            if( lb.hasWall() && ! lb.getWall().isIndestructable()) {
                LevelObject w = lb.getWall();
                int wIndex = lb.getWallIndex();
                lb.setWall(null);
                w.dispose();
                if(wIndex >= 0) {
                    LevelObject destroyedWall = new LevelObject(lb,destroyedWalls[wIndex], LevelObject.Types.DestroyedWall);
                }
            } else if (lb.hasDoor()) {
                Graphics g;
                LevelObject d = lb.getDoor();
                d.dispose();
                lb.setDoor(null);
                if(d.getDoorType() == LevelObject.DoorTypes.HorizontalOpened || d.getDoorType() == LevelObject.DoorTypes.HorizontalClosed ) {
                    g = Entities.walls_door_destroyed_horizontal;
                } else {
                    g = Entities.walls_door_destroyed_vertical;
                }
                new LevelObject(lb,g, LevelObject.Types.Unspecified);
            } else if (lb.hasRock()) {
                if(lb.isRockme()) level.increaseRockmes();
                LevelObject r = lb.getRock();
                new LevelObject(lb,Entities.extras_rock_destroyed,LevelObject.Types.Unspecified);
                //handled in remove lb.setRock(null);
                game.removeMover(r.getMover());
                lb.remove(r); // from global movables
                r.dispose();
            }
        }
    }

    /**
     * kill ghosts, mcminos, remove pills, light other explosives
     */
    private void destroyLiving(LevelBlock center) {
        LevelBlock[] area = computeArea(center);
        for( int ac=area.length-1; ac>=0; ac--) {
            LevelBlock lb = area[ac];
            if( lb.hasPill()) {
                lb.removePill();
                mcminos.increaseScore(1);
            }
            if(lb.hasHole()) {
                lb.getHole().increaseHole(audio);
            }
            if(lb.hasOneWay()) {
                lb.turnOneWay(audio);
            }
            // kill ghosts and mcminos
            ArrayList<LevelObject> list = lb.getMovables();
            for( int i=list.size()-1; i>=0; i--) {
                if(list.size() > i) {
                    LevelObject lo = list.get(i);
                    int ghostnr = lo.getGhostNr();
                    if (ghostnr != -1) { // This is a ghost
                        if (ghostnr == 3) { // jumping pill
                            level.decreasePills();
                            audio.soundPlay("knurps");
                        }
                        ghosts.decreaseGhosts(ghostnr);
                        game.removeMover(lo.getMover());
                        list.remove(i);
                        lo.dispose();
                        mcminos.increaseScore(30);
                    }
                    if (lo.getType() == LevelObject.Types.McMinos) {
                        mcminos.kill("skullkill", Entities.mcminos_dying,false);
                    }
                }
            }

            ArrayList<LevelObject> collectibles = lb.getCollectibles(); // cascade explosions
            for( int i = collectibles.size() - 1; i>=0; i-- ) {
                LevelObject lo = collectibles.get(i);
                switch(lo.getType()) {
                    case Bomb:
                        collectibles.remove(lo);
                        game.schedule(Types.FuseBomb,lo.getLevelBlock());
                        lo.dispose();
                        break;
                    case Dynamite:
                        collectibles.remove(lo);
                        game.schedule(Types.FuseDynamite,lo.getLevelBlock());
                        lo.dispose();
                        break;
                    case LandMine:
                    case LandMineActive:
                        collectibles.remove(lo);
                        game.schedule(EventManager.Types.ExplosionLight,lo.getLevelBlock());
                        lo.dispose();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    void disposeAllTasks() {
        for(int i=tasks.size()-1; i>=0; i-- ) {
            Task t = tasks.get(i);
            t.animation.dispose();
            tasks.remove(i);
        }
    }
}
