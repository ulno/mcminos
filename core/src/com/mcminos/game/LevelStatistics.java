package com.mcminos.game;

/**
 * Record statistics and acievements for on eLevel.
 * Created by ulno on 20.12.15.
 */
public class LevelStatistics {
    private boolean activated = false; // is this level playable
    // TODO: idea: save date, when managed first
    private long bestTime=-1; // best time needed

    public LevelStatistics() {
        this.activated = true; // activate, when created
    }

    public void update(long newtime ) {
        if(newtime < bestTime) {
            bestTime = newtime;
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public long getBestTime() {
        return bestTime;
    }
}
