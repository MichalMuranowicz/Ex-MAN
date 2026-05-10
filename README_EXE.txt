MAN Ekstraktor - wersja EXE

Gotowy plik programu:
dist\MAN_Ekstraktor.exe

Jak uruchomic na innym komputerze:
1. Skopiuj plik dist\MAN_Ekstraktor.exe na drugi komputer.
2. Upewnij sie, ze na komputerze jest zainstalowany Google Chrome.
3. Uruchom MAN_Ekstraktor.exe.

Uwagi:
- Python nie musi byc zainstalowany na komputerze docelowym.
- Program korzysta z Selenium Managera, wiec przy pierwszym uruchomieniu moze potrzebowac dostepu do internetu, aby dobrac sterownik Chrome.
- Windows lub antywirus moze pokazac ostrzezenie, bo plik EXE nie jest podpisany cyfrowo.
- Opcja zapamietania loginu i hasla zapisuje dane w profilu uzytkownika Windows:
  %APPDATA%\MAN_Ekstraktor\config.json
  Haslo jest zabezpieczone mechanizmem Windows DPAPI i powinno byc mozliwe do odczytania tylko z tego samego konta Windows.

Jak zbudowac EXE ponownie po zmianach w kodzie:
1. Uruchom build_exe.bat.
2. Nowy plik pojawi sie w dist\MAN_Ekstraktor.exe.
