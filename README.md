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

Przejść do zakładki Libraries.

Kliknąć + → Java, wybierać katalog lib z rozpakowanego SDK, np. C:\javafx-sdk\lib.

Zaznaczyć wszystkie pliki .jar i zatwierdźić.

Upewnić się, że biblioteka została przypisana do modułu w zakładce Modules → Dependencies.

3. Skonfigurować VM options dla JavaFX
Otwórzyć konfigurację uruchamiania: Run → Edit Configurations.

Dodać Application.

Dodać main jako Main Class.

Wejść w Modift Options.

W polu VM options dodać:


--module-path "C:\ścieżka\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics --add-opens java.base/sun.misc=ALL-UNNAMED


4. Zapisać konfigurację
