package com.mcminos.game;

/**
 * Created by ulno on 04.09.15.
 */
public interface MoverDirectionChooser {
    Mover.directions[] chooseDirection(LevelObject lo);
}
