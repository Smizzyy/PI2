import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Sheduler {
    public static void main(String[] args) {
        int timePerRound = 100;
        //TODO erstellen Sie 5 prozesse und fügen sie diese der Liste hinzu
        Prozess prozess1 = new Prozess("A", 500);
        Prozess prozess2 = new Prozess("B", 200);
        Prozess prozess3 = new Prozess("C", 300);
        Prozess prozess4 = new Prozess("D", 100);
        Prozess prozess5 = new Prozess("E", 400);
        List<Prozess> prozesse = List.of(new Prozess[]{prozess1, prozess2, prozess3, prozess4, prozess5});
        roundRobin(prozesse, timePerRound);
    }

    private static void roundRobin(List<Prozess> prozesse, int timePerRound) {
        //TODO implementieren Sie hier den Round-Robin Algorithmus
        Queue<Prozess> roundRobinQueue = new LinkedList<>();
        int timePassed = 0;

        for (Prozess prozess : prozesse) roundRobinQueue.add(prozess);

        while (!roundRobinQueue.isEmpty()) {
            Prozess prozess = roundRobinQueue.poll();
            timePassed += timePerRound; // zählt die gesamte Laufzeit eines Prozesses
            prozess.run(timePerRound);
            if (prozess.isCompleted()) {
                System.out.println("Prozess " + prozess.getName() + " Laufzeit: " + timePassed + " ms");
                continue;
            } else {
                roundRobinQueue.add(prozess);
            }
        }
    }
}
