package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    BorderPane loginPane;
    @FXML
    private Button cancelButton;
    @FXML
    private Label loginMessageLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //TODO
    }

    // listen the event when push the "login in" button.
    public void loginButtonOnAction(ActionEvent event) {
        if (!usernameTextField.getText().isBlank() && !passwordTextField.getText().isBlank()) {
            loginMessageLabel.setText("��¼��...");
            Map user = Client.login(usernameTextField.getText(), passwordTextField.getText());
            // read the stage and set new stage
            if (user != null) {
                Stage stage = (Stage)cancelButton.getScene().getWindow();
                try {
                    ListController.setStage(stage);
                    ListController.setUser(user);
                    Scene swiftScene = new Scene(new FXMLLoader(getClass().getResource("/list-view.fxml")).load()
                            , 350, 700);
                    stage.setScene(swiftScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                loginMessageLabel.setText("�����˺Ż����������!");
            }
        } else {
            loginMessageLabel.setText("�������û��������룡");
        }
    }
    // listen the event when push the "cancel" button.
    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage)cancelButton.getScene().getWindow();
        stage.close();
    }
}