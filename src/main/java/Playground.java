import objects.PlaygroundObject;
import util.Schedule;
import util.Scheduler;

import java.io.IOException;

public class Playground {

    static {
        try {
            Scheduler.INSTANCE.scheduleStatic();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new PlaygroundObject("Obj", 8);
    }

    @Schedule
    public static void playground() {
        System.out.println("Playground");
    }

}
