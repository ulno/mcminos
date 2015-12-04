#!/usr/bin/env python
# -*- coding: ISO-8859-15 -*-

"""
Convert numbered SVG files in the current directory to one animated GIF file.

This is achieved in 3 steps:
  1. Convert SVGs to PNGs using Inkscape
  2. Convert resulting PNGs to GIFs using convert from the ImageMagick toolbox
  3. Convert the GIFs to one animated GIF fil using the informations in
     the CONFIG file in the current folder

After this, the PNGs and GIFs (except for the animated one) will be deleted.

Requirements:
  * Bash
  * Python
  * Inkscape
  * convert (from ImageMagick)

Version: 0.2
Author(s): Andreas Neudecker (nope)
Created: 2010-08-10
Latest changes: 2010-08-17

Usage:
  * In a shell, cd to the directory where the SVG files are.
  * execute svg2anigif.py without any parameters
"""

# Imports ----------------------------------------------------------------------

import fnmatch
import os
import re
import subprocess
import types


# Execute always --------------------------------------------------------------#

# Set verbosity of program's messages. Requires 'verbosity.py'
# See documentation of verbosity.py for details.

# set to true, if you want to use it AND have verbosity.py
useVerbosity = True

if useVerbosity:
  import verbosity
  verbose = verbosity.Verbosity (
    #"Info",
    #"Value",
    "Testing",
    "Warning",
    #"Debugging"
  )
  # Use this if you want totally quiet execution.
  #verbose = verbosity.Quiet ()

# Globals ----------------------------------------------------------------------

config_options = {
  "symbol": None,  # symbol representing the entity
  "size":   1,     # the multiplier for the size (castle:2, explosion: 3)
  "speed":  50,   # animation speed (ms), single value or list. default = 100ms
  "order":  None,  # sequence of images, list, default [1, .., n] for n images
  "zindex": 0,     # show this on which level?
  "anchor": (0,0), # anchor point of the icon
  "moving": False, # This object moves by itself (like mcminos or the ghosts)
}

dpi = 180           # for the size of the PNG files
                   # (32x32-> 90; 48x48-> 135; 64x64 -> 180)

HIDE_PNG   = True
HIDE_GIF   = True
DELETE_PNG = False
DELETE_GIF = False
FORCE_CONVERSION = False
HIDE = "."         # prefix for hidden files; only works on Unix-like systems
# Here go the classes, functions, etc. -----------------------------------------


