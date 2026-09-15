import java.util.concurrent.Semaphore;

public class ReaderWriter {

    // Actual shared resource
    static Semaphore resource = new Semaphore(1);

    // Gate that writers can close to block new readers
    static Semaphore readTry = new Semaphore(1);

    // Protects read_count
    static Semaphore rmutex = new Semaphore(1);

    // Protects write_count
    static Semaphore wmutex = new Semaphore(1);

    // Global queue / turnstile
    static Semaphore serviceQueue = new Semaphore(1);

    // Number of active readers
    static int read_count = 0;

    // Number of waiting/active writers
    static int write_count = 0;


    public static void main(String[] args) {

        Thread reader1 = new Thread(() -> reader(), "Reader 1");
        Thread reader2 = new Thread(() -> reader(), "Reader 2");
        Thread writer1 = new Thread(() -> writer(), "Writer 1");
        Thread reader3 = new Thread(() -> reader(), "Reader 3");
        Thread writer2 = new Thread(() -> writer(), "Writer 2");

        reader1.start();
        reader2.start();
        writer1.start();
        reader3.start();
        writer2.start();
    }


    // ---------------- READER ----------------

    static void reader() {

        while (true) {

            try {

                // Get in the global queue
                serviceQueue.acquire();

                // Block if a writer has closed the gate
                readTry.acquire();

                // Let the next thread enter the queue
                serviceQueue.release();

                // Protect read_count
                rmutex.acquire();

                read_count++;

                // First reader locks the resource
                if (read_count == 1) {
                    resource.acquire();
                }

                rmutex.release();

                // Pass the gate
                readTry.release();


                // -------- CRITICAL SECTION --------

                System.out.println(
                    Thread.currentThread().getName() + " is reading"
                );

                Thread.sleep(500);


                // -------- READER LEAVES --------

                rmutex.acquire();

                read_count--;

                // Last reader releases the resource
                if (read_count == 0) {
                    resource.release();
                }

                rmutex.release();


                // Think / wait before reading again
                Thread.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    // ---------------- WRITER ----------------

    static void writer() {

        while (true) {

            try {

                // Protect write_count
                wmutex.acquire();

                write_count++;

                // First writer closes reader gate
                if (write_count == 1) {
                    readTry.acquire();
                }

                wmutex.release();


                // Get into the same global queue
                serviceQueue.acquire();

                // Writer needs exclusive access
                resource.acquire();

                // Let next thread enter queue
                serviceQueue.release();


                // -------- CRITICAL SECTION --------

                System.out.println(
                    Thread.currentThread().getName() + " is writing"
                );

                Thread.sleep(500);

                System.out.println(
                    Thread.currentThread().getName() + " finished writing"
                );


                // -------- WRITER LEAVES --------

                resource.release();

                wmutex.acquire();

                write_count--;

                // Last writer opens reader gate
                if (write_count == 0) {
                    readTry.release();
                }

                wmutex.release();


                // Think / wait before writing again
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}