<!DOCTYPE html>
<html><head>
<meta charset=utf-8" />
<title>McMinos level source reference</title>
<style type="text/css">body{padding:1em 5%;} table,th,td,tr{border:1px solid black; padding: .25em .5em; border-collapse:collapse;} pre,code{font-family:DejaVu Sans Mono, Droid Mono, Inconsolata,monospace;} pre{padding:.5em 1.5em;} table{width: 100%;}</style>
</head><body>

McMinos level source reference
==============================

**for McMinos versions 0.815, 1.10, 2.00, and mobile**


Author: Andreas Neudecker

Created: 1995-01-11


McMinos level sources are simple text files. They used to be in ASCII (Atari flavour) and are now in UTF-8. The general level structure is very simple:

Level parameters
----------------

All settings and data in the level source are given as simple parameter and value pairs. There are two kinds of parameters: 

1. single line parameters
2. multi line

Single-line parameters have the form 

```
PARAMETER: data
```

while multi-line parameters have the form

```
PARAMETER:
data
#
```

where the '#' signals the end of a multi-line parameter's data.

Comments start with ';' and stretch to the end of the line


### Header

parameter    | description
------------ | -----------------------------------------------------------------
`AUTHOR`     | level author; space not allowed; use "\_" instead (optional)
`NUMBR`      | number of the level — *currently unused*
~~`SHOWNR`~~ | ~~number of the level as displayed~~ — **deprecated**
~~`ACCCD`~~  | ~~level code (0 = no direct access)~~ — **deprecated**

**Example:**

```
AUTHOR: Andreas_Neudecker
NUMBR:  199
```

### Level story

The parameters in this section are new in McMinos mobile.

parameter    | description
------------ | -----------------------------------------------------------------
`SYMBOL`     | grafic entity; e.g.: ghosts_hanky,5 (name, animation image no.)
`TITLE`      | level title to be displayed in set menu and before level start
`TITLE-de`   | German language level title
`BODY`       | multi-line text, displayed before level start and in level menu
`#`          | obligatory end symbol for multi-line `BODY` text
`BODY-de`    | German language level text
`#`          | obligatory end symbol for multiline `BODY-de` text

**Example:**

```
SYMBOL:   level_symbol_castle
TITLE:    One ghost, one cookie
TITLE-de: Ein Geist, ein Keks
BODY:
Eat all peas. And stay alive.
#
BODY-de:
Iss alle Pillen. Und bleib am Leben.
#
```


### Level dimensions

parameter    | description
------------ | -----------------------------------------------------------------
`LWID`       | level width, measured in fields (max. 50)
`LHI`        | level height, measured in fields, too (max. 50)
`SCROLLX`    | horizontal wrap-around; 0 = off, 1 = on, default: 0
`SCROLLY`    | vertical wrap-around; 0 = off, 1 = on, default: 0

The paramters `VWID` and `VHI` that we used in the Atari version of McMinos to
define a clipping box to limit the visible level area have been dropped in
McMinos mobile, since they were only used in rare cases and did not provide any
improvement to the game. Additionally, the vast range of display sizes today
made this feature look useless.

**Example:**

```
LWID:    20
LHI:     20
SCROLLX: 0
SCROLLY: 0
```


### Level background

parameter    | description
------------ | -----------------------------------------------------------------
`BACK`       | background style (0 = black; 1, …, 5 = tiles; default = 1)

The Atari versions of McMinos used to have 50 different backround tiles
available. With McMinos mobile we decided to reduce the number of backgrounds
to only five (not counting black = no background). While the background can be
specified by using the image entitie's id (eg `backgrounds_pavement_01` for the
current default), it is strongly recommended to use their numbers only, as
these will work, even if image files are replaced.

number | background tile
------ | -----------------------------------------------------------------------
0      | ![backgrounds_black][black]
1      | ![backgrounds_pavement_01][pavement]
2      | ![backgrounds_gravel_01][gravel]
3      | ![backgrounds_meadow_flowers][meadow]
4      | ![backgrounds_sand_01][sand]
5      | ![backgrounds_soil_01][soil]

[black]: img/backgrounds_black_0.png "backgrounds_black"
[pavement]: img/backgrounds_pavement_01_0.png "backgrounds_pavement_01"
[gravel]: img/backgrounds_gravel_01_0.png "backgrounds_gravel_01"
[meadow]: img/backgrounds_meadow_flowers_0.png "backgrounds_meadow_flowers"
[sand]: img/backgrounds_sand_01_sand_0.png "backgrounds_sand_01"
[soil]: img/backgrounds_soil_01_0.png "backgrounds_soil_01"

Old levels may still have background numbers higher than 5. These will default to 1 (currently `backgrounds_pavement_01`)

**Example:**

```
BACK: 2
```


### Start-up settings

parameter    | description
------------ | -----------------------------------------------------------------
~~`LTIME`~~  | limited level solving time — *currently unused*
`RSTRT`      | restart mode after losing a life (details: see below)
`MIRROR`     | mirror player's movements; 1 = on, 0 = off (default)


