#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  testjson.py
#  
#  Copyright 2015 Andreas Neudecker <zapyon@gmx.net>

"""Load a JSON file to test if it's syntax is correct.

testjson.py expects exactly one command line argument: the name of the file 
to check.

If the JSON file can be imported and parsed successfully, testjson.py 
exits with return value 0. Otherwise exeptions from the json module will point 
at the errors found.
"""

from __future__ import print_function
import json
import pprint
import sys

def main():

  argv = sys.argv
  pp = pprint.PrettyPrinter( indent = 2, width = 80 )
  if len(argv) == 2:
    with open(argv[-1]) as infile:
      d = json.load(infile)
      #pp.pprint( d )
  else:
    print( __doc__ )
  
  return 0

if __name__ == '__main__':
  main()

