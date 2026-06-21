@echo off
setlocal.\
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    for /f "delims=" %%D in ('dir /b /ad /o-n "%USERPROFILE%\.jdks" 2^>nul') do (
        if exist "%USERPROFILE%\.jdks\%%D\bin\javac.exe" (
            set "JDK_HOME=%USERPROFILE%\.jdks\%%D"
            goto found_jdk
        )
    )
    echo javac not found. Please install a JDK and add its bin directory to PATH.
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    for /f "delims=" %%D in ('dir /b /ad /o-n "%USERPROFILE%\.jdks" 2^>nul') do (
        if exist "%USERPROFILE%\.jdks\%%D\bin\java.exe" (
            set "JDK_HOME=%USERPROFILE%\.jdks\%%D"
            goto found_jdk
        )
    )
    echo java not found. Please install a JDK and add its bin directory to PATH.
    exit /b 1
)

:found_jdk
if defined JDK_HOME (
    set "PATH=%JDK_HOME%\bin;%PATH%"
)

if not exist out mkdir out
javac -encoding UTF-8 -d out src\DanmakuCover.java
if errorlevel 1 exit /b 1

java -cp out DanmakuCover
