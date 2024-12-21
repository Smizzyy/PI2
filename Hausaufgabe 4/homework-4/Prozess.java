public class Prozess {
    private final String name;
    private int remainingTime;

    public Prozess(String name, int executionTime) {
        this.name = name;
        this.remainingTime = executionTime;
    }

    public void run(int timeSlice) {
        if (remainingTime <= 0) {
            System.out.println(name + " ist bereits abgeschlossen.");
            return;
        }

        int actualRunTime = Math.min(timeSlice, remainingTime);
        System.out.println(name + " läuft für " + actualRunTime + " ms.");

        try {
            Thread.sleep(actualRunTime);
        } catch (InterruptedException e) {
            System.err.println(name + " wurde unterbrochen.");
        }

        remainingTime -= actualRunTime;

        if (remainingTime <= 0) {
            System.out.println(name + " wurde abgeschlossen.");
        } else {
            System.out.println(name + " verbleibende Zeit: " + remainingTime + " ms.");
        }
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return remainingTime <= 0;
    }
}
