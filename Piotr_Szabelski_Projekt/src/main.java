import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class main extends Application {
    private int liczbaPoczatkowychPszczol;
    private int maksymalnaLiczbaPszczolWulu;
    private int maxWejscDoUla;
    private boolean czySymulacjaUruchomiona = false;
    private final List<Thread> watkiPszczol = new ArrayList<>(); // Lista wątków pszczół robotnic
    private Thread watekKrolowej; // Wątek pszczoły królowej
    private Ul ul; // Obiekt Ul dostępny na poziomie klasy
    @Override
    public void start(Stage scenaPodstawowa) {
        // Wczytanie ustawień z pliku properties
        Properties wlasciwosci = new Properties();
        try {
            FileInputStream wejscie = new FileInputStream("src/resources/config.properties");
            wlasciwosci.load(wejscie);

            liczbaPoczatkowychPszczol = Integer.parseInt(wlasciwosci.getProperty("liczbaPoczatkowychPszczol"));
            maksymalnaLiczbaPszczolWulu = Integer.parseInt(wlasciwosci.getProperty("maksymalnaLiczbaPszczolWulu"));
            maxWejscDoUla = Integer.parseInt(wlasciwosci.getProperty("maxWejscDoUla"));

        } catch (IOException _) {
        }

        scenaPodstawowa.setTitle("Piotr Szabelski WCY23IY2S1");
        // Tworzenie głównego panelu
        Pane panel = new Pane();
        panel.setPrefSize(1250, 700);
        panel.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        Scene scena = new Scene(panel, 1250, 700);

        Rectangle kolorowyObszar = new Rectangle(0, 0, 480, 700); // (x, y, szerokość, wysokość)
        kolorowyObszar.setFill(Color.GOLD); //kolor ula
        panel.getChildren().add(kolorowyObszar);

        Rectangle prawyProstokatStrony = new Rectangle(1000, 0, 250, 700); // (x, y, szerokość, wysokość)
        prawyProstokatStrony.setFill(Color.WHITE); // Ustawienie koloru wypełnienia prostokąta
        prawyProstokatStrony.setStroke(Color.BLACK); // Ustawienie koloru obramowania
        prawyProstokatStrony.setStrokeWidth(2); // Ustawienie grubości obramowania
        panel.getChildren().add(prawyProstokatStrony);

        //  Przyciski Start i Stop
        Button przyciskStart = new Button("Start");
        przyciskStart.setLayoutX(1060);
        przyciskStart.setLayoutY(500);
        przyciskStart.setPrefWidth(130);
        przyciskStart.setPrefHeight(60);
        przyciskStart.setStyle("-fx-font-size: 16px;"+"-fx-background-color: #4CAF50;" +"-fx-text-fill: white;");
        przyciskStart.setOnAction(_ -> uruchomSymulacje(panel));

        Button przyciskStop = new Button("Stop");
        przyciskStop.setLayoutX(1060);
        przyciskStop.setLayoutY(600);
        przyciskStop.setPrefWidth(130);
        przyciskStop.setPrefHeight(60);
        przyciskStop.setStyle("-fx-font-size: 16px;"+"-fx-background-color: #F44336;" +"-fx-text-fill: white;");
        przyciskStop.setOnAction(_ -> zatrzymajSymulacje());

        Label etykietaLiczbaPszczol = new Label("Liczba Początkowych Pszczół");
        etykietaLiczbaPszczol.setLayoutX(1016);
        etykietaLiczbaPszczol.setLayoutY(25);
        etykietaLiczbaPszczol.setStyle("-fx-font-size: 16px;");
        TextField poleLiczbaPszczol = new TextField(String.valueOf(liczbaPoczatkowychPszczol));
        poleLiczbaPszczol.setLayoutX(1020);
        poleLiczbaPszczol.setLayoutY(50);
        poleLiczbaPszczol.setStyle("-fx-font-size: 16px;");

        Label etykietaMaksymalnaLiczbaPszczol = new Label("Maksymalna Liczba Pszczół w ulu");
        etykietaMaksymalnaLiczbaPszczol.setLayoutX(1005);
        etykietaMaksymalnaLiczbaPszczol.setLayoutY(85);
        etykietaMaksymalnaLiczbaPszczol.setStyle("-fx-font-size: 16px;");
        TextField poleMaksymalnaLiczbaPszczol = new TextField(String.valueOf(maksymalnaLiczbaPszczolWulu));
        poleMaksymalnaLiczbaPszczol.setLayoutX(1020);
        poleMaksymalnaLiczbaPszczol.setLayoutY(110);
        poleMaksymalnaLiczbaPszczol.setStyle("-fx-font-size: 16px;");

        Label etykietaMaxWejscDoUla = new Label("Maksymalna liczba wejść do ula");
        etykietaMaxWejscDoUla.setLayoutX(1010);
        etykietaMaxWejscDoUla.setLayoutY(145);
        etykietaMaxWejscDoUla.setStyle("-fx-font-size: 16px;");
        TextField poleMaxWejscDoUla = new TextField(String.valueOf(maxWejscDoUla));
        poleMaxWejscDoUla.setLayoutX(1020);
        poleMaxWejscDoUla.setLayoutY(175);
        poleMaxWejscDoUla.setStyle("-fx-font-size: 16px;");

        Button przyciskZatwierdz = new Button("Zatwierdź");
        przyciskZatwierdz.setLayoutX(1060);
        przyciskZatwierdz.setLayoutY(240);
        przyciskZatwierdz.setPrefWidth(130);
        przyciskZatwierdz.setPrefHeight(60);
        przyciskZatwierdz.setStyle("-fx-font-size: 16px;"+"-fx-background-color: #2196F3;" +"-fx-text-fill: white;");
        przyciskZatwierdz.setOnAction(_ -> potwierdz(poleLiczbaPszczol.getText(), poleMaksymalnaLiczbaPszczol.getText(),poleMaxWejscDoUla.getText(),panel));

        panel.getChildren().addAll(przyciskStart, przyciskStop, etykietaLiczbaPszczol, poleLiczbaPszczol, etykietaMaksymalnaLiczbaPszczol, poleMaksymalnaLiczbaPszczol, etykietaMaxWejscDoUla, poleMaxWejscDoUla, przyciskZatwierdz);

        // Tworzenie krzywej linii
        Path sciezka1 = new Path();
        sciezka1.setStroke(Color.GOLD);
        sciezka1.setStrokeWidth(70);

        //  Punkt początkowy krzywej
        MoveTo tlo1 = new MoveTo();
        tlo1.setX(450);
        tlo1.setY(0);

        //  Punkty krzywej
        CubicCurveTo tloDroga = new CubicCurveTo();
        tloDroga.setControlX1(530); // Kontrolny punkt X1
        tloDroga.setControlY1(233);  // Kontrolny punkt Y1
        tloDroga.setControlX2(530); // Kontrolny punkt X1
        tloDroga.setControlY2(466);  // Kontrolny punkt Y1
        tloDroga.setX(450);         // Koncowy punkt X
        tloDroga.setY(700);         // Koncowy punkt Y

        sciezka1.getElements().add(tlo1);
        sciezka1.getElements().add(tloDroga);
        panel.getChildren().add(sciezka1);

        // Krzywa dla ścian ula
        Path sciezka2 = new Path();
        sciezka2.setStroke(Color.BROWN); // Ustawienie koloru linii
        sciezka2.setStrokeWidth(15); // Ustawienie grubości linii

        MoveTo linia1 = new MoveTo();
        linia1.setX(480);
        linia1.setY(0);

        CubicCurveTo droga1 = new CubicCurveTo();
        droga1.setControlX1(510); // Kontrolny punkt X1
        droga1.setControlY1(80);  // Kontrolny punkt Y1
        droga1.setControlX2(518); // Kontrolny punkt X1
        droga1.setControlY2(150); // Kontrolny punkt Y1
        droga1.setX(527);         // Koncowy punkt X
        droga1.setY(200);         // Koncowy punkt Y

        MoveTo linia2 = new MoveTo();
        linia2.setX(534);
        linia2.setY(260);

        CubicCurveTo droga2 = new CubicCurveTo();
        droga2.setControlX1(539); // Kontrolny punkt X1
        droga2.setControlY1(320); // Kontrolny punkt Y1
        droga2.setControlX2(539); // Kontrolny punkt X1
        droga2.setControlY2(380); // Kontrolny punkt Y1
        droga2.setX(534);         // Koncowy punkt X
        droga2.setY(440);         // Koncowy punkt Y

        MoveTo linia3 = new MoveTo();
        linia3.setX(480);
        linia3.setY(700);

        CubicCurveTo droga3 = new CubicCurveTo();
        droga3.setControlX1(510); // Kontrolny punkt X1
        droga3.setControlY1(620);  // Kontrolny punkt Y1
        droga3.setControlX2(518); // Kontrolny punkt X1
        droga3.setControlY2(550);  // Kontrolny punkt Y1
        droga3.setX(527);         // Koncowy punkt X
        droga3.setY(500);         // Koncowy punkt Y

        sciezka2.getElements().add(linia1);
        sciezka2.getElements().add(droga1);
        sciezka2.getElements().add(linia2);
        sciezka2.getElements().add(droga2);
        sciezka2.getElements().add(linia3);
        sciezka2.getElements().add(droga3);

        panel.getChildren().add(sciezka2);

        scenaPodstawowa.setScene(scena);
        scenaPodstawowa.show();

    }
    // Metoda uruchamiająca symulację
    private void uruchomSymulacje(Pane panel) {
        if (!czySymulacjaUruchomiona) {
            if (liczbaPoczatkowychPszczol <= 0 || maksymalnaLiczbaPszczolWulu <= 0 || maxWejscDoUla <= 0) {
                //  Alerty o niepoprawnych danych
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Błąd danych");
                alert.setHeaderText("Niepoprawne dane");
                alert.setContentText("Wartości muszą być dodatnie");
                alert.showAndWait();
                return;
            }
            int zaokraglonaPolowa = (int) Math.ceil(liczbaPoczatkowychPszczol / 2.0);
            if(maksymalnaLiczbaPszczolWulu >= zaokraglonaPolowa){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Błąd danych");
                alert.setHeaderText("Niepoprawne dane");
                alert.setContentText("Maksymalna liczba pszczół w ulu musi być mniejsza niż połowa liczby pszczół.");
                alert.showAndWait();
                return;
            }
            czySymulacjaUruchomiona = true;
            ul = new Ul(maksymalnaLiczbaPszczolWulu, panel, maxWejscDoUla);

            // Uruchomienie wątku pszczoły królowej
            watekKrolowej = new Thread(() -> new PszczolaKrolowa(ul, panel).run());
            watekKrolowej.start();

            // Uruchomienie wątków pszczół robotnic
            for (int i = 0; i < liczbaPoczatkowychPszczol; i++) {
                Thread watekPszczoly = new Thread(() -> new Pszczola(ul, maxWejscDoUla, panel, false, 0, 0).run());
                watkiPszczol.add(watekPszczoly);
                watekPszczoly.start();
            }
        }
    }
    // Metoda potwierdzająca nowe dane i uruchamiająca symulację od nowa
    private void potwierdz(String lpp, String mlp, String mwdu,Pane panel)
    {
        try {
            liczbaPoczatkowychPszczol = Integer.parseInt(lpp);
            maksymalnaLiczbaPszczolWulu = Integer.parseInt(mlp);
            maxWejscDoUla = Integer.parseInt(mwdu);
            zatrzymajSymulacje(); // Zatrzymanie bieżącej symulacji
            uruchomSymulacje(panel); // Uruchomienie nowej symulacji z nowymi danymi
        } catch (NumberFormatException e) {
            // Alert o błędnym formacie danych
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Błąd danych");
            alert.setHeaderText("Niepoprawny format danych");
            alert.setContentText("Proszę wprowadzić prawidłowe liczby całkowite.");
            alert.showAndWait();
        }
    }
    // Metoda zatrzymująca symulację
    private void zatrzymajSymulacje() {
        czySymulacjaUruchomiona = false;
        if (watekKrolowej != null) {
            watekKrolowej.interrupt();
        }
        // Przerwanie wątków pszczół robotnic
        for (Thread watekPszczoly : watkiPszczol) {
            watekPszczoly.interrupt();
        }
        watkiPszczol.clear();
        // Zatrzymanie symulacji w Ul
        if (ul != null) {
            ul.requestStop();
        }
        if (ul != null) {
            ul.usunJaja(); // Usuwanie jaj po zatrzymaniu symulacji
        }
    }
    public static void main(String[] args) {
        launch(args); // Uruchomienie aplikacji JavaFX
    }
}