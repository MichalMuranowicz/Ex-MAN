@echo off
setlocal

cd /d "%~dp0"

py -m pip install selenium
py desktop_webview_test.py

pause
