=======================================
Project McMinos: List of level elements
=======================================

:Authors:
  Andreas Neudecker <zapyon@gmx.net>

:Date: 2015-09-08


Alive
=====

* McMinos

  * default (still, left, down, right, up)
  * power(ed) (still, left, down, right, up)
  * poisoned (still)
  * drunken (still = moving?)
  * cloaked (still = moving)

* Ghosts

  * Ghost 1, Hanky
  * Ghost 2, Panky
  * Ghost 3, Zarathustra
  * Ghost 4, JumpingPill
  * Ghost 5 (?)

Inanimate
=========

* Wall

  * default, 16 types (see graphics)
  * InvisibleWall
  * IndestructibleWall -- TODO: select existing sketch or redraw
  * Door -- TODO: fix problem with indestructable walls
  * Castle
  * Gravestone -- TODO: redraw, possibly as a cross again (see ATARI version)
  * Hole -- TODO: draw graphics for areas of connected holes
  * Field

    + SpeedUp
    + SpeedDown
    + Arrow
    + RevolvingArrow
    + DeathField
        (i.e. skull field)
    + WarpHole -- TODO: try different animation
    + Rock
    + RockTargetField -- TODO: agree upon selection among sketches

Item
====

* Bonus -- TODO: piggy bank exists, add others according to ATARI version
* Cloak (disguises McMinos as ghost)
* Clock (increases available time in time-limited levels) -- TODO: clock + animations
* Explosive

  * Bomb (default, fused, explosion)
  * Dynamite (default, fused, explosion)
  * Mine (default, active)

* FlyingTool (was umbrella on Atari, may now be cloud) -- TODO: pick umbrella or cloud
* Letter (provides level code for direct access to level) -- TODO: do we still need this?
* Life = heart
* Mirror
* NextLevelTool (was ladder on Atari, may be different item now) -- TODO: draw
* Skull
* Surprise -- TODO: do we still need this? If so, draw nicely wrapped present or similar
* Edible

  * Pill
  * Medicine
  * Poison
  * PowerPill
  * Whiskey -- TODO: draw

Background
==========

* diverse background tiles -- TODO: select usable background among existing sketches, draw new ones.

