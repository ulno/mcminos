====================================
Project McMinos: bugs, issues, ideas
====================================

:Authors:
  Andreas Neudecker <zapyon@gmx.net>,
  Ulrich Norbisrath <ulno@ulno.net>

:Date: 2015-10-10



2015-10-08 — Andreas

[ ] Level 0: McMinos cannot walk accross destroyed walls.

[ ] Level 0: clicking ahead of McMinos at the bottom of the screen to make him walk down, McMinos will walk down, shifting the level accordingly. But while the mouse pointer stays at the bottom edge of the window/clipping area, McMinos will stop walking once he has arrived at the level field that I originally clicked on. -- After my opinion McMinos should keep walking in the direction of the mouse pointer.

[ ] Level 0: Ghosts walk beneath McMinos when they kill him. Maybe we should rethink the z-indexing of ghosts and McMinos? I suggest:

  * McMinos:
    default: 750
    doped:   790
  * ghosts:  775

This way, McMinos is beneath ghosts, unless he has had power pill or similar power-up. Then, while hunting ghosts, he will be on top.


2015-10-10 — Andreas

[ ] use cheering McMinos for level end

[ ] replace current holes with new variant with shading/visible scarps

[ ] new animation of McMinos falling into warp hole! Animations of ghosts falling in shall follow soon

[ ] do we have a distinction between cycling and non-cycling animations? If so, we should add a switch to the CONFIG definition. If not, I would like to introduce them. This would allow new kinds of animations.

[ ] CONFIG for animation images: In Python, the 'order' sequence (actually a Python list) may stretch over several lines. Is that still allowed, or do we used the CONFIG files anywhere else where that might cause problems? Using multiple lines allows me to group stuff by sub-animations (check mcminos-default-front-new's CONFIG, for example)

[ ]
