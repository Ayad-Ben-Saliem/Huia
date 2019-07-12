package ly.rqmana.huia.java.event;

import static javafx.scene.input.MouseEvent.*;
import static org.reactfx.EventStreams.*;

import java.time.Duration;

import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.reactfx.EventStream;
import org.reactfx.Subscription;
import org.reactfx.util.Either;

public class MouseStationaryHelper {
    private final Node node;
    private final EventStream<Either<Point2D, Void>> stationaryEvents;

    private Subscription installed = null;

    public MouseStationaryHelper(Node node, Duration delay) {
        this.node = node;
        EventStream<MouseEvent> mouseEvents = eventsOf(node, MouseEvent.ANY);
        EventStream<Point2D> stationaryPositions = mouseEvents
                .successionEnds(delay)
                .filter(e -> e.getEventType() == MOUSE_MOVED)
                .map(e -> new Point2D(e.getX(), e.getY()));
        EventStream<Void> stoppers = mouseEvents.supply((Void) null);
        stationaryEvents = stationaryPositions.or(stoppers).distinct();
    }

    public EventStream<Either<Point2D, Void>> events() {
        return stationaryEvents;
    }

    public void install() {
        if(installed == null) {
            installed = stationaryEvents.<Event>map(either -> either.unify(
                    pos -> MouseStationaryEvent.beginAt(node.localToScreen(pos)),
                    stop -> MouseStationaryEvent.end()))
                .subscribe(evt -> Event.fireEvent(node, evt));
        }
    }

    public void uninstall() {
        if(installed != null) {
            installed.unsubscribe();
            installed = null;
        }
    }
}