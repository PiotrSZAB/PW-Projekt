import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import javafx.scene.image.ImageView;
import java.util.Random;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Ul {
    private final Semaphore miejscaWulu;
    private final int maksymalnaLiczbaPszczolWulu;

    // Pane do wyświetlania pszczół i jaj
    private final Pane panel;
    // Zmienna do zmiany pozycji jaj
    private final AtomicInteger zmiana = new AtomicInteger(0);
    // Flaga do zatrzymywania wszystkich operacji
    private volatile boolean zatrzymanieZadane = false;

    private final BlockingQueue<Pszczola> kolejkaWejscia = new LinkedBlockingQueue<>();
    private final BlockingQueue<Pszczola> kolejkaWyjscia = new LinkedBlockingQueue<>();

    // Lista jaj w ulu
    private final List<Circle> jaja = new CopyOnWriteArrayList<>();
    // Lista wątków do uruchamiania zadań pszczół
    private final List<Thread> watkiPszczol = new CopyOnWriteArrayList<>();
    // Maksymalna liczba wejść do ula
    private final int maxWejscDoUla;

    // ExecutorService dla lepszego zarządzania wątkami
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    // Worker threads dla ciągłego przetwarzania kolejek
    private volatile boolean robotnicyDzialaja = true;

    // Konstruktor
    public Ul(int maksymalnaLiczbaPszczolWulu, Pane panel, int maxWejscDoUla) {
        this.maksymalnaLiczbaPszczolWulu = maksymalnaLiczbaPszczolWulu;
        this.miejscaWulu = new Semaphore(maksymalnaLiczbaPszczolWulu);
        this.panel = panel;
        this.maxWejscDoUla = maxWejscDoUla;

        System.out.println("UL UTWORZONY: Maksymalna pojemnosc: " + maksymalnaLiczbaPszczolWulu + " pszczol");

        uruchomWatkiRobotnicze();
    }

    private void uruchomWatkiRobotnicze() {
        scheduler.execute(() -> {
            while (robotnicyDzialaja && !Thread.currentThread().isInterrupted()) {
                try {
                    Pszczola pszczola = kolejkaWejscia.take();
                    poruszPszczoleWejscie(pszczola, 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("WORKER WEJSCIA: Zatrzymany");
        });

        scheduler.execute(() -> {
            while (robotnicyDzialaja && !Thread.currentThread().isInterrupted()) {
                try {
                    Pszczola pszczola = kolejkaWyjscia.take();
                    poruszPszczoleWyjscie(pszczola, 2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("WORKER WYJSCIA: Zatrzymany");
        });
    }

    public void wejsciDoUla(Pszczola pszczola) {
        try {
            // Czekaj na miejsce w ulu
            miejscaWulu.acquire();

            int zajete = maksymalnaLiczbaPszczolWulu - miejscaWulu.availablePermits();
            System.out.println("WEJSCIE: Pszczola wchodzi do ula. W ulu: " +
                    zajete + "/" + maksymalnaLiczbaPszczolWulu);

            // Dodaj do kolejki wejścia
            if (!kolejkaWejscia.offer(pszczola)) {
                System.err.println("BŁĄD: Nie udało się dodać pszczoły do kolejki wejścia");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("BLAD: Przerwano oczekiwanie na wejscie do ula");
        }
    }

    public void wyjdzZula(Pszczola pszczola) {
        // Dodaj do kolejki wyjścia
        if (!kolejkaWyjscia.offer(pszczola)) {
            System.err.println("BŁĄD: Nie udało się dodać pszczoły do kolejki wyjścia");
        }

        miejscaWulu.release();

        int zajete = maksymalnaLiczbaPszczolWulu - miejscaWulu.availablePermits();
        System.out.println("WYJSCIE: Pszczola wychodzi z ula. W ulu: " +
                zajete + "/" + maksymalnaLiczbaPszczolWulu);
    }

    private void poruszPszczoleWejscie(Pszczola pszczola, int trasa) {
        Random rand = new Random();
        double[] targetsX = {600, 400, 20 + Math.random() * 430};
        double[] targetsY = {230, 230, rand.nextBoolean() ? 20 + Math.random() * 280 : 400 + Math.random() * 270};

        poruszajPszczolaDoPunktow(pszczola, targetsX, targetsY, 1, trasa);
    }

    private void poruszPszczoleWyjscie(Pszczola pszczola, int trasa) {
        Random rand = new Random();
        double[] targetsX = {400, 600, 600 + Math.random() * 350};
        double[] targetsY = {470, 470, rand.nextBoolean() ? 20 + Math.random() * 270 : 420 + Math.random() * 270};

        poruszajPszczolaDoPunktow(pszczola, targetsX, targetsY, 2, trasa);
    }

    public void poruszajPszczolaDoPunktow(Pszczola pszczola, double[] targetsX, double[] targetsY, int funkcja, int trasa) {
        if (zatrzymanieZadane || pszczola == null) {
            return;
        }

        AtomicInteger indeks = new AtomicInteger(0);
        ImageView obrazekPszczoly = pszczola.getBee();

        AnimationTimer timer = new AnimationTimer() {
            private double targetX = targetsX[0];
            private double targetY = targetsY[0];
            // Oblicz prędkość względem środka obrazka
            private double currentCenterX = obrazekPszczoly.getX() + obrazekPszczoly.getFitWidth()/2;
            private double currentCenterY = obrazekPszczoly.getY() + obrazekPszczoly.getFitHeight()/2;
            private double velocityX = (targetX - currentCenterX) / 70; // Jeśli mają sie szybciej poruszać to zmniejszyć np do 50 lub 35
            private double velocityY = (targetY - currentCenterY) / 70; // Jeśli mają sie szybciej poruszać to zmniejszyć np do 50 lub 35

            @Override
            public void handle(long now) {
                if (zatrzymanieZadane) {
                    stop();
                    return;
                }

                Platform.runLater(() -> {
                    if (zatrzymanieZadane) {
                        stop();
                        return;
                    }

                    // Przesuń obrazek pszczoły
                    obrazekPszczoly.setX(obrazekPszczoly.getX() + velocityX);
                    obrazekPszczoly.setY(obrazekPszczoly.getY() + velocityY);

                    // Sprawdź czy dotarła do celu (sprawdzaj środek obrazka)
                    double centerX = obrazekPszczoly.getX() + obrazekPszczoly.getFitWidth()/2;
                    double centerY = obrazekPszczoly.getY() + obrazekPszczoly.getFitHeight()/2;

                    if (Math.abs(centerX - targetX) < 1 && Math.abs(centerY - targetY) < 1) {
                        // Wycentruj na docelowej pozycji
                        obrazekPszczoly.setX(targetX - obrazekPszczoly.getFitWidth()/2);
                        obrazekPszczoly.setY(targetY - obrazekPszczoly.getFitHeight()/2);

                        if (indeks.incrementAndGet() < targetsX.length) {
                            // Przejdź do następnego punktu
                            targetX = targetsX[indeks.get()];
                            targetY = targetsY[indeks.get()];

                            // Przelicz nową prędkość z aktualnej pozycji
                            currentCenterX = obrazekPszczoly.getX() + obrazekPszczoly.getFitWidth()/2;
                            currentCenterY = obrazekPszczoly.getY() + obrazekPszczoly.getFitHeight()/2;
                            velocityX = (targetX - currentCenterX) / 70; // Jeśli mają sie szybciej poruszać to zmniejszyć np do 50 lub 35
                            velocityY = (targetY - currentCenterY) / 70; // Jeśli mają sie szybciej poruszać to zmniejszyć np do 50 lub 35
                        } else {
                            // Animacja zakończona
                            if (funkcja == 1) {
                                pszczola.wejscie();
                                // System.out.println("WESZLA: Pszczola dotarla do ula (TRASA " + trasa + ")");
                            } else {
                                pszczola.wyjscie();
                                // System.out.println("WYSZLA: Pszczola opuscila ul (TRASA " + trasa + ")");
                            }
                            stop();
                        }
                    }
                });
            }
        };
        timer.start();
    }

    public void zgon(Pszczola pszczola) {
        scheduler.schedule(() -> {
            Platform.runLater(() -> panel.getChildren().remove(pszczola.getBee()));

            miejscaWulu.release();

            int dostepneMiejsca = miejscaWulu.availablePermits();
            int zajete = maksymalnaLiczbaPszczolWulu - dostepneMiejsca;

            System.out.println("SMIERC: Pszczola zginela.         W ulu: " +
                    zajete + "/" + maksymalnaLiczbaPszczolWulu);

        }, 1, TimeUnit.SECONDS);
    }


    public void zlozJaja() {
        try {
            // Czekaj na miejsce w ulu
            miejscaWulu.acquire();

            int zajete = maksymalnaLiczbaPszczolWulu - miejscaWulu.availablePermits();
            System.out.println("JAJO: Krolowa sklada jajo.        W ulu: " +
                    zajete + "/" + maksymalnaLiczbaPszczolWulu);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (zatrzymanieZadane) {
            miejscaWulu.release();
            return;
        }

        int currentPos = zmiana.getAndUpdate(pozycja -> (pozycja + 15) >= 90 ? 0 : pozycja + 15);
        int X = 80;
        int Y = 310 + currentPos;

        Circle jajo = new Circle(5, Color.WHITE);
        jaja.add(jajo);

        Platform.runLater(() -> {
            if (!zatrzymanieZadane) {
                panel.getChildren().add(jajo);
                jajo.setCenterX(X);
                jajo.setCenterY(Y);
                jajo.setStroke(Color.BLACK);
                jajo.setStrokeWidth(1);
            }
        });

        // Wyklucie po 1 sekundzie, warto zmniejszyć jak się też zmniejszy czas składania jaj przez Krolową
        scheduler.schedule(() -> nowaPszczola(jajo, X, Y), 1, TimeUnit.SECONDS);
    }

    private void nowaPszczola(Circle jajo, int X, int Y) {
        if (zatrzymanieZadane) {
            return;
        }

        Platform.runLater(() -> {
            panel.getChildren().remove(jajo);
            jaja.remove(jajo);

            int aktualnyStanPrint = maksymalnaLiczbaPszczolWulu - miejscaWulu.availablePermits();

            // Utwórz nową pszczołę i uruchom jej wątek
            Thread watekPszczoly = new Thread(() -> new Pszczola(this, maxWejscDoUla, panel, true, X, Y).run());
            watkiPszczol.add(watekPszczoly);
            watekPszczoly.start();
        });
    }

    // Funkcja przerwania
    public void requestStop() {
        zatrzymanieZadane = true;
        robotnicyDzialaja = false;

        // Przerwanie wątków pszczół robotnic
        for (Thread watekPszczoly : watkiPszczol) {
            watekPszczoly.interrupt();
        }
        watkiPszczol.clear();

        // Zamknij scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public void usunJaja() {
        Platform.runLater(() -> {
            for (Circle jajo : jaja) {
                panel.getChildren().remove(jajo);
            }
            jaja.clear();
        });
    }
}