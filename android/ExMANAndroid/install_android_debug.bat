@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "ANDROID_ROOT=%PROJECT_DIR%.."
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_HOME=%ANDROID_ROOT%\.android-sdk"
set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
set "GRADLE_HOME=%ANDROID_ROOT%\.gradle-local\gradle-8.10.2"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%GRADLE_HOME%\bin;%PATH%"

cd /d "%PROJECT_DIR%"
gradle.bat assembleDebug --no-daemon
if errorlevel 1 (
    echo.
    echo Build nie powiodl sie.
    pause
    exit /b 1
)

echo.
echo Podlaczone urzadzenia:
adb.exe devices -l
echo.
echo Instaluje Ex MAN...
adb.exe install -r "%PROJECT_DIR%app\build\outputs\apk\debug\app-debug.apk"

if errorlevel 1 (
    echo.
    echo Instalacja nie powiodla sie. Sprawdz, czy telefon ma wlaczone debugowanie USB i czy zaakceptowano komunikat RSA na ekranie telefonu.
    pause
    exit /b 1
)

echo.
echo Zainstalowano Ex MAN.
pause
