import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PszczolaKrolowa implements Runnable {
    private final Ul ul;
    private final ImageView krolowa;
    private final Pane panel;

    // Obrazek królowej
    private final Image obrazekKrolowej;

    // Konstruktor klasy PszczolaKrolowa
    public PszczolaKrolowa(Ul ul, Pane panel) {
        this.ul = ul;
        this.panel = panel;

        // Wczytanie obrazka królowej
        try {
            this.obrazekKrolowej = new Image("file:src/resources/krolowa.png");
        } catch (Exception e) {
            System.err.println("BŁĄD: Nie można wczytać obrazka królowej! Sprawdź czy plik queen_bee.png istnieje w src/resources/");
            e.printStackTrace();
            throw new RuntimeException("Brak pliku obrazka królowej", e);
        }

        // Stworzenie ImageView z obrazkiem królowej
        this.krolowa = new ImageView(obrazekKrolowej);
        krolowa.setFitWidth(100);
        krolowa.setFitHeight(100);
        krolowa.setPreserveRatio(true);

        // Dodanie królowej pszczół do panelu (stała pozycja w centrum ula)
        Platform.runLater(() -> {
            krolowa.setX(150 - 55);
            krolowa.setY(350 - 55);
            panel.getChildren().add(krolowa);
        });
    }


    public ImageView getQueen() {
        return krolowa;
    }

    // Metoda uruchamiana w wątku królowej pszczół
    @Override
    public void run() {
        while (true) {
            try {
                // Składanie jaj co 2 sekundy, jak się zakomentuje pracę pszczół poza ulem lub dla bardzo dużej ilości pszczół (100+) warto zmniejszyć ten czas
                Thread.sleep(2000);
                ul.zlozJaja();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Usuń królową z panelu przy przerwaniu
                Platform.runLater(() -> panel.getChildren().remove(krolowa));
                break;
            }
        }
    }
}