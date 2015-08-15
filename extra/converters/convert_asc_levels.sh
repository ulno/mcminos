#!/bin/bash
for f in $(find ../levels); do
    echo "File: $f"
    recode AtariST..UTF-8 "$f"
    python convert_holes.py "$f" > /tmp/convert_asc_levels.tmp
    cp /tmp/convert_asc_levels.tmp "$f"
done
