import java.util.concurrent.locks.*;

public class ProducerConsumer {

    static int[] buffer = new int[5];
    static int in = 0, out = 0, count = 0;

    static Lock lock = new ReentrantLock();
    static Condition notFull = lock.newCondition();
    static Condition notEmpty = lock.newCondition();

    public static void main(String[] args) {

        Thread producer = new Thread(() -> produce());
        Thread consumer = new Thread(() -> consume());

        producer.start();
        consumer.start();
    }

    static void produce() {

        while (true) {

            int item = (int)(Math.random() * 100);

            lock.lock();

            try {

                // Buffer full
                while (count == 5) {
                    notFull.await();
                }

                // Add item
                buffer[in] = item;
                System.out.println("Produced: " + item);

                in = (in + 1) % 5;
                count++;

                // Tell consumer
                notEmpty.signal();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void consume() {

        while (true) {

            lock.lock();

            try {

                // Buffer empty
                while (count == 0) {
                    notEmpty.await();
                }

                // Remove item
                int item = buffer[out];
                System.out.println("Consumed: " + item);

                out = (out + 1) % 5;
                count--;

                // Tell producer
                notFull.signal();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}