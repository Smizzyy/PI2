import java.util.List;

public class Sheduler {
    public static void main(String[] args) {
        int timePerRound = 100;
        //TODO erstellen Sie 5 prozesse und fügen sie diese der Liste hinzu
        List<Prozess> prozesse = List.of(new Prozess[]{});
        roundRobin(prozesse, timePerRound);
    }

    private static void roundRobin(List<Prozess> prozesse, int timePerRound) {
        //TODO implementieren Sie hier den Round-Robin Algorithmus
    }
}
