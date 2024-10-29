package util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;

import static util.ClassUtils.getClassFiles;
import static util.ClassUtils.getClasses;

public class Scheduler {

    public static final ScheduledExecutorService WORKER_POOL = Executors.newScheduledThreadPool(2);
    public static final Cleaner CLEANER = Cleaner.create();

    public static final Scheduler INSTANCE = new Scheduler(WORKER_POOL, CLEANER);

    private final ScheduledExecutorService workerPool;
    private final Cleaner cleaner;

    public Scheduler(ScheduledExecutorService workerPool, Cleaner cleaner) {
        this.workerPool = workerPool;
        this.cleaner = cleaner;
    }

    public void scheduleStatic() throws IOException {
        List<File> classFiles = new ArrayList<>();
        Enumeration<URL> roots = ClassLoader.getSystemClassLoader().getResources("");
        while (roots.hasMoreElements()) {
            URL root = roots.nextElement();
            File file = new File(root.getPath());
            classFiles.addAll(getClassFiles(file));
        }
        List<Class<?>> classes = getClasses(classFiles);
        for (Class<?> cls : classes) {
            schedule(null, cls);
        }
    }

    public Cleaner.Cleanable schedule(Object invokingObject, Class<?> cls) {
        List<RunnableScheduledFuture<?>> scheduledFutures = new ArrayList<>();
        for (Method method : cls.getDeclaredMethods()) {
            if (invokingObject == null && !Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.isAnnotationPresent(Schedule.class)) {
                Schedule annotation = method.getAnnotation(Schedule.class);
                WeakReference<Object> reference = invokingObject == null ? null : new WeakReference<>(invokingObject);
                RunnableScheduledFuture<?> scheduledFuture = (RunnableScheduledFuture<?>) workerPool.scheduleAtFixedRate(() -> {
                    try {
                        if (reference == null) {
                            // Static method
                            method.invoke(null);
                        } else {
                            // Instance method, check if instance is still reachable
                            if (reference.get() != null) {
                                method.invoke(reference.get());
                            }
                        }
                    } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                }, 0, annotation.interval(), annotation.timeUnit());
                scheduledFutures.add(scheduledFuture);
            }
        }
        if (invokingObject == null) {
            return null;
        } else {
            // Register for cleanup if invokingObject != null
            return cleaner.register(invokingObject, () -> scheduledFutures.forEach(scheduledFuture -> scheduledFuture.cancel(true)));
        }
    }

    public void shutdown() {
        workerPool.shutdown();
    }

}
