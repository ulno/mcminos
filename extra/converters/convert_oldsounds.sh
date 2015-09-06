#!/bin/bash
for file in $(ls *.SAM); do
    low=$(echo $file| tr '[:upper:]' '[:lower:]')
    out=$(basename "$low" .sam).wav
    ffmpeg -f u8 -ar 8000 -ac 1 -i "$file" "$out"
done
