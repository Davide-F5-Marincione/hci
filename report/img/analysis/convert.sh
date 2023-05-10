#!/bin/bash
# Odio windows
echo -e "\033[32m" # set output color to green

# iterate through all .svg files recursively
find . -name '*.svg' -print0 | while read -d $'\0' file
do
  # export each file as a PDF using inkscape
  inkscape --export-type=pdf "${file%.*}.svg"
  rm "${file%.*}.svg" # get rid of the .svg junk
done
