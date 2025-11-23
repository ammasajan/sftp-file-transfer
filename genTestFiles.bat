@echo off
setlocal enabledelayedexpansion

REM Path to config file
set "CONFIG_FILE=%~dp0src\main\resources\config.properties"

REM Read sftp.local.dir from config file
set "OUTDIR="
for /f "tokens=1* delims==" %%A in ('findstr /b "sftp.local.dir=" "%CONFIG_FILE%"') do (
    set "OUTDIR=%%B"
)

REM Check if OUTDIR was found
if "!OUTDIR!"=="" (
    echo Error: Could not find sftp.local.dir in %CONFIG_FILE%
    pause
    exit /b 1
)

REM Convert forward slashes to backslashes for Windows compatibility
set "OUTDIR=!OUTDIR:/=\!"

echo Output directory from config: "!OUTDIR!"

if not exist "!OUTDIR!" mkdir "!OUTDIR!"

echo Creating 100 random dummy files in "!OUTDIR!"...
echo.

for /L %%i in (1,1,100) do (
    REM Generate random size in KB (1 to 3072 KB)
    set /a "sizeKB=!random! %% 3072 + 1"
    set /a "sizeBytes=!sizeKB! * 1024"

    REM File name with index and random number
    set "filename=File_%%i_!random!.bin"

    REM Create the dummy file
    fsutil file createnew "!OUTDIR!\!filename!" !sizeBytes! >nul

    echo Created !filename! - Size: !sizeKB! KB
)

echo.
echo Done! 100 files created in "!OUTDIR!".
pause