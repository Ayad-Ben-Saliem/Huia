package ly.rqmana.huia.java.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.*;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketClient extends org.java_websocket.client.WebSocketClient {


    private ObservableSet<EventHandler<OpenEvent>> onOpenEvents = FXCollections.observableSet();
    private ObservableSet<EventHandler<CloseEvent>> onCloseEvents  = FXCollections.observableSet();
    private ObservableSet<EventHandler<ReceiveEvent>> onReceiveEvents = FXCollections.observableSet();
    private ObservableSet<EventHandler<ErrorEvent>> onErrorEvents = FXCollections.observableSet();

    public WebSocketClient(String uri) throws URISyntaxException {
        this(new URI(uri));
    }

    public WebSocketClient(URI uri) {
        super(uri);
    }

    public WebSocketClient(URI uri, Draft draft) {
        super(uri, draft);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        OpenEvent openEvent = new OpenEvent(handshakeData);
        onOpenEvents.forEach(openEventEventHandler -> openEventEventHandler.handle(openEvent));
    }

    @Override
    public void onMessage(String message) {
        ReceiveEvent receiveEvent = new ReceiveEvent(message);
        onReceiveEvents.forEach(receiveEventEventHandler -> receiveEventEventHandler.handle(receiveEvent));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        CloseEvent closeEvent = new CloseEvent(code, reason, remote);
        onCloseEvents.forEach(closeEventEventHandler -> closeEventEventHandler.handle(closeEvent));
    }

    @Override
    public void onError(Exception ex) {
        ErrorEvent errorEvent = new ErrorEvent(ex);
        onErrorEvents.forEach(errorEventEventHandler -> errorEventEventHandler.handle(errorEvent));
    }

    public static class OpenEvent extends Event {

        private final ServerHandshake serverHandshake;

        public static final EventType<OpenEvent> OPEN = new EventType<>(Event.ANY, "OPEN");

        public OpenEvent(ServerHandshake serverHandshake) {
            super(OPEN);
            this.serverHandshake = serverHandshake;
        }

        public ServerHandshake getServerHandshake() {
            return serverHandshake;
        }
    }

    public static class CloseEvent extends Event {

        private final int code;
        private final String reason;
        private final boolean remote;

        public static final EventType<CloseEvent> CLOSE = new EventType<>(Event.ANY, "CLOSE");

        public CloseEvent(int code, String reason, boolean remote) {
            super(CLOSE);
            this.code = code;
            this.reason = reason;
            this.remote= remote;
        }

        public int getCode() {
            return code;
        }

        public String getReason() {
            return reason;
        }

        public boolean isRemote() {
            return remote;
        }
    }

    public static class ReceiveEvent extends Event {

        private final String message;

        public static final EventType<ReceiveEvent> RECEIVE = new EventType<>(Event.ANY, "RECEIVE");

        public ReceiveEvent(String message) {
            super(RECEIVE);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ErrorEvent extends Event {

        private final Exception exception;

        public static final EventType<ErrorEvent> ERROR = new EventType<>(Event.ANY, "ERROR");

        public ErrorEvent(Exception exception) {
            super(ERROR);
            this.exception = exception;
        }

        public Exception getMessage() {
            return exception;
        }
    }

    public void setOnOpenEvent(EventHandler<OpenEvent> onOpenEvent) {
        this.onOpenEvents.clear();
        this.onOpenEvents.add(onOpenEvent);
    }

    public void setOnCloseEvent(EventHandler<CloseEvent> onCloseEvent) {
        this.onCloseEvents.clear();
        this.onCloseEvents.add(onCloseEvent);
    }

    public void setOnReceiveEvent(EventHandler<ReceiveEvent> onReceiveEvent) {
        this.onReceiveEvents.clear();
        this.onReceiveEvents.add(onReceiveEvent);
    }

    public void setOnErrorEvent(EventHandler<ErrorEvent> onErrorEvent) {
        this.onErrorEvents.clear();
        this.onErrorEvents.add(onErrorEvent);
    }

    public void addOnOpenEvent(EventHandler<OpenEvent> onOpenEvent) {
        this.onOpenEvents.add(onOpenEvent);
    }

    public void addOnCloseEvent(EventHandler<CloseEvent> onCloseEvent) {
        this.onCloseEvents.add(onCloseEvent);
    }

    public void addOnReceiveEvent(EventHandler<ReceiveEvent> onReceiveEvent) {
        System.out.println(onReceiveEvent);
        this.onReceiveEvents.add(onReceiveEvent);
    }

    public void addOnErrorEvent(EventHandler<ErrorEvent> onErrorEvent) {
        this.onErrorEvents.add(onErrorEvent);
    }
}