#!/bin/sh
alias inkscape='/Applications/Inkscape.app/Contents/Resources/bin/inkscape'
for x in 16 32 64 128 256 512 ; do inkscape --export-area-drawing --export-png shortcut-${x}.png -w ${x} logo.svg ; done
declare -i w
for x in 16 32 64 128 256 512;
do
    w=$x*2
#    x=$x*2
#    echo $x
    inkscape --export-area-drawing --export-png shortcut-${x}@2x.png -w ${w} logo.svg ;
done
mv *.png ../src/deploy/package
#shortcut-64.png', 'shortcut-128.png', 'shortcut-256.png', 'shortcut-32@2x.png', 'shortcut-32@2x.png', 'shortcut-128@2x.png
