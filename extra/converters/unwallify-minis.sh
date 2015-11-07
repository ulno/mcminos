#!/bin/bash
# Andreas was confuding doors and animations, so this had to be modified for the mini-images
for i in *.svg; do 
    root=$(basename $i .svg); 
    mkdir $root; 
    mv $root.svg $root/mini.svg; 
done