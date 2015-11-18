====================================
Project McMinos: bugs, issues, ideas
====================================

:Authors:
  Andreas Neudecker <andreas.mcminos@googlemail.com>,
  Ulrich Norbisrath <ulno@ulno.net>

:Date: 2015-10-23


Keywords: 2DO, BUG, IDEA, ISSUE (implies discussion), TESTING


Bugs and Issues
================================================================================

2015-10-08 — Andreas

[ ] BUG Level 0: Ghosts walk beneath McMinos when they kill him. Maybe we should rethink the z-indexing of ghosts and McMinos? I suggest:

  * McMinos:
    default: 750
    doped:   790
  * ghosts:  775

This way, McMinos is beneath ghosts, unless he has had power pill or similar power-up. Then, while hunting ghosts, he will be on top.


2015-10-10 — Andreas

[ ] IDEA, 2DO: Do we have a distinction between cycling and non-cycling animations? If so, we should add a switch to the CONFIG definition. If not, I would like to introduce them. This would allow new kinds of animations.


2015-10-11 — Andreas

[ ] ISSUE: Level 1: I remember ghosts running over McMinos (and on) when killing McMinos. Now, ghost Hanky stops where he touched McMinos and McMinos is dead. I liked the old behaviour better. And perhaps even the malevolent laughter they used to laugh. Though the organ sound is also nice. Perhaps both can go together ... — Thinking about changing the animation for dying/dead McMinos. I like the current version, but think the change from live McMinos to dead is too instantaneous.

[ ] IDEA: Any level: wouldn't it make sense to limit zooming out to the point where the whole level is visible at current screen size?

[ ] IDEA: Level number: it would be good to have the level number shown somewhere in the level, at least for testing.


2015-10-17 - Andreas

[ ] IDEA, 2DO: Having an option to load levels from a (pre-defined?) directory outside the jar would make testing+editing levels a lot easier and faster. I suggest that the program checks for existence of <position-of-the-jar>/test-levels and, if this folder exists, loads list and levels from there instead from the internal data.

2015-10-18

[ ] BUG: Level tut0n-*: Player always has 3 lives only at level start, even though LIVE: 999,999 (i.e. min-lives = 999) in all tutorial levels. -- UPDATE: It seems that only the display in the level suggest there are only 3 lives. The player can die multiple times without losing.

[ ] ISSUE: Do we still need the differenciation between NUMBR and SHOWNR in the level sources? I suggest we drop SHOWNR as the level list decides about the position. — 2015-11-16: May be we can even drop NUMBR?

[ ] ISSUE, 2DO: For testing purposes we need the version of the program visible inside the game, in a prominent place, if possible. When alpha and beta testers test, they may not always have the most recent version and we cannot know whithout them telling us.

[ ] IMPORTANT BUG: Level tut08-holes: while the level is easy to do, the pill at the bottom right needs to be eaten last. Otherwise the player falls into the hole next to it. The level source defines RSTRT: 1 which should make the whole level restart after the player falls into a hole. But the level stays in the state it was right before the players demise. This makes the level impossible to solve after the player fell into one hole.

[ ] BUG: Level 6: McMinos finished the level by eating the last jumping pill. And is killed by a ghost while already cheering. Ghosts should stop running once McMinos wins.

[ ] 2DO: At the end of the player falling into a hole there should be some clank & rattle (Scheppern). Or is the hole bottomless?

[ ] ISSUE, 2DO: I don't like the current player's death animation very much any more. It's not bad, but I feel it is missing something and may be too short. Any ideas?


2015-10-19 - Andreas

[ ] IDEA, TESTING: The following features would greatly facilitate testing and/or preparation of the release:

* program version visible inside the program

* screenshot functionality inside the program

* recording playing a level (for testing and demo use)

[ ] 2DO: Level tut11-dynamite: This level has an outer wall made of indestructible walls. Inside are a couple of default walls. Where the default walls "touch" the indestructible ones, they are sort of connected. In the Atari version we had them separated by end pieces, i.e.

  ZXXX

