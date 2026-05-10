# Ex MAN Android

Aplikacja Android dla Ex MAN. To natywna aplikacja Java oparta o `WebView`, ktora otwiera portal MAN MOTOS, odczytuje jednostki z kodem `20` i zapisuje wynik do pliku `MAN extract.xlsx`.

## Co zawiera

- aplikacje Android o nazwie `Ex MAN`,
- ikone zgodna z wersja Windows,
- baner zgodny z wersja Windows,
- `WebView` otwierajacy portal MAN MOTOS,
- odczyt rekordow z kodem `20` z kolejnych stron listy,
- wejscie w szczegoly kazdego rekordu i pobranie danych z `TO details`,
- dodatkowy odczyt `Vehicle details` dla zaladunku `Bialezyce 100`,
- eksport pliku `MAN extract.xlsx` przez systemowe okno zapisu Androida,
- uproszczony interfejs z jednym glownym przyciskiem `Pobierz i zapisz`,
- pasek postepu i krotki tekst statusu,
- powrot do ekranu logowania po zapisaniu pliku,
- brak lokalnego zapisywania loginu i hasla.

## Aktualny przeplyw aplikacji

1. Po starcie aplikacja od razu otwiera ekran logowania MAN MOTOS.
2. Uzytkownik loguje sie w `WebView`.
3. Uzytkownik przechodzi do listy transportow/jednostek.
4. Uzytkownik klika `Pobierz i zapisz`.
5. Aplikacja zbiera jednostki z kodem `20`.
6. Aplikacja otwiera szczegoly kazdej jednostki i pobiera dane z `TO details`.
7. Dla zaladunku `Bialezyce 100` aplikacja dodatkowo sprawdza `Vehicle details`.
8. Aplikacja prosi o miejsce zapisu pliku `MAN extract.xlsx`.
9. Po zapisie aplikacja czysci sesje WebView i wraca do logowania.

## Mapowanie danych

Najwazniejsze reguly mapowania:

| Zrodlo | Kolumna Excel |
| --- | --- |
| Stala wartosc `MAN` | `NAZWA / SKROT` |
| Lista glowna `Vehic. no` | `VIN NUMER REFERENCYJNY` |
| TO details `VIN` | `WYSZUKIWANIE` |
| TO details `Type / cab` | `TOWAR NAZWA URZADZENIA` |
| TO details `Pick up until` | `ZALADUNEK PLANOWANA DATA` |
| TO details `Latest delivery` | `DOSTAWA PLANOWANA DATA` |
| TO details `Pick up` | pola adresu zaladunku |
| TO details `Delivery` | pola adresu dostawy |
| Vehicle details `Vehicle type` | dopisywane do towaru tylko dla `Bialezyce 100` |

Nie nalezy zamieniac pol `Vehic. no` i `VIN`: pierwsze trafia do numeru referencyjnego, drugie do pola wyszukiwania.

## Jak uruchomic

1. Otworz folder:

   `android\ExMANAndroid`

2. Uruchom `build_android_debug.bat`, zeby zbudowac APK.
3. APK pojawi sie tutaj:

   `app\build\outputs\apk\debug\app-debug.apk`

4. Jesli telefon jest podlaczony przez USB i ma wlaczone debugowanie USB, uruchom `install_android_debug.bat`.

Mozesz tez otworzyc projekt w Android Studio i kliknac `Run`.

## Test pomocniczy bez Android Studio

W projekcie znajduje sie pomocniczy test desktopowy:

`run_desktop_webview_test.bat`

Ten test:

1. otworzy Chrome w trybie mobilnym,
2. wejdzie na portal MAN MOTOS,
3. pozwoli zalogowac sie recznie,
4. po nacisnieciu Enter sprawdzi, czy skrypt widzi tekst strony, tabele i wiersze z kodem 20.

Wyniki zapisuja sie w:

`desktop-test-output\webview_inspect_result.json`

oraz:

`desktop-test-output\webview_test_screenshot.png`

To nie jest pelny emulator Android WebView, ale pozwala szybko testowac logike odczytu strony na tym komputerze.

## Pierwszy test

1. Aplikacja otworzy portal MAN MOTOS.
2. Zaloguj sie recznie w oknie WebView.
3. Przejdz do listy jednostek, tak jak w wersji Windows.
4. Kliknij `Pobierz i zapisz`.
5. Po zakonczeniu pobierania wybierz miejsce zapisu pliku `MAN extract.xlsx`.

Po zapisaniu pliku aplikacja wraca do ekranu logowania i resetuje status pobierania.

## Wymagania

- Android 8.0 lub nowszy,
- `minSdk 26`,
- dzialajacy Android System WebView lub systemowy Chrome,
- dostep do internetu,
- konto z dostepem do portalu MAN MOTOS.

## Artefakty

Gotowe lub wygenerowane pliki APK moga znajdowac sie w:

- `app\build\outputs\apk\debug\app-debug.apk`,
- `app\build\outputs\apk\release\app-release-unsigned.apk`,
- `release\Ex MAN.apk`,
- `release\Ex MAN-aligned.apk`.

Plik `release\ex-man-release.jks` jest kluczem podpisu APK. Nie powinien byc publikowany w publicznym repozytorium.

## Uwagi do publikacji na GitHub

Jesli projekt ma byc pokazany jako portfolio, warto:

- pokazac kod zrodlowy aplikacji i assety,
- nie publikowac prywatnych kluczy podpisu,
- nie publikowac danych logowania ani danych klientow,
- przeniesc gotowe APK do GitHub Releases zamiast trzymac je w repo,
- pominac lokalne katalogi SDK, cache i build outputs,
- opisac projekt jako przyklad automatyzacji WebView, mapowania danych i generowania XLSX.

## Wazne

Aplikacja nie zapisuje loginu ani hasla lokalnie. Komunikuje sie z portalem MAN MOTOS przez `WebView` i zapisuje tylko plik XLSX wskazany przez uzytkownika. Nie jest to formalny audyt bezpieczenstwa, tylko opis aktualnej architektury aplikacji.
