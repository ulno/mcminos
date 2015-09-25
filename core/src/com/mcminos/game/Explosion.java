package com.mcminos.game;

/**
 * Created by ulno on 19.09.15.
 */
public class Explosion {
    private final LevelBlock center;
    private final LevelObject.Types type;
    private LevelObject fuseObject;
    private FrameTimer.Task triggerExplosionTask;
    private LevelObject explosionObject;

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
        explosionObject = new LevelObject(center, Entities.extras_bomb_exploding, LevelObject.Types.BombFused);
        explosionObject.animationStartNow();
        Root.soundPlay("explosio");
        // TODO: take care of destruction and removal
    }

    private void initFuse() {
        int animationLengthMS = 0;

        if(type == LevelObject.Types.Bomb) {
            fuseObject = new LevelObject(center, Entities.extras_bomb_fused, LevelObject.Types.BombFused);
            Root.soundPlay("zisch");
            animationLengthMS = Entities.extras_bomb_fused.getAnimationFramesLength();
        }
        else { // so it's dynamite
            fuseObject = new LevelObject(center, Entities.extras_dynamite_fused, LevelObject.Types.BombFused);
            fuseObject.animationStartNow();
            Root.soundPlay("zisch");
            animationLengthMS = Entities.extras_dynamite_fused.getAnimationFramesLength();
        }
        // Start a timer to come back here, when fusing finished
        triggerExplosionTask = new FrameTimer.Task() {
            @Override
            public void run() {
                fuseObject.dispose();
                initExplosion();
            }
        };
        Root.frameTimer.schedule(triggerExplosionTask, animationLengthMS );
    }
}
