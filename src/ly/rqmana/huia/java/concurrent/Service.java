package ly.rqmana.huia.java.concurrent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutionException;

public class Service<V> extends javafx.concurrent.Service<V> {

    ObjectProperty<Task<V>> task = new SimpleObjectProperty<>();

    public Service(Task<V> task) {
        super();
        setTask(task);
    }


    public Task getTask() {
        return task.get();
    }

    public ObjectProperty<Task<V>> taskProperty() {
        return task;
    }

    public void setTask(Task task) {
        this.task.set(task);
    }

    @Override
    protected Task<V> createTask() {
        return getTask();
    }


    public V runAndGet() {
        getTask().run();
        try {
            return (V)getTask().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------------

    public Service<V> addOnSucceeded(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event);
        return this;
    }

    public Service<V>  addOnCanceled(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event);
        return this;
    }

    public Service<V>  addOnFailed(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event);
        return this;
    }

    public Service<V>  addOnRunning(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, event);
        return this;
    }

    public Service<V>  addOnScheduled(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_SCHEDULED, event);
        return this;
    }

    public Service<V>  addOnComplete(EventHandler<WorkerStateEvent> event) {
        addEventHandler(WorkerStateEvent.ANY, event);
        return this;
    }
}
