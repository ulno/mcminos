Search and replace
==================

Remove deprecated min/max tools settings:
-----------------------------------------

*regex search:*
"^(LIVE|KEYS|BMBS|DYNA|MINE|CHOC|MEDC|UMBR)\:\s*\d*\,\d*\n"

*replace:* ""



*regex search:*
"^\;\s*(HEADER|LEVELFORMAT|STATUS\-Angaben|McMINOS|GEISTER|LEVELDATEN|MIN\-\/MAX\-ZAHL\ DER\ WERKZEUGE)\s*(\-)*[\+|\*]\n"

*replace:* ""



^ACCCD\:\s+\d+\s*\n

""



^(\;\s+LEVEL)\s+(\d+|NEW)\s+\-+[\+|\*]\n

\1\ \2\n



^(AUTHOR\:)\s+NoPe\;\s+(Andreas)(\s+)(Neudecker)

\1\ \2\_\4



^(AUTHOR\:)\s+(NoPe)

\1\ Andreas\_Neudecker
