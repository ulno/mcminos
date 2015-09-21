package com.mcminos.game;

import com.badlogic.gdx.utils.Timer;

/**
 * Created by ulno on 19.09.15.
 */
public class Explosion {
    private final LevelBlock center;
    private final LevelObject.Types type;
    private LevelObject fuseObject;
    private Timer.Task triggerExplosionTask;
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
        Root.soundPlay("explosio");
        // TODO: take care of destruction and removal
    }

    private void initFuse() {
        int animationLengthMS = 0;

        if(type == LevelObject.Types.Bomb) {
            fuseObject = new LevelObject(center, Entities.extras_bomb_fused, LevelObject.Types.BombFused);
            Root.soundPlay("zisch");
            animationLengthMS = Entities.extras_bomb_fused.getAnimationFrames();
        }
        else { // so it's dynamite
            fuseObject = new LevelObject(center, Entities.extras_dynamite_fused, LevelObject.Types.BombFused);
            Root.soundPlay("zisch");
            animationLengthMS = Entities.extras_dynamite_fused.getAnimationFrames();
        }
        // Start a timer to come back here, when fusing finished
        triggerExplosionTask = new Timer.Task() { // TODO: usemy own timer in mover
            @Override
            public void run() {
                fuseObject.dispose();
                initExplosion();
            }
        };
        Timer.schedule(triggerExplosionTask, (float)animationLengthMS/1000 );
    }
}
