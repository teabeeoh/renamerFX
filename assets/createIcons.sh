#!/bin/sh
alias inkscape='/Applications/Inkscape.app/Contents/Resources/bin/inkscape'
#for x in 16 32 64 128 256 ; do inkscape --export-area-drawing --export-png icon-${x}.png -w ${x} shortcut.svg ; done
#mv *.png ../src/main/resources
#for x in 16 32 64 128 256 ; do inkscape --export-area-drawing --export-png shortcut-${x}.png -w ${x} shortcut.svg ; done
declare -i w
for x in 16 32 64 128 256;
do
    w=$x*2
#    x=$x*2
#    echo $x
    inkscape --export-area-drawing --export-png icon-${x}@2x.png -w ${w} shortcut.svg ;
    inkscape --export-area-drawing --export-png icon-${x}.png -w ${x} shortcut.svg ;
    inkscape --export-area-drawing --export-png shortcut-${x}@2x.png -w ${w} shortcut.svg ;
    inkscape --export-area-drawing --export-png shortcut-${x}.png -w ${x} shortcut.svg ;
    inkscape --export-area-drawing --export-png volume-${x}@2x.png -w ${w} volume.svg ;
    inkscape --export-area-drawing --export-png volume-${x}.png -w ${x} volume.svg ;
done
mv icon*.png ../src/main/resources
mv shortcut*.png ../src/deploy/package
mv volume*.png ../src/deploy/package
#shortcut-64.png', 'shortcut-128.png', 'shortcut-256.png', 'shortcut-32@2x.png', 'shortcut-32@2x.png', 'shortcut-128@2x.png
