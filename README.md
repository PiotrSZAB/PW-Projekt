# PW-Projekt
Jeśli nie ma się jescze dodanego JavaFX

 Konfiguracja JavaFX w IntelliJ IDEA 
1. Pobrać JavaFX SDK
Przejść do: https://gluonhq.com/products/javafx/

Pobrać paczkę JavaFX SDK odpowiednią dla systemu operacyjnego.

Rozpakować archiwum do katalogu lokalnego, np. C:\javafx-sdk lub /home/user/javafx-sdk.

2. Dodać JavaFX SDK jako bibliotekę do projektu
Otwóryć projekt w IntelliJ IDEA.

Otwóryć ustawienia modułu: File → Project Structure (Ctrl+Alt+Shift+S lub F4).

Przejdź do zakładki Libraries.

Kliknąć + → Java, wybierać katalog lib z rozpakowanego SDK, np. C:\javafx-sdk\lib.

Zaznaczyć wszystkie pliki .jar i zatwierdź.

Upewnić się, że biblioteka została przypisana do modułu w zakładce Modules → Dependencies.

3. Skonfigurować VM options dla JavaFX
Otwórzyć konfigurację uruchamiania: Run → Edit Configurations.

W polu VM options dodać:

swift
Kopiuj
Edytuj
--module-path /ścieżka/do/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml


4. Zapisać konfigurację
