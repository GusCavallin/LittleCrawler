@echo off
rem Starts LittleCrawler from the jar that "mvn clean package" builds.
rem Double-click it, or run it from any folder. To use another properties file,
rem pass its path:  LittleCrawler.bat config\my-site.local.properties
setlocal

rem The properties file: the first argument, or config\webCrawler.properties.
rem It is resolved here, before the working directory changes.
if "%~1"=="" (
    set "CONFIG=%~dp0config\webCrawler.properties"
) else (
    set "CONFIG=%~f1"
)

rem Use JAVA_HOME if it is set, otherwise the java found on the PATH.
if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
    set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
) else (
    set "JAVA=java"
    set "JAVAW=javaw"
)

rem Check that Java is installed and is version 21 or later.
set "JAVA_VERSION="
for /f "tokens=2 delims==" %%v in ('"%JAVA%" -XshowSettings:properties -version 2^>^&1 ^| findstr java.specification.version') do set "JAVA_VERSION=%%v"
if not defined JAVA_VERSION (
    echo Java was not found. Install Java 21 or later, or set JAVA_HOME.
    pause
    exit /b 1
)
set "JAVA_MAJOR=0"
set /a JAVA_MAJOR=%JAVA_VERSION: =% 2>nul
if %JAVA_MAJOR% LSS 21 (
    echo LittleCrawler needs Java 21 or later, but this is Java %JAVA_VERSION: =%.
    echo Install a newer Java, or set JAVA_HOME to one.
    pause
    exit /b 1
)

rem Find the jar, whatever its version number.
set "JAR="
for %%f in ("%~dp0target\littlecrawler-*.jar") do set "JAR=%%f"
if not defined JAR (
    echo LittleCrawler has not been built yet. Run this first, in this folder:
    echo     mvn clean package
    pause
    exit /b 1
)

rem Run from the "out" folder: when webCrawler.destinationFolder is not set,
rem the downloads and the logs are saved under the working directory.
if not exist "%~dp0out" mkdir "%~dp0out"
cd /d "%~dp0out"

rem javaw starts the program without a console window.
start "LittleCrawler" "%JAVAW%" -jar "%JAR%" "%CONFIG%"
