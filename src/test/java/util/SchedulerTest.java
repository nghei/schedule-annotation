package util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class SchedulerTest {

    @Mock
    private ScheduledExecutorService workerPool;

    @Mock
    private Cleaner cleaner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testScheduleStatic() throws IOException {
        Scheduler scheduler = new Scheduler(workerPool, cleaner);
        scheduler.scheduleStatic();
        verify(workerPool, atLeast(1)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        verifyNoInteractions(cleaner);
    }

    @Test
    void testScheduleInstanceMethods() {
        Scheduler scheduler = new Scheduler(workerPool, cleaner);
        TestObject testObject1 = new TestObject("Test", 8, 0, scheduler);
        TestObject testObject2 = new TestObject("Test", 8, 0, scheduler);
        TestObject testObject3 = new TestObject("Test", 8, 0, scheduler);
        verify(workerPool, times(6)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        verify(cleaner).register(eq(testObject1), any(Runnable.class));
        verify(cleaner).register(eq(testObject2), any(Runnable.class));
        verify(cleaner).register(eq(testObject3), any(Runnable.class));
    }

    @Test
    @Disabled
    void testScheduleLongRunningMethods() throws InterruptedException {
        int m = 10;
        int n = 64;
        int size = 32 * 1024;
        int p = 4;
        long sleepTime = 30000;
        ScheduledExecutorService workerPool = Executors.newScheduledThreadPool(2);
        Cleaner cleaner = Cleaner.create();
        Scheduler scheduler = new Scheduler(workerPool, cleaner);
        createAndNullifyTestObjects(new TestObject[n], m, n, size, p, sleepTime, scheduler);
    }

    @Test
    @Disabled
    void testSmallObjects() throws InterruptedException {
        int m = 10;
        int n = 4096;
        int size = 32 * 1024;
        int p = 4;
        long sleepTime = 0;
        ScheduledExecutorService workerPool = Executors.newScheduledThreadPool(2);
        Cleaner cleaner = Cleaner.create();
        Scheduler scheduler = new Scheduler(workerPool, cleaner);
        createAndNullifyTestObjects(new TestObject[n], m, n, size, p, sleepTime, scheduler);
    }

    @Test
    @Disabled
    void testLargeObjects() throws InterruptedException {
        int m = 10;
        int n = 64;
        int size = 128 * 1024 * 1024;
        int p = 4;
        long sleepTime = 0;
        ScheduledExecutorService workerPool = Executors.newScheduledThreadPool(2);
        Cleaner cleaner = Cleaner.create();
        Scheduler scheduler = new Scheduler(workerPool, cleaner);
        createAndNullifyTestObjects(new TestObject[n], m, n, size, p, sleepTime, scheduler);
    }

    private void createAndNullifyTestObjects(TestObject[] testObjects, int m, int n, int size, int p, long sleepTime, Scheduler scheduler) throws InterruptedException {
        ExecutorService executorService = Executors.newWorkStealingPool(p);
        for (int iteration = 0; iteration < m; ++iteration) {
            for (int i = 0; i < n; ++i) {
                int state = iteration * n + i;
                executorService.submit(() -> testObjects[state] = new TestObject(String.valueOf(state), size, sleepTime, scheduler));
            }
            Thread.sleep(5000);
            for (int i = 0; i < n; ++i) {
                testObjects[i] = null;
            }
            System.gc();
            System.out.println("GC!");
            Thread.sleep(5000);
        }
    }

    @Schedule
    public static void someMethod() {
        // No-op
    }

}