===========================
Project McMinos: Agreements
===========================

--------------------------
Important for coordination
--------------------------

:Authors:
  Andreas Neudecker <zapyon@gmx.net>,
  Ulrich Norbisrath <ulno@ulno.net>

:Date: 2015-09-08


* Developer abbreviations:

  * Andreas Neudecker: nope (minuscules only! ;-)
  * Ulrich Norbisrath: UlNo

* We will develop both in a common git repository
* How will pictures be stored and translated:

  * every picture is in its own folder
  * The entity's name is constructed from the folders it is in (example: mcminos/up/01.svg -> mcminos_up)
  * CONFIG, README:
    Every folder can contain a CONFIG and a README file. The README will be ignored. The CONFIG file contains defaults for the current folder and all subfolders. It will be python-style and can contain the following:

    + code='X' with X the character representing the utf-8 code of the level element
    + size=n or size=(x,y) with n giving the size of the symbol in nr of fields it occupies (x,y) dimensions repsectively.
    + speed=value or speed = [value1,value2,...] with value specifiying the time in milliseconds how long each image is displayed or valuen being the value for the n'th image.

  * Pathname: <category>[/<subcategory>]/<entity>[/<entity-variatons>]/[anything]<aa>.[svg|<ww>x<hh>.png]

    + <category>: what kind of game entities are here grouped together (walls, pills, ghosts, ...)
    + <subcategory>: a sub category
    + <entity>: a name consisting only of English letters, numbers and _ (- will be translated to _)
    + <entity-kinds>: Usually something like directions (up, down, left right)
    + <aa>: animation number. If static only a file with <aa>=00 is available. If animated, no file with <aa>=00 available, but files with <aa>=01, 02, 03, ... depending on number of animation steps available
    + <ww> = width, <hh> = height
    + The extension is either svg or <ww>x<hh>.png. If it is svg, then the icon is present in svg format, endings <ww>x<hh> are images in png-format.
    + There must be images of the size 2x2 and 4x4 available for the radar screen

  * The icon renderer (some pre-processsor program Ulno will write) will read the existing svg files and create (if not already existent) png files in the following sizes will be created and taken into account

    + 128x128
    + 64x64
    + 48x48 <- vote for skipping
    + 32x32
    + 24x24 <- vote for skipping
    + 16x16
    + 12x12 <- vote for skipping
    + 08x08
    + [do we need 06x06?] <- need to do some testing when things have matured a bit
    + (also the radar icons with 04x04 and 02x02 will be read)

  * The renderer will create a new directory structure and leave the old one as is. The svg images are not copied to the new structure. It calls inkscape via command line (like for example this: inkscape -w 64 -e frontal-test.png frontal-nn.svg) to create the png's.

Thought dump:

* Boss monster/ghosts?
* Using inkscape via command line to create images
* drop the mirror¹ - necessary to still realize touch screen navigation - could still work, when we mirror screen coordinates
* convert the level sources in a way that they, too, become valid Python code (just like the CONFIGs), i.e.:
        currently there are <parameter>:<value> pairs separated by colons. Replace these by =. Currently ; is a comment character. Replace these by #. The level consists of a number of lines with "values" (strings). Convert this to """<newline><level><newline>""" (i.e. multi line Python strings).
____
¹ will cause shards lying around. 7 years of bad luck ;-) <- so it's 2015 now - five year past our last try, means, mcminos-mobile is done in 2017?
