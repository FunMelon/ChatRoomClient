package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    // listen to the event when push the "login in" button.
    public void loginButtonOnAction() {
        if (!usernameTextField.getText().isBlank() && !passwordTextField.getText().isBlank()) {
            Map user = Client.login(usernameTextField.getText(), passwordTextField.getText());
            // read the stage and set new stage
            if (user != null) {
                if (user.get("result").equals(-1)) {
                    loginMessageLabel.setText("您的账号或者密码错误!");
                    return;
                }
                Stage stage = (Stage)cancelButton.getScene().getWindow();
                try {
                    ListController.setStage(stage);
                    ListController.setUser(user);
                    Scene swiftScene = new Scene(new FXMLLoader(getClass().getResource("/list-view.fxml")).load()
                            , 350, 450);
                    stage.setScene(swiftScene);
                    Client client = new Client();
                    client.resetThread();
                } catch (Exception e) {
                    System.out.println("页面切换失败");
                }
            } else {
                loginMessageLabel.setText("无法连接至服务器!");
            }
        } else {
            loginMessageLabel.setText("请输入用户名或密码！");
        }
    }
    // listen to the event when push the "cancel" button.
    public void cancelButtonOnAction() {
        Stage stage = (Stage)cancelButton.getScene().getWindow();
        stage.close();
    }
    //  listen to the event when push the "forget" button.
    public void ForgetButtonOnAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("开发者懒癌患了");
        alert.setContentText("请联系服务器管理员修改密码");
        alert.show();
    }

    // listen to the event when push the "register" button.
    public void RegisterButtonOnAction() {
        System.out.println("我要注册");
    }

    // cloakingCheckBox
    public void CloakingCheckBoxOnAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("开发者摆烂了");
        alert.setContentText("这就是个装饰");
        alert.show();
    }
}