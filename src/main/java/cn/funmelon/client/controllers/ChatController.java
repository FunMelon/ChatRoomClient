package cn.funmelon.client.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    // stage
    static private Stage stage;
    // user id
    private String userId;
    // friend id
    private String friendId;
    // friend name
    private String friendName;
    // message log
    private StringBuffer infoLog;
    // receive message thread
    private Thread receiveMessageThread;
    // date format
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private Button sendButton;
    @FXML
    private Button exitButton;
    @FXML
    private TextArea mainTextArea;
    @FXML
    private TextArea sendTextArea;
    @FXML
    private Label friendNameLabel;

    public static void setStage(Stage stage) {
        ChatController.stage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("监听到chat窗口关闭");
            }
        });
    }

    public void setUser(Map<String, String> user) {
        this.userId = user.get("user_id");
    }

    public void setFriend(Map<String, String> friend) {
        this.friendId = friend.get("user_id");
        this.friendName = friend.get("user_name");
    }

    public void sendButtonOnAction(ActionEvent event) {
        if (!sendTextArea.getText().equals("")) {
            // TODO send message
        }
    }

    public void exitButtonOnAction(ActionEvent event) {
        // TODO exit the window
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.infoLog = new StringBuffer();
        friendNameLabel.setText(String.format("与%s聊天中...", this.friendName));
        // you can 't edit it!
        mainTextArea.cancelEdit();
    }
}
