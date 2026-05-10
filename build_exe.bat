@echo off
setlocal

cd /d "%~dp0"

py -m pip install -r requirements.txt
py -m pip install pyinstaller

py -m PyInstaller --clean --noconfirm --onefile --windowed --add-data "assets;assets" --collect-all selenium --icon "assets\ex_man.ico" --name "Ex MAN" man_extractor.py

echo.
echo Gotowe. Plik EXE znajduje sie w folderze:
echo %~dp0dist\Ex MAN.exe
echo.
pause
