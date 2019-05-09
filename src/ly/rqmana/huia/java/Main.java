package ly.rqmana.huia.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        Logger.getLogger("org.apache.catalina.core").setLevel(Level.OFF);

        Path dbPath = Utils.getInstallationPath();
        assert dbPath != null;
        if (!Files.notExists(dbPath)) {
            Windows.INSTALLATION_WINDOW.open();
            Windows.INSTALLATION_WINDOW.setOnHiding(event -> {
                Utils.closeWebSocket();
            });
        } else {
            Windows.MAIN_WINDOW.open();
        }

//        try {
//            WebSocketClient client = new WebSocketClient("ws://huia.herokuapp.com//");
//            client.connect();
//            new Thread(() -> {
//                try {
//                    while (!client.isOpen()) {
//                        Thread.sleep(1000);
//                    }
////                    client.send("Hello from Java");
//                    EventHandler<WebSocketClient.ReceiveEvent> eventHandler = new EventHandler<WebSocketClient.ReceiveEvent>() {
//                        @Override
//                        public void handle(WebSocketClient.ReceiveEvent event) {
//                            System.out.println(event.getMessage());
//                        }
//                    };
//                    System.out.println("eventHandler: " + eventHandler);
//                    client.addOnReceiveEvent(eventHandler);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        Windows.MAIN_WINDOW.setOnCloseRequest(event -> {
//            MainWindowController controller = Windows.MAIN_WINDOW.getController();
//            controller.getWindowEventHandler().handle(event);
//        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
