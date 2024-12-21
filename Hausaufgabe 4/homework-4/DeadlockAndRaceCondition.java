import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockAndRaceCondition {
    // Gemeinsame Ressourcen
    private static final Lock resourceA = new ReentrantLock();
    private static final Lock resourceB = new ReentrantLock();
    private static final Lock CounterLock = new ReentrantLock();
    private static int sharedCounter = 0;
    private static final ArrayList<Integer> liste = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new Task1());
        Thread thread2 = new Thread(new Task2());
        Thread thread3 = new Thread(new CounterTask("t3"));
        Thread thread4 = new Thread(new CounterTask("t4"));

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        //TODO hier fehlt noch was

        if (!thread3.isAlive() && !thread4.isAlive()) {
            //TODO Es sollte eine Liste von 1-40 ausgegeben werden
            System.out.println(liste);
        }

    }

    static class Task1 implements Runnable {
        @Override
        public void run() {
            try {
                //TODO Was geht hier schief
                System.out.println("Task1: Sperre Resource A");
                resourceA.lock();
                Thread.sleep(50); // Simulation von Arbeit
                System.out.println("Task1: Sperre Resource B");
                resourceB.lock();
                System.out.println("Task1: Arbeiten abgeschlossen");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                resourceA.unlock();
                resourceB.unlock();
            }
        }
    }

    static class Task2 implements Runnable {
        @Override
        public void run() {
            try {
                //TODO Was geht hier schief
                System.out.println("Task2: Sperre Resource B");
                resourceB.lock();
                Thread.sleep(50); // Simulation von Arbeit
                System.out.println("Task2: Sperre Resource A");
                resourceA.lock();
                System.out.println("Task2: Arbeiten abgeschlossen");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                resourceB.unlock();
                resourceA.unlock();
            }
        }
    }

    // Task, der Race Conditions verursacht
    static class CounterTask implements Runnable {
        CounterTask(String threadName) {
            this.threadName = threadName;
        }
        public String threadName;
        @Override
        public void run() {
            //TODO Hier muss die Resource geschützt werden
            for (int i = 0; i < 20; i++) {
                sharedCounter = sharedCounter + 1;
                liste.add(sharedCounter);
            }
        }
    }
}
