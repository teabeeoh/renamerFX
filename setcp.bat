REM ***
REM this must be run before creating native installers with the gradle javafx plugin
REM ***
set JAVA_HOME="C:\Program Files\Java\jdk1.8.0"
REM adding JAVA executables
set PATH=%JAVA_HOME%\bin;%PATH%
REM adding InnoSetup
set PATH=C:\Program Files\Inno Setup 5;%PATH%
REM adding WiX
set PATH=C:\Program Files\Windows Installer XML v3\bin;%PATH%

