#!/bin/bash
silence=1.0
for file in $(ls *.wav); do
    low=$(echo $file| tr '[:upper:]' '[:lower:]')
    out=$(basename "$low" .wav).mp3
    # also add some silence to end to prevent problems on iOS
    ffmpeg \
        -i "$file"  \
        -f lavfi -i "aevalsrc=0|0|0|0|0|0:d=$silence" \
        -filter_complex "[0:0] [1:0] concat=n=2:v=0:a=1 [a]" -map [a] \
        -b:a 128k "$out"
done
