package ly.rqmana.huia.java.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Threading {

    public static final ScheduledExecutorService REGISTRATION_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);
    public static final ScheduledExecutorService CAPTURING_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);
    public static final ExecutorService MAIN_EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    public static final ScheduledExecutorService TIME_COUNT_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);


    public static void shutdown(){
        REGISTRATION_EXECUTOR_SERVICE.shutdown();
        CAPTURING_EXECUTOR_SERVICE.shutdown();
        SCHEDULED_EXECUTOR_SERVICE.shutdown();
        MAIN_EXECUTOR_SERVICE.shutdown();
        TIME_COUNT_SERVICE.shutdown();
    }
}
