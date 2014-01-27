@echo off
set PATH=C:\Program Files\Inkscape;%PATH%
FOR %%f IN (16 32 64 128 256) DO (
    echo inkscape --export-area-drawing --export-png shortcut-%%f.png -w %%f logo.svg
    inkscape --export-area-drawing --export-png icon-%%f.png -w %%f shortcut.svg
    inkscape --export-area-drawing --export-png shortcut-%%f.png -w %%f shortcut.svg
)
inkscape --export-area-drawing --export-png setup.png -w 48 shortcut.svg
move icon*.png ..\src\main\resources
move shortcut*.png ..\src\deploy\package
move setup.png ..\src\deploy\package
