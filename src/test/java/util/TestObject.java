package util;

public class TestObject {

    private final String state;
    private final long sleepTime;
    private byte[] bytes;

    public TestObject(String state, int size, long sleepTime, Scheduler scheduler) {
        this.state = state;
        this.bytes = new byte[size];
        this.sleepTime = sleepTime;
        scheduler.schedule(this, TestObject.class);
    }

    @Schedule
    public void first() throws InterruptedException {
        Thread.sleep(sleepTime);
        System.out.println(String.format("%s::first with state - %s", getClass().getSimpleName(), state));
    }

    @Schedule
    public void second() throws InterruptedException {
        Thread.sleep(sleepTime);
        System.out.println(String.format("%s::second with state - %s", getClass().getSimpleName(), state));
    }

}