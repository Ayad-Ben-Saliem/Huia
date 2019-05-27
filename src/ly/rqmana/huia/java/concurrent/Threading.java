package ly.rqmana.huia.java.concurrent;

import ly.rqmana.huia.java.util.Windows;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Threading {

    public static final ScheduledExecutorService REGISTRATION_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);
    public static final ScheduledExecutorService CAPTURING_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);

    static {
        Windows.MAIN_WINDOW.addOnCloseRequest(event -> REGISTRATION_EXECUTOR_SERVICE.shutdown());
        Windows.MAIN_WINDOW.addOnCloseRequest(event -> CAPTURING_EXECUTOR_SERVICE.shutdown());
    }

}
