package com.mcminos.game;

import java.util.HashSet;

/**
 * Created by ulno on 19.09.15.
 */
public class Explosion {
    private final LevelBlock center;
    private final LevelObject.Types type;
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
            Root.soundPlay("explosio");
            animationLength = Entities.extras_bomb_exploding.getAnimationFramesLength();
            destroyLiving();
        }
        else if (type == LevelObject.Types.Dynamite) {
            explosionObject = new LevelObject(center, Entities.extras_dynamite_exploding, LevelObject.Types.DynamiteExplosion);
            Root.soundPlay("explosio");
            animationLength = Entities.extras_dynamite_exploding.getAnimationFramesLength();
            destroyLiving();
            destroyWalls();
        }
        else { // land-mine
            explosionObject = new LevelObject(center, Entities.extras_dynamite_exploding, LevelObject.Types.LandMineExplosion);
            Root.soundPlay("explosio");
            animationLength = Entities.extras_land_mine_exploding.getAnimationFramesLength();
            destroyLiving();
        }
        explosionObject.animationStartNow();
        // Start a timer to come back here, when explosion finished
        explosionFinishTask = new FrameTimer.Task() {
            @Override
            public void run() {
                explosionObject.dispose(); // just finish
            }
        };
        Root.frameTimer.schedule(explosionFinishTask, animationLength);
    }

    /**
     * remove all walls (also invisible ones), doors, and rocks affected by explosion
     */
    private void destroyWalls() {
        for( LevelBlock lb: getArea() ) {
            if( lb.hasWall() ) {
                LevelObject w = lb.getWall();
                int wIndex = lb.getWallIndex();
                lb.setWall(null);
                w.dispose();
                if(wIndex >= 0) {
                    LevelObject destroyedWall = new LevelObject(lb,destroyedWalls[wIndex], LevelObject.Types.DestroyedWall);
                }
            } else if (lb.hasDoor()) {
                LevelObject d = lb.getDoor();
                // TODO: add destroyed door
                d.dispose();
                lb.setDoor(null);
            } else if (lb.hasRock()) {
                LevelObject r = lb.getRock();
                // TODO: add destroyed rock
                r.dispose();
                lb.setRock(null);
            }
        }
    }

    /**
     * kill ghosts, mcminos, remove pills, light other explosives
     */
    private void destroyLiving() {
        for( LevelBlock lb: getArea() ) {
            if( lb.hasPill()) {
                lb.removePill();
                Root.increaseScore(1);
            }
            HashSet<LevelObject> collectibles = lb.getCollectibles();
            for( LevelObject lo: collectibles) {
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
    private LevelBlock[] getArea() {
        LevelBlock u = center.up();
        LevelBlock d = center.down();
        return new LevelBlock[]{
                u.left(),u,u.right(),
                center.left(),center,center.right(),
                d.left(),d,d.right()
        };
    }

    private void initFuse() {
        int animationLength;

        if(type == LevelObject.Types.Bomb) {
            fuseObject = new LevelObject(center, Entities.extras_bomb_fused, LevelObject.Types.BombFused);
            Root.soundPlay("zisch");
            animationLength = Entities.extras_bomb_fused.getAnimationFramesLength();
        }
        else { // so it's dynamite
            fuseObject = new LevelObject(center, Entities.extras_dynamite_fused, LevelObject.Types.BombFused);
            fuseObject.animationStartNow();
            Root.soundPlay("zisch");
            animationLength = Entities.extras_dynamite_fused.getAnimationFramesLength();
        }
        // Start a timer to come back here, when fusing finished
        triggerExplosionTask = new FrameTimer.Task() {
            @Override
            public void run() {
                fuseObject.dispose();
                initExplosion();
            }
        };
        Root.frameTimer.schedule(triggerExplosionTask, animationLength );
    }
}
