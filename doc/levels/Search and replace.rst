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
