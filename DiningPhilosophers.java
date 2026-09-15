import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DiningPhilosophers {

    static final int N = 5;
    static Semaphore[] chopstick = new Semaphore[N];

    static Semaphore mutex = new Semaphore(N - 1);

    public static void main(String[] args) {

        for (int i = 0; i < N; i++) {
            chopstick[i] = new Semaphore(1);
        }

        for (int i = 0; i < N; i++) {
            new Thread(new Philosopher(i)).start();
        }
    }

    static class Philosopher implements Runnable {

        int id;

        Philosopher(int id) {
            this.id = id;
        }

        public void run() {

            while (true) {

                System.out.println("Philosopher " + id + " is thinking...");
                sleep(1);

                try {
                    // Allow only N-1 philosophers to compete
                    mutex.acquire();

                    // Pick left chopstick
                    chopstick[id].acquire();

                    try {
                        // Pick right chopstick
                        chopstick[(id + 1) % N].acquire();

                        try {
                            System.out.println(
                                "Philosopher " + id + " is eating..."
                            );

                            sleep(2);

                        } finally {
                            // Release right chopstick
                            chopstick[(id + 1) % N].release();
                        }

                    } finally {
                        // Release left chopstick
                        chopstick[id].release();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;

                } finally {
                    // Release mutex
                    mutex.release();
                }
            }
        }

        static void sleep(int seconds) {
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}