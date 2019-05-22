package ly.rqmana.huia.java.concurrent;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutionException;

public abstract class Task<V> extends javafx.concurrent.Task<V> {


    public final void start() {
        new Thread(this).start();
    }

    public V runAndGet() {
        run();
        try {
            return get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------------

    public Task<V> addOnSucceeded(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event);
        return this;
    }

    public Task<V> addOnCanceled(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event);
        return this;
    }

    public Task<V> addOnFailed(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event);
        return this;
    }

    public Task<V> addOnRunning(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, event);
        return this;
    }

    public Task<V> addOnScheduled(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_SCHEDULED, event);
        return this;
    }

    public Task<V> addOnComplete(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.ANY, event);
        return this;
    }
}