# Main function, in case this is run as a program. -----------------------------
def main ():
  cwd = os.getcwd()
  ls = os.listdir( cwd )

  ## Currently I simply "believe" that "*.svg" is a file, not a directory etc.
  #  TODO: Check should be inserted later.
  svg = fnmatch.filter(ls, "*.svg")

  svgDic = {}
  for f in svg:
    # Filename has 2-digit number right before the extension?
    name = os.path.splitext( os.path.split( f )[1] )[0]

    pattern = re.compile( r"""
      (.*)
      (\d{2})$  # expect exactly 2 digits at the end of the file name (excl.
                # the extension which has been stripped above)
    """,
    re.VERBOSE )

    m = pattern.match( name )

    # Isolate number
    if m:
      nr = m.group( 2 )
      # Add filename to svgDic using the isolated number as key
      if not nr.strip == "":
        svgDic[ int( nr ) ] = name
      else:
        # Otherwise drop svg, issuing a warning.
        #print( "WARNING: %s is not a properly numbered file and will not be used.\nFormat should be: <anyname><-><nn><.svg> where nn is a 2 digit number." % svg )
        verbose( "Warning", f , " is not a properly numbered file and will not bei used.\nFormat should be: <anyname><-><nn><.svg> where nn is a 2 digit number.", f )
    else:
      #print( "WARNING: %s is not a numbered file and will not be used." % f )
      verbose( "Warning", f, " is not a numbered file and will not be used." )
    #

  if svgDic:
    for k in svgDic.keys():
      infile = os.path.join( cwd, svgDic[k] + ".svg" )
      verbose( "Testing", "infile = ", infile )
      if HIDE_PNG:
        png = os.path.join( cwd, HIDE + svgDic[k] + ".png" )
        verbose( "Testing", "png = ", png )
      else:
        png = os.path.join( cwd, svgDic[k] + ".png" )
        verbose( "Testing", "png = ", png )
      if HIDE_GIF:
        gif = os.path.join( cwd, HIDE + svgDic[k] + ".gif" )
        verbose( "Testing", "gif = ", gif )
      else:
        gif = os.path.join( cwd, svgDic[k] + ".gif" )
        verbose( "Testing", "gif = ", gif )

      verbose( "Testing", "os.path.getctime( png ) = ", os.path.getctime( png ) )
      verbose( "Testing", "os.path.getctime( infile ) = ", os.path.getctime( infile ) )

      if os.path.exists( png ) and (( os.path.getctime( png ) <= os.path.getctime( infile ) ) or FORCE_CONVERSION):
        # Convert SVG to PNG using Inkscape ...
        p = subprocess.Popen(
          [
            "inkscape",
            "-z",             # --without-gui
            "-C",             # --export-area-page,
            "-d", "%s" % dpi, # --export-dpi=DPI,
            "-f", infile,     # --file=filename (input file)
            "-e", png,        # --export-png=filename
            "-b", "#ffffff",  # --export-background=COLOUR
          ],
          stdout=subprocess.PIPE
        )
        p.wait()
      else:
        verbose( "Testing" ,"Skipping conversion to PNG because\n\t", infile, "\nhas not changed since last conversion." )


      if os.path.exists( gif ) and (( os.path.getctime( gif ) <= os.path.getctime( png ) ) or FORCE_CONVERSION):
        # Convert PNG to GIF using convert from ImageMagick ...
        q = subprocess.Popen(
          [ "convert", png, gif ],
          stdout=subprocess.PIPE
        )
        q.wait()
      else:
        verbose( "Testing" ,"Skipping conversion PNG->GIF because\n\t", png, "\nhas not changed since last conversion." )

    if "CONFIG" in ls:
      execfile( "CONFIG" )

      for option in config_options:
        verbose( "Testing", "option = ", option )
        try:
          config_options[option]=eval(option)
          verbose( "Testing", "config_options[option] = ", config_options[option] )
        # Doesn't matter if a non-used value like zindex is not found in CONFIG!
        except:
          verbose( "Error", "config_options[option] could not be set." )

    # if there is no CONFIG or oder was not defined in config,
    # use the sorted list of keys in svgDic as order
    if not type( config_options["order"] ) == types.ListType:
      config_options["order"] = svgDic.keys()

    sequence = []
    for i in config_options["order"]:
      if HIDE_GIF:
        sequence.append( os.path.join( cwd, HIDE + svgDic[i] + ".gif" ) )
      else:
        sequence.append( os.path.join( cwd, svgDic[i] + ".gif" ) )

    execList = [
          "convert",
          "-delay", str( int( round( config_options["speed"] / 10.) ) ),
          "-loop", "0",
    ]

    execList = execList + sequence + [ os.path.join( cwd, "__ani__.gif" ) ]

    r = subprocess.Popen(
      execList,
      stdout = subprocess.PIPE
    )
    r.wait()

    for k in svgDic.keys():
      # ATTENTION: DO NOT DELETE THE SVG FILES!!!
      if DELETE_PNG:
        if HIDE_PNG:
          f = os.path.join( cwd, HIDE + svgDic[k] + ".png" )
          if os.path.exists( f ):
            os.remove( f )
        else:
          f = os.path.join( cwd, svgDic[k] + ".png" )
          if os.path.exists( f ):
            os.remove( f )
      if DELETE_GIF:
        if HIDE_GIF:
          f = os.path.join( cwd, HIDE + svgDic[k] + ".gif" )
          if os.path.exists( f ):
            os.remove( f )
        else:
          f = os.path.join( cwd, svgDic[k] + ".gif" )
          if os.path.exists( f ):
            os.remove( f )
  else:
    print( "No properly numbered SVG files found. Please check and try again." )
# Check, if this was run as program --------------------------------------------
if __name__ == '__main__':
  main ()


# EOF --------------------------------------------------------------------------
