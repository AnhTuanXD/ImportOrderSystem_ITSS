@echo off
setlocal
set "JAVA_FX=D:\Code\javafx-sdk-21.0.7\lib"
if not exist out mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac --module-path "%JAVA_FX%" --add-modules javafx.controls -encoding UTF-8 -d out @sources.txt
if errorlevel 1 exit /b 1
java -cp out com.itss.importorder.TestRunner

