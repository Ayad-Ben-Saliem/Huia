package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.*;

public class HomeWindowController implements Controllable {
    public Label companyName;
    public JFXButton loginBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onLoginBtnClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(Res.Fxml.LOGIN_WINDOW.getUrl(), Utils.getBundle());
            Pane rootPane = loader.load();
            JFXDialog dialog = new JFXDialog(getMainController().getRootStack(), rootPane, JFXDialog.DialogTransition.CENTER, true);
            LoginWindowController controller = loader.getController();
            controller.cancelBtn.setOnAction(event -> dialog.close());
            controller.loginBtn.setOnAction(event -> {
                new Thread(() -> {
                    controller.formContainer.setDisable(true);
//                    controller.spinner.setVisible(true);
                    try {
                        HttpClient client = HttpClients.createDefault();
                        HttpPost post = new HttpPost("http://huia.herokuapp.com/checkUser/");

                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("username", controller.usernameTF.getText()));
                        urlParameters.add(new BasicNameValuePair("password", controller.passwordTF.getText()));

                        post.setEntity(new UrlEncodedFormEntity(urlParameters));

                        HttpEntity entity = client.execute(post).getEntity();
                        String response = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);

                        System.out.println(response);
                        if (response.equals("Yes")) {
                            dialog.close();

                            MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();
                            Platform.runLater(() -> mainWindowController.lock(false));

                        } else {
                            controller.formContainer.setDisable(false);
                            controller.spinner.setVisible(false);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            });

            dialog.show();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
