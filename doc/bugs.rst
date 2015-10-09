=========================
Project McMinos: bug list
=========================

---------------------
Non-development tasks
---------------------

:Authors:
  Andreas Neudecker <zapyon@gmx.net>,
  Ulrich Norbisrath <ulno@ulno.net>

:Date: 2015-10-08



2015-10-08

[ ] Level 0: McMinos cannot walk accross destroyed walls.

[ ] Level 0: clicking ahead of McMinos at the bottom of the screen to make him walk down, McMinos will walk down, shifting the level accordingly. But while the mouse pointer stays at the bottom edge of the window/clipping area, McMinos will stop walking once he has arrived at the level field that I originally clicked on. -- After my opinion McMinos should keep walking in the direction of the mouse pointer.

[ ] Level 0: Ghosts walk beneath McMinos when they kill him. Maybe we should rethink the z-indexing of ghosts and McMinos? I suggest:

  * McMinos:
    default: 750
    doped:   790
  * ghosts:  775

This way, McMinos is beneath ghosts, unless he has had power pill or similar power-up. Then, while hunting ghosts, he will be on top.

[ ]
