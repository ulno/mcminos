#!/usr/bin/env python
# -*- coding: ISO-8859-15 -*-
"""Provide fine-grained message output according to type of info and importance.

When you initialise a Verbosity object, you may pass a list (or tuple) of
strings (or, if you desire so, numbers) to name the levels of verbosity. If you
do not provide any, a standard set of verbosity levels is used:

  "info",       # informational stuff
  "value",      # usually parameter values, input values, stuff like that
  "warning",    # warning messages if something is not perfect
  "testing",    # messages for testing purposes
  "debugging",  # messages for debugging, usually the most verbose

How to use verbosity.py:

  1. Import the module.

  from verbosity import Verbosity, Quiet

  2. Initialise an instance of Verbosity

  verbose = Verbosity (
    "Info",
    "Value",
    "Testing",
    "Warning",
    "Debugging",
    "Error",
  )

  3. Use the new object instead of plain 'print' commands to output program
  information.

  verbose ("Info", "Now processing XY ...")
  verbose ("Value", "valueName =", value)
  verbose ("Testing", "x =", x, "y =", y)

  Remark: At initialisation of the object comment-out values, if you don't
  need them or initialise conditionally what you need according to command
  line options. If you want totally quiet processing, do not pass any
  verbosity levels at initialisation or use

  verbose = quiet ()


Author: Andreas Neudecker
Created: 2003-08-28
Changed: 2010-08-14
Project version: 0.1
Module version : 0.3

Many thanks go to Michael Wurzel for suggesting this feature and discussing
several possible ways of implementing it.
"""

# Comment conventions:
# [???] = Problem to be solved
# [!!!] = Needs testing/editing
# You should add a usefull explanation.

# Modules ----------------------------------------------------------------------
import sys
import types


# Globals ----------------------------------------------------------------------
verboseTesting = True

# Classes ----------------------------------------------------------------------

class Verbosity:
  """Handle verbosity-correlated output of messages in a program.

  On initialisation pass a list of verbosity levels (i.e. strings or integers
  whatever you prefer). If you do not pass a list, verbosity levels default
  to:
    "Info",       # informational stuff
    "Value",      # usually parameter values, input values, stuff like that
    "Warning",    # warning messages if something is not perfect
    "Testing",    # messages for testing purposes
    "Debugging",  # messages for debugging, usually the most verbose
    "Error",      # Error messages (when more than the usual exeption stuff is needed)
  """

  defaultLevels = [
    "Info",       # informational stuff
    "Value",      # usually parameter values, input values, stuff like that
    "Warning",    # warning messages if something is not perfect
    "Testing",    # messages for testing purposes
    "Debugging",  # messages for debugging, usually the most verbose
    "Error",      # Error messages (when more than the usual exeption stuff is needed)
  ]

  braces = ('','')

  def __init__ (self, *levels):
    """Set up verbosity levels.

    If none are provided by the caller, use defaults instead.
    """
    self.levels = levels or self.defaultLevels

    ok = [l for l in self.levels if type (l) == type (self.levels [0])]
    if not ok:
      raise "[Error] All level types must be of same type, i.e. strings or numbers."

    self.setBraces ()
  #

  def setBraces (self, before = '[', after = ']'):
    """Set the 'braces' before and after the labels of the messages.


    Parameters:

    before -- the character or string to go before the label. Default is '['.

    after -- the character or string to go after the label and before the
    message content. Default is ']'.
    """
    self.braces = (before, after)
  #

  def __call__ (self, level, *values):
    """Print message, if verbosity level applies."""

    separator = ' '

    if level in self.levels:
      msgText = separator.join ([str (v) for v in values])
      if type (level) == types.StringType:
        msgText = (
          self.braces [0]
        + str (level)
        + (self.braces [1] and self.braces [1] + separator or '')
        + msgText
        )

      print >> sys.stderr, msgText
  #
# End of class Verbosity


class Quiet:
  """Do not print any messages.

  Use this class to initialise the verbosity object, if you do not
  want any message output.
  """
  def __call__ (self, level, *values):
    pass
# End of class Quiet


# Functions --------------------------------------------------------------------

# main () ----------------------------------------------------------------------
def __main ():
  """Show some examples when this module is called as program."""

  # variables
  message1 = "First message."
  message2 = "2nd message."
  message3 = "3rd message."
  message4 = "4th and last message."
  x = 2 ** 16

  # levels I want to introduce, will override default.
  myLevels = [
    "info",        # informational stuff
    "showVal",     # parameter values
    "attention",   # warning messages
    "bugSwatting", # messages for debugging purposes
    "sayAll",      # print ALL messages. Yeah.
  ]

  # use this for default verbosity levels
  verbose = Verbosity ()

  # use this to set your own verbosity levels
  # if you later want to omit certain messages, simply omit the verbosity
  # level key from the list.
  #verbose = Verbose (myLevels)

  # use this if you want your program to shut up and not clutter the console
  #verbose = Quiet()

  # a message targeted to one verbosity level
  verbose ("Testing", message1, "x =", x,)
  verbose ("Info",    message2)
  verbose ("Warning", message3)


# Check, if this was run as program --------------------------------------------

if __name__ == '__main__':
  __main ()


# EOF ==========================================================================