#### Level restart mode

After the demise of McMinos a level may continue in different modes,
depending on the intentions of the level's author. Permitted values are:

mode         | description
------------ | -----------------------------------------------------------------
0            | ghosts and McMinos start in their original positions (default)
1            | level starts anew
2            | ghosts start from castle(s), McMinos from original position
4            | McMinos starts from where he died, ghosts from start positions
8            | bonus level -- when McMinos dies, level is finished
16           | McMinos starts at spot of death, ghosts where they are

*Mode 16 is currently not implemented (2016-01-05)*

**Example:**

```
LTIME:  0
RSTRT:  0
MIRROR: 0
```


### Creatures


#### McMinos

parameter   | description
----------- | ------------------------------------------------------------------
MCSPEED     | McMinos' base speed at level start (1, 2, 4); default = 1


#### Ghosts

-   There are four types of ghosts:
    1.  **Hanky** -- the white blanket ghost that hunts McMinos and kills him
    2.  **Perry** -- the *burger ghost* that lays new peas in the level,
        hunts McMinos and poisons him (curable with medicine)
    3.  **Zarathustra** -- the yellow ghost that can traverse walls (though
        not through indestructible walls, doors or rocks) and, of
        course, hunts and kills McMinos. Is killed by dynamite, but
        survives detonation of mines or bombs.
    4.  **Jumping Pea** -- this ghost is different: it flees from McMinos,
        unless McMinos has eaten a power cookie or similar, then it
        hunts McMinos to poison him (again, curable with medicine)
-   Most setting options are identical for all ghosts (count, speed,
    "agility", reappearance time); few are specific to one type of
    ghost.
-   Ghosts that are not in the level at level start will only appear out
    of castles. (GHOST*n* - default: 0; GRTIME*n* - default: 1 *s*)
-   "agility" -- this property describes the probability of errors a
    ghost makes in pursuit of McMinos: the higher the value the rarer a
    ghost makes mistakes; 0 means "never" (AGIL*n* - values: 0, …,
    32768)

parameter    | description
------------ | -----------------------------------------------------------------
**GHOST1**   | max. of **Hanky** ghosts appearing simultaneously (default: 0)
GHSPEED1     | speed of ghost Hanky (1, 2, 4, 8 - default: 1)
AGIL1        | ghost Hanky's probabilty of errors in pursuit of McMinos
GRTIME1      | seconds before a Hanky reappears (default: 1)
**GHOST2**   | max. of **Perry** gnosts appearing simultaneously (default: 0)
GHSPEED2     | speed of ghost Perry (1, 2, 4, 8 - default: 1)
AGIL2        | ghost Perry's probabilty of errors in pursuit of McMinos
GRTIME2      | seconds before a Perry reappears (default: 1)
PILLMAX2     | maximum number of peas Perry will lay in the level (default: 0)
PILLFREQ2    | randomly lay peas every PILLFREQ2-th field; 1: each (default)
**GHOST3**   | max. no. of **Zarathustra** appearing simultaneously (default: 0)
GHSPEED3     | speed of ghost Zarathustra (1, 2, 4, 8 - default: 1)
AGIL3        | ghost Zarathustra's probabilty of errors in pursuit of McMinos
GRTIME3      | seconds before a Zarathustra reappears (default: 1)
TRANSWALL3   | randmly traverse walls; probability is higher for small values
**GHOST4**   | max. no. of **jumping peas** appearing simultaneously (default: 1)
GHSPEED4     | speed of jumping peas (1, 2, 4, 8 - default: 1)
AGIL4        | jumping peas' probabilty of errors in pursuit of McMinos
GRTIME4      | seconds before a jumping pea reappears (default: 1)

```
MCSPEED:    1
GHOST1:     5
GHSPEED1:   1
AGIL1:      10
GRTIME1:    1
GHOST2:     1
GHSPEED2:   1
AGIL2:      10
GRTIME2:    1
PILLMAX2:   100
PILLFREQ2:  10
GHOST3:     0
GHSPEED3:   1
AGIL3:      10
GRTIME3:    1
TRANSWALL3: 10
GHOST4:     1
GHSPEED4:   1
AGIL4:      10
GRTIME4:    1
```


### Minimum and maximum amounts of items

Levels written for the Atari version of McMinos had a couple of min/max
settings for items like bombs, chocolate, medicine etc. These are now
deprecated and will be ignored. If a level requires certain items, please
provide them inside the level!


### Level data

The level data is a multi-line parameter (see above) and thus has the general
form of

```
LEVEL:
<level data>
#
```

while *level data* is a simple character representation of all items in the
level that, when viewed with a monospace font, may look just like the real
level to an experienced level author (even though a little distorted due to the
fact that most monospace characters do not cover exact squares).

The maximum size for a level is currently 50 by 50 squares, i.e. 50 lines with
50 colums, each.

#### Creatures

While the number of ghosts in a level is not limited except by the number of
free level available there must be **exactly one McMinos** in the level.

