====================================
Project McMinos: bugs, issues, ideas
====================================

:Authors:
  Andreas Neudecker <zapyon@gmx.net>,
  Ulrich Norbisrath <ulno@ulno.net>

:Date: 2015-10-10


Bugs and Issues
================================================================================

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

[x] use cheering McMinos for level end

[ ] replace current holes with new variant with shading/visible scarps

[ ] new animation of McMinos falling into warp hole! Animations of ghosts falling in shall follow soon

[ ] do we have a distinction between cycling and non-cycling animations? If so, we should add a switch to the CONFIG definition. If not, I would like to introduce them. This would allow new kinds of animations.

[ ] CONFIG for animation images: In Python, the 'order' sequence (actually a Python list) may stretch over several lines. Is that still allowed, or do we used the CONFIG files anywhere else where that might cause problems? Using multiple lines allows me to group stuff by sub-animations (check mcminos-default-front-new's CONFIG, for example)


2015-10-11 — Andreas

[ ] Level 1: I remember ghosts running over McMinos (and on) when killing McMinos. Now, ghost Hanky stops where he touched McMinos and McMinos is dead. I liked the old behaviour better. And perhaps even the malevolent laughter they used to laugh. Though the organ sound is also nice. Perhaps both can go together ... — Thinking about changing the animation for dying/dead McMinos. I like the current version, but think the change from live McMinos to dead is too instantaneous.

[ ] Level 1: McMinos cannot walk across destroyed walls.

[ ] Any level: wouldn't it make sense to limit zooming out to the point where the whole level is visible at current screen size?

[ ] Level number: it would be good to have the level number shown somewhere in the level, at least for testing.

[ ] Level 2, desktop: it is impossible to cross the level border (to enter the other side again): clicking on the edge field will bring you just there. The only way to reach the other side is by zooming out until the complete level is visible and then clicking on the edge field on the other side. May be we can introduce an invisble function-field that pushes McMinos over the edge in desktop version? Like a special version of warp that has a specific corresponding target field?


2015-10-16 - Andreas

[ ] 2DO: When bomb, dynamite or mine explode, the field under the explosive should be decorated with a crater, using /extra/images/walls/destroyed/00/00.svg





Ideas, Brainstorming
================================================================================

GUI design
----------

* Toolbox
  * Extras should only be shown after they have been selected
  * Show options as Icons (tooltips optional)
    * Bomb, Dynamite, mine
      * show overlay ring with options "put down" or "ignite/dig in"
    * key
      * show (un)locking direction in the level or in an overlay ring with direction-buttons


Game navigation
---------------

* navigation using a method similar to a joystick might be a usable alternative
* opening (closing) the toolbox is currently possible via
  * touchscreen (and desktop): tapping/clicking the toolbox button
  * keyboard: pressing either the [Space] or the [Esc] key
  * with the mouse: right-clicking
* zooming in and out of the level
  * touchscreen: tapping + or - buttons in the toolbox
  * keyboard: + or - keys
  * mouse: scroll-wheel up or down to zoom in or out


Tutorial levels
---------------

* level size of 7 x 7 fields (using 64 pixel sprites this results in 448 x 448 pixel levels)
* before starting a level, display introductory text (incl. graphics where required)
* these introductions/explanations should be accessible always, e.g. from within the toolbox
* explain most important features one by one, one feature each level
* features to explain (in no specific order; should be ordered sensibly in the final version):

  * eat pills
  * avoid ghosts
  * use power cookies do get (temporarily) rid of ghosts
  * hunt jumping pills (unless you have eaten a cookie)
  * use keys and doors
  * use rocks and rock-me fields
  * use explosives
  * avoid holes
  * collect bonuses
  *