would lead to a sequence of <indestructible wall>,<default wall 02>,<default wall 10>,<default wall 8>, while currently this becomes:
<indestructible wall>,_<default wall 10>_,<default wall 10>,<default wall 8>


2015-10-20 - Andreas

[ ] BUG: Level testing, tut17-inifinite-world: In a level with infinite world, when McMinos finishes the level on an edge field (have seen this on the right edge), McMinos has sort of 2½ arms on the left side.

[ ] BUG: At typo in the level name causes a crash immediately once the level is started in the program. It would be nicer if the program simply returned to the main menu.

2015-10-22 - Andreas

[ ] BUG: Level tut17-infinite-world: SCROLLX and SCROLLY are on. If the player is in a position where the castle is visible at the bottom accross the level edges, but only the two top field quarters of the castle should be visible, the castle is not drawn at all. If a little more than the two top fields are shown, the castle is visible. — Addendum: this only happens at maximum size of the fields (128x128) in a 1280x878 pixel window.

  UPDATE (2015-10-23): The same happens with background pavement-01 (now a 3x3 field object with corresponding CONFIG), level tut17-infinite-world: the bottom row of fields should show the upper third of the background. It shows the frame background (currently pavement-04) instead.

[ ] ISSUE: (sample) level: tut06-jumping-pills: the jpills are too stupid: simply waiting in a corner or closing the door and waiting in the middle of the level is enough to have all jpills jump in your mouth - unless they have all done that already before you managed to close the door. It would be great if the jpills could be set to a difficulty where they can also turn in their own path to evade the player.

2015-10-24 - Andreas

[ ] ISSUE: I think we need some visual and/or acoustic feedback that announces the end of effect of power pills, may be for poison, too. Would it be much work for you to integrate a separate animation for, let's say, the last 1 to 3 seconds of effect time? For the power pills I could remove the stars from McMinos and flip colours between orange and red.


2015-10-27 Phone talk Andreas + Uli

* UI possibly done without skins, similar to Atari version: icons + text, where required
* LEVEL SOURCES: no extras are transferred to the next level; all tools will be converted to extra points. Exception: extra lives will be transferred. Corresponding variables in the level sources are deprecated and will be ignored. Levels need to be adapted accordingly (i.e.: provide required extras at level start - inside the level)


2015-11-16 - Andreas

[ ] IDEA: desktop, level screen, pause mode: the cursor keys should allow navigation in the level in view mode.

[ ] ISSUE: level screen, minimap: I would like to get rid of the yellow frame. I think it is often misleading as it wraps around the edges - or worse: the corners - of the minimap. Now, I thought, it might be nicer, to automatically scroll the minimap just the same as the big level, so that McMinos is usally in the center of the minimap, too. This might also let us get rid of double walls on the edges as the wrap-around levels would than REALLY be infinite world style and it doesn't matter where the actual level edges are. Optionally, McMinos might blink in the minimap, as he did on Atari (right?)

[ ] ISSUE: Currently the level is centered on the level screen. Due to this, there is often a part of the level covered by the minimap, even though there is a lot of free space on the left, between the toolbox and the level, that could be used for displaying the level. — Oh, the same is valid for vertical positioning. I.e.: I consider it best, if the there is a sensible way not to cover the level with the minimap, not to do it.

[ ] ISSUE: Showing the level number inside the level for testing would be very helpful, not only for testers, but, too, for my screenshots for level list thumbnails, as this would be the only useful way I could see the level number corresponding to a screenshot right away without searching and guessing.

[ ] BUG: Walls next to invisible walls should not be drawn as connected to those as they give away the invisble walls. And it looks awful, too.


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
    * [x] dynamite
    * [ ] landmine
  * [x] avoid holes
  * ~~[ ] collect bonuses~~
  * [x] poison and medicine (may be it should be renamed antidote)
  * [x] whisky - done, but not yet implemented
  * [X] oneways