character       | description
--------------- | --------------------------------------------------------------
`P`             | player -- this is McMinos
`G`             | ghost 1 -- Hanky, the white ghost
`g`             | ghost 2 -- Perry, the pea layer
`H`             | ghost 3 -- Zarathustra, the ghost that can traverse walls
`h`             | ghost 4 -- jumping pea

#### The castle

A complete castle must always look like this:

```
CC
CC
```

The maximum count of castles is 64 per level!

character       | description
--------------- | --------------------------------------------------------------
`C`             | ghosts' castle; must always consist of a 2 by 2 group of Cs


#### Elements of the maze

This is a very diverse group of items. It comprises walls and doors, holes, one-ways, effect fields like speed-up, kill-all-ghosts, etc.

character       | description
--------------- | --------------------------------------------------------------
`X`             | wall
`Z`             | undestroyable wall
`U`             | invisable wall
`D`             | door, closed
`\|`            | door, opened
`F`             | speed-Up field
`f`             | speed-Down field
`W`             | warp hole
`A`             | Kill All Field
`^, >, v, <`    | one way fields; up, right, down, left, respectively
`ä, ö, ü, ß`    | rotating one ways; preset directions: up, right, down, left
`T`             | skull field
`S`             | skull
~~`?`~~         | ~~surprise field (positive or negative)~~ — **deprecated**
`r`             | rock
`O`             | rock me field
`0`             | rock me field with a stone on it
`6,…, 9, o`     | holes in the ground (6 = smallest, o = biggest)


#### Useful items

They take effect immediately when McMinos walks on them.

character       | description
--------------- | --------------------------------------------------------------
`.`             | pea (pellet)
`x`             | exit to next level
`a`             | kill all ghosts
**power pills** | *effective for 10 sec (each)*
  `*`           | cookie; multipliers: MCSPEED x2; GHSPEEDs x1
  `(`           | milk; multipliers: MCSPEED x1; GHSPEEDs x2
  `)`           | mushroom; multipliers: MCSPEED x1; GHSPEEDs x1
`L`             | Life (currently heart symbol)
~~`$`~~         | clock, Level time + 60 sec. — **deprecated**
~~`c`~~         | Letter with level code — **deprecated**
`1`             | ~~Bonus 100 Pt~~; currently replaced with decorative flower
`2`             | ~~Bonus 250 Pt~~; replaced with a decorative flower
`3`             | ~~Bonus 500 Pt~~; replaced with a decorative flower


#### Bad items

Just like the useful items above, these items take effect immediately when McMinos walks on them.

character       | description
--------------- | --------------------------------------------------------------
`w`             | Whisky
`M`             | mirror
`p`             | poison; can be cured with medicine


#### Tools

These are the items that, when collected, are put into the *toolbox*, in McMinos mobile that is the bar on the left side of the level screen. From there, they can be used by tapping/clicking on the respective item.

character       | description
--------------- | --------------------------------------------------------------
`k`             | key
`b`             | bomb
`d`             | dynamite
`_`             | mine (not aktivated)
`,`             | mine in the ground (aktivated)
`+`             | chocolate; power for later use; MCSPEED x2; GHSPEEDs x1
`m`             | medicine (bottle of)
`u`             | umbrella; helps McMinos to hover over holes

**Example:**

```
LEVEL:
XXXXXXXXXXXXXXXXXXXXXXXX
X*..........P.........1X
X.XXXX.XXXX..XXXX.XXXX.X
X.X..................X.X
X.X.XXX.XXXrOXXX.XXX.X.X
X.X..................X.X
X...XXXX.XXXXXX.XXXX...X
X.XXX......CC......XXX.X
X.....XXXX1CC.XXXX.....X
X.XXX.X*.XXXXXX.*X.XXX.X
X......................X
XXXXXXXXXXXXXXXXXXXXXXXX
#
```


### Sample level source

Below is the modernised level source of the very first level of the Atari
version of McMinos (version 0.815).

```
; LEVEL 000
; FOR McMINOS - Versions > 0.51

AUTHOR: Andreas_Neudecker
NUMBR:    000
VERSION:  0815
TITLE:    One ghost, one cookie
TITLE-de: Ein Geist, ein Keks
BODY:
Eat all peas. And stay alive.
#
BODY-de:
Iss alle Pillen. Und bleib am Leben.
#

LWID:     12
LHI:      12
SCROLLX:  0
SCROLLY:  0
BACK:     1

LTIME:    0
RSTRT:    0
MIRROR:   0

MCSPEED:  1

GHOST1:   1
GHSPEED1: 1
AGIL1:    10
GRTIME1:  5

LEVEL:
XXXXXXXXXXXX
X....P.....X
X.XX.XX.XX.X
X.X......X.X
X...XXXX...X
XXX..G...XXX
X...XXXX...X
X.XXXCCXXX.X
X....CC....X
X.XX.XX.XX.X
X*.........X
XXXXXXXXXXXX
#
```

</body>
</html>
