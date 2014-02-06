renamerFX 
=========
### ![Icon](https://raw2.github.com/teabeeoh/renamerFX/master/src/deploy/package/shortcut-128.png "Icon") Thommis Picture Renamer 

 The purpose of this software is to batch rename your pictures and movies after taking them from the camera from cryptic
to meaningful file names.
The filenames are compiled from the name of the parent directory plus a trailing index.

### Usage:

1. Choose a SOURCE and a TARGET directory with the according buttons
2. Place all files that you want to rename into a folder structure under a SOURCE directory of your choice.
3. Rename the folders under SOURCE with meaningful names that reflect whats on the pictures under this folder
   e.g.: `2013-07 Summer Holiday France` or `2012-12-24 Christmas at grandparents`
4. Push the button "Simulate renaming". The software analyzes the directories and shows you what would happen.
   No renaming is done at this point!
5. If you are satisfied with what would happen. Push the same button again, this time to really execute the renaming
   operation.
6. All files from SOURCE are recursively copied to TARGET and renamed according to their parent directory name with a
   trailing index. The index is calculated from the alphabetical order of the files.

### Note:

*  The files in SOURCE are not renamed, deleted our touched at all.
*  The files in TARGET are a 1:1 copy of their originals except for the name.
*  No files are overwritten in TARGET.
*  Files that comply with the following wildcards are not renamed or copied: ".*", "*.ini", "thumbs.db"
  
![Screenshot](http://www.thomasbolz.de/renamerFX/renamerFX.png "RenameFX Screenshot")
