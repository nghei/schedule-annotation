package objects;

import util.Schedule;
import util.Scheduler;

import java.util.concurrent.TimeUnit;

public class PlaygroundObject {

    private final String state;
    private byte[] bytes;

    public PlaygroundObject(String state, int size) {
        this.state = state;
        this.bytes = new byte[size];
        Scheduler.INSTANCE.schedule(this, PlaygroundObject.class);
    }

    @Schedule(interval = 1, timeUnit = TimeUnit.SECONDS)
    public void first() {
        System.out.println(String.format("%s::first with state - %s", getClass().getSimpleName(), state));
    }

    @Schedule(interval = 2, timeUnit = TimeUnit.SECONDS)
    public void second() {
        System.out.println(String.format("%s::second with state - %s", getClass().getSimpleName(), state));
    }

}