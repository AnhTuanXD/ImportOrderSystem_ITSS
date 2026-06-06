@echo off
setlocal enabledelayedexpansion

REM Change to script directory
cd /d "%~dp0"

REM Set paths
set "JAVA_FX=D:\Code\javafx-sdk-21.0.7\lib"
set "LIB_JAR=D:\Code\ImportOrderSystem_ITSS V2\lib\postgresql-42.7.1.jar"

if not exist out mkdir out

echo ========================================
echo Generating source file list...
echo ========================================
if exist sources.txt del sources.txt
for /r "src\main\java" %%f in (*.java) do (
    set "_fp=%%~f"
    set "_fp=!_fp:\=/!"
    echo "!_fp!">> sources.txt
)

echo ========================================
echo Compiling with PostgreSQL Driver...
echo ========================================

REM Compile with proper quote handling
setlocal disabledelayedexpansion
javac --module-path "%JAVA_FX%" --add-modules javafx.controls -cp "%LIB_JAR%" -encoding UTF-8 -d out @sources.txt 2>&1
endlocal & setlocal enabledelayedexpansion

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo.
    echo Check:
    echo 1. Is JAR file exists? "%LIB_JAR%"
    echo 2. Is JAVA_FX path correct? "%JAVA_FX%"
    if not exist "%LIB_JAR%" (
        echo.
        echo MISSING: postgresql-42.7.1.jar
        echo Download from: https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar
        echo Save to: D:\Code\ImportOrderSystem_ITSS V2\lib\
    )
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo ========================================
echo Copying resources...
echo ========================================
if exist src\main\resources (
    xcopy /E /I /Y src\main\resources out > nul 2>&1
)

echo.
echo ========================================
echo Running tests...
echo ========================================

REM Run tests
java --module-path "%JAVA_FX%" --add-modules javafx.controls -cp "out;%LIB_JAR%" com.itss.importorder.TestRunner

pause
