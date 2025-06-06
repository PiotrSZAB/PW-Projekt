import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Pszczola implements Runnable {
    private final Ul ul;
    private final int maxWejscDoUla;
    private final ImageView pszczola;
    private boolean wUlu = false;
    private final Pane panel;
    private final boolean wykluta;

    private CountDownLatch wejscieLatch;
    private CountDownLatch wyjscieLatch;

    // Obrazki pszczół
    private final Image obrazekNormalnejPszczoly;
    private final Image obrazekCzerwonejPszczoly;
    private final Image obrazekMartwejPszczoly;

    public Pszczola(Ul ul, int maxWejscDoUla, Pane panel, boolean wykluta, int X, int Y) {
        this.ul = ul;
        this.maxWejscDoUla = maxWejscDoUla;
        this.panel = panel;
        this.wykluta = wykluta;

        // Wczytanie obrazków pszczół
        try {
            this.obrazekNormalnejPszczoly = new Image("file:src/resources/gucio_normal.png");
            this.obrazekCzerwonejPszczoly = new Image("file:src/resources/gucio_wyjscie.png");
            this.obrazekMartwejPszczoly = new Image("file:src/resources/gucio_dead.png");
        } catch (Exception e) {
            System.err.println("BŁĄD: Nie można wczytać obrazków pszczół!");
            e.printStackTrace();
            throw new RuntimeException("Brak plików obrazków pszczół", e);
        }

        this.pszczola = new ImageView(obrazekNormalnejPszczoly);
        pszczola.setFitWidth(40);
        pszczola.setFitHeight(40);
        pszczola.setPreserveRatio(true);

        if(wykluta) {
            Platform.runLater(() -> {
                pszczola.setX(X - 12.5);
                pszczola.setY(Y - 12.5);
                panel.getChildren().add(pszczola);
            });
        } else {
            Platform.runLater(() -> {
                pszczola.setX(570 + Math.random() * 410 - 12.5);
                pszczola.setY(10 + Math.random() * 670 - 12.5);
                panel.getChildren().add(pszczola);
            });
        }
    }

    public ImageView getBee() {
        return pszczola;
    }

    public void wejscie() {
        wUlu = true;
        if (wejscieLatch != null) {
            wejscieLatch.countDown(); // Powiadom czekający wątek
        }
    }

    public void wyjscie() {
        wUlu = false;
        if (wyjscieLatch != null) {
            wyjscieLatch.countDown(); // Powiadom czekający wątek
        }
    }

    private void ustawNormalnaPszczole() {
        Platform.runLater(() -> {
            if (pszczola != null && obrazekNormalnejPszczoly != null) {
                pszczola.setImage(obrazekNormalnejPszczoly);
            }
        });
    }

    private void ustawCzerwonaPszczole() {
        Platform.runLater(() -> {
            if (pszczola != null && obrazekCzerwonejPszczoly != null) {
                pszczola.setImage(obrazekCzerwonejPszczoly);
            }
        });
    }

    private void ustawMartwaPszczole() {
        Platform.runLater(() -> {
            if (pszczola != null && obrazekMartwejPszczoly != null) {
                pszczola.setImage(obrazekMartwejPszczoly);
            }
        });
    }

    @Override
    public void run() {
        Random random = new Random();
        for (int i = 0; i < maxWejscDoUla; i++) {
            try {
                if(wykluta && i == 0) {
                    Random rand = new Random();
                    double x = 20 + Math.random() * 430 - 12.5;
                    double y;
                    if (rand.nextBoolean()) {
                        y = 20 + Math.random() * 280 - 12.5;
                    } else {
                        y = 400 + Math.random() * 270 - 12.5;
                    }
                    Platform.runLater(() -> {
                        Timeline timeline = new Timeline();
                        KeyFrame keyFrame = new KeyFrame(
                                Duration.seconds(1),
                                new KeyValue(pszczola.xProperty(), x),
                                new KeyValue(pszczola.yProperty(), y)
                        );
                        timeline.getKeyFrames().add(keyFrame);
                        timeline.play();
                    });
                    wejscie();

                    Thread.sleep(4000); // Czas spędzony w ulu
                    ustawCzerwonaPszczole();

                    // Stwórz nowy latch przed wyjściem
                    wyjscieLatch = new CountDownLatch(1);
                    ul.wyjdzZula(this);
                    wyjscieLatch.await();

                    ustawNormalnaPszczole();
                   // Thread.sleep(4000);
                }

                // Zbieranie nektaru lub jakas praca poza ulem, to można zakomentować i wszystko będzie działąć ale wtedy warto zmiejszyć czas składania jaj przez królową
                Thread.sleep(1000 + random.nextInt(2000));

                // Stwórz nowy latch przed wejściem
                wejscieLatch = new CountDownLatch(1);
                ul.wejsciDoUla(this);
                wejscieLatch.await();

                ustawNormalnaPszczole();

                // Śmierć pszczoły
                if(i == maxWejscDoUla-1) {
                    ustawMartwaPszczole();
                    ul.zgon(this);
                    break;
                }

                // Praca w ulu
                Thread.sleep(4000);
                ustawCzerwonaPszczole();

                // Stwórz nowy latch przed wyjściem
                wyjscieLatch = new CountDownLatch(1);
                ul.wyjdzZula(this);
                wyjscieLatch.await();

                ustawNormalnaPszczole();
                //Thread.sleep(4000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> panel.getChildren().remove(pszczola));
                break;
            }
        }
    }
}