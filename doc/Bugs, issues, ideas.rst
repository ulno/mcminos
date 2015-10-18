====================================
Project McMinos: bugs, issues, ideas
====================================

:Authors:
  Andreas Neudecker <andreas.mcminos@googlemail.com>,
  Ulrich Norbisrath <ulno@ulno.net>

:Date: 2015-10-10


Keywords: 2DO, BUG, IDEA, ISSUE (implies discussion)


Bugs and Issues
================================================================================

2015-10-08 — Andreas

[ ] BUG Level 0: McMinos cannot walk accross destroyed walls.

[ ] Level 0: clicking ahead of McMinos at the bottom of the screen to make him walk down, McMinos will walk down, shifting the level accordingly. But while the mouse pointer stays at the bottom edge of the window/clipping area, McMinos will stop walking once he has arrived at the level field that I originally clicked on. -- After my opinion McMinos should keep walking in the direction of the mouse pointer.

[ ] BUG Level 0: Ghosts walk beneath McMinos when they kill him. Maybe we should rethink the z-indexing of ghosts and McMinos? I suggest:

  * McMinos:
    default: 750
    doped:   790
  * ghosts:  775

This way, McMinos is beneath ghosts, unless he has had power pill or similar power-up. Then, while hunting ghosts, he will be on top.


2015-10-10 — Andreas

[ ] IDEA|2DO: Do we have a distinction between cycling and non-cycling animations? If so, we should add a switch to the CONFIG definition. If not, I would like to introduce them. This would allow new kinds of animations.


2015-10-11 — Andreas

[ ] ISSUE: Level 1: I remember ghosts running over McMinos (and on) when killing McMinos. Now, ghost Hanky stops where he touched McMinos and McMinos is dead. I liked the old behaviour better. And perhaps even the malevolent laughter they used to laugh. Though the organ sound is also nice. Perhaps both can go together ... — Thinking about changing the animation for dying/dead McMinos. I like the current version, but think the change from live McMinos to dead is too instantaneous.

[ ] IDEA: Any level: wouldn't it make sense to limit zooming out to the point where the whole level is visible at current screen size?

[ ] IDEA: Level number: it would be good to have the level number shown somewhere in the level, at least for testing.


2015-10-16 - Andreas

[ ] 2DO: When bomb, dynamite or mine explode, the field under the explosive should be decorated with a crater, using /extra/images/walls/destroyed/00/00.svg

2015-10-17 - Andreas

[ ] 2DO: I just realise, that the current way of walking across level borders is totally different from what we were having in the Atari version: Due to the centering of McMinos, the level would simply scroll on to show (e.g.) the left side of the level when closing in on the right edge of the level, so that the level looks like it is infinite - or is that due to the SCROLL settings of the level?

[ ] IDEA|2DO: Having an option to load levels from a (pre-defined?) directory outside the jar would make testing+editing levels a lot easier and faster. I suggest that the program checks for existence of <position-of-the-jar>/test-levels and, if this folder exists, loads list and levels from there instead from the internal data.




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

  * [x] eat pills
  * [x] avoid ghosts
  * [x] use power pills do get (temporarily) rid of ghosts
  * [x] hunt jumping pills (unless you have eaten a cookie)
  * [x] use keys and doors
  * [x] use rocks and rock-me fields
  * [ ] use explosives
    * [x] bomb
    * [ ] dynamite
    * [ ] landmine
  * [x] avoid holes
  * ~~[ ] collect bonuses~~
  *
