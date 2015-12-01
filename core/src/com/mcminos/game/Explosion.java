package com.mcminos.game;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by ulno on 19.09.15.
 */

public class Explosion {
    private final LevelBlock center;
    private final LevelObject.Types type;
    private final Level level;
    private final Game game;
    private final Audio audio;
    private final Ghosts ghosts;
    private final McMinos mcminos;
    private LevelObject fuseObject;
    private FrameTimer.Task triggerExplosionTask, explosionFinishTask;
    private LevelObject explosionObject;

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
    
    public Explosion(LevelBlock center, LevelObject.Types type) {
        this.center = center;
        this.type = type;
        level = center.getLevel();
        game = level.getGame();
        audio = game.getAudio();
        ghosts = game.getGhosts();
        mcminos = game.getMcMinos();

        // decide if we need to show a fuse or directly explode
        if( type != LevelObject.Types.LandMine || type != LevelObject.Types.LandMine ) {
            // Initiate fuse
            initFuse();
        }
        else {
            initExplosion();
        }
    }

    private void initExplosion() {
        int animationLength;

        if(type == LevelObject.Types.Bomb) {
            explosionObject = new LevelObject(center, Entities.extras_bomb_exploding, LevelObject.Types.BombExplosion);
            audio.soundPlay("explosio");
            animationLength = Entities.extras_bomb_exploding.getAnimationFramesLength();
            destroyLiving();
        }
        else if (type == LevelObject.Types.Dynamite) {
            explosionObject = new LevelObject(center, Entities.extras_dynamite_exploding, LevelObject.Types.DynamiteExplosion);
            audio.soundPlay("explosio");
            animationLength = Entities.extras_dynamite_exploding.getAnimationFramesLength();
            destroyLiving();
            destroyWalls();
        }
        else { // land-mine
            explosionObject = new LevelObject(center, Entities.extras_dynamite_exploding, LevelObject.Types.LandMineExplosion);
            audio.soundPlay("explosio");
            animationLength = Entities.extras_land_mine_exploding.getAnimationFramesLength();
            destroyLiving();
        }
        explosionObject.animationStartNow(game);
        // Start a timer to come back here, when explosion finished
        explosionFinishTask = new FrameTimer.Task(explosionObject) {
            @Override
            public void run() {
                explosionObject.dispose(); // just finish
            }
        };
        // crater
        new LevelObject(center,Entities.walls_destroyed_crater, LevelObject.Types.Unspecified);
        game.schedule(explosionFinishTask, animationLength);
    }

    /**
     * remove all walls (also invisible ones), doors, and rocks affected by explosion
     */
    private void destroyWalls() {
        computeArea();
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
    private void destroyLiving() {
        computeArea();
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
                        mcminos.kill("skullkill", Entities.mcminos_dying);
                    }
                }
            }

            ArrayList<LevelObject> collectibles = lb.getCollectibles(); // cascade explosions
            for( int i = collectibles.size() - 1; i>=0; i-- ) {
                LevelObject lo = collectibles.get(i);
                switch(lo.getType()) {
                    case Bomb:
                        collectibles.remove(lo);
                        lo.dispose();
                        new Explosion(lb, LevelObject.Types.Bomb);
                        break;
                    case Dynamite:
                        collectibles.remove(lo);
                        lo.dispose();
                        new Explosion(lb, LevelObject.Types.Dynamite);
                        break;
                    case LandMine:
                    case LandMineActive:
                        collectibles.remove(lo);
                        lo.dispose();
                        new Explosion(lb, LevelObject.Types.LandMine);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Get all neighboring blocks affected by an explosion
     * @return
     */
    private LevelBlock[] area = new LevelBlock[9];
    private void computeArea() {
        area[1] = center.up();
        area[0] = area[1].left();
        area[2] = area[1].right();
        area[4] = center;
        area[3] = center.left();
        area[5] = center.right();
        area[7] = center.down();
        area[6] = area[7].left();
        area[8] = area[7].right();
    }

    private void initFuse() {
        int animationLength;

        if(type == LevelObject.Types.Bomb) {
            fuseObject = new LevelObject(center, Entities.extras_bomb_fused, LevelObject.Types.BombFused);
            fuseObject.animationStartNow(game);
            audio.soundPlay("zisch");
            animationLength = Entities.extras_bomb_fused.getAnimationFramesLength();
        }
        else { // so it's dynamite
            fuseObject = new LevelObject(center, Entities.extras_dynamite_fused, LevelObject.Types.BombFused);
            fuseObject.animationStartNow(game);
            audio.soundPlay("zisch");
            animationLength = Entities.extras_dynamite_fused.getAnimationFramesLength();
        }
        // Start a timer to come back here, when fusing finished
        triggerExplosionTask = new FrameTimer.Task(fuseObject) {
            @Override
            public void run() {
                fuseObject.dispose();
                initExplosion();
            }
        };
        game.schedule(triggerExplosionTask, animationLength );
    }
}
