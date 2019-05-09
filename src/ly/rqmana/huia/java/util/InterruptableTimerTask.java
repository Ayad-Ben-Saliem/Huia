package ly.rqmana.huia.java.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.TimerTask;

public abstract class InterruptableTimerTask extends TimerTask {

    private final BooleanProperty finished = new SimpleBooleanProperty(false);

    public BooleanProperty finishedProperty() {
        return finished;
    }

    public boolean isFinished() {
        return finished.get();
    }

    public void setFinished(boolean finished) {
        this.finished.set(finished);
    }
}
