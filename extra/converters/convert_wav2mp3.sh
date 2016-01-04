#!/bin/bash
for file in $(ls *.wav); do
    low=$(echo $file| tr '[:upper:]' '[:lower:]')
    out=$(basename "$low" .wav).mp3
    ffmpeg -i "$file" -b:a 128k "$out"
done
