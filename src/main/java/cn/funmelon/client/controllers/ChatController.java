package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
import cn.funmelon.client.FileInteraction;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

public class ChatController implements Initializable, Runnable {

    // user id
    private static String userId;
    // user name
    private static String userName;
    // friend 's all information
    private static Map<String, String> friend;
    // running status
    public static boolean isRunning;
    // refresh
    public static boolean isRefresh;
    // date format
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private TextArea mainTextArea;
    @FXML
    private TextArea sendTextArea;
    @FXML
    private Label friendNameLabel;
    @FXML
    private Button sendButton;

    public static void setUser(Map<String, String> user) {
        userId = user.get("user_id");
        userName = user.get("user_name");
    }

    public static void setFriend(Map<String, String> friend) {
        ChatController.friend = friend;
        System.out.println(friend);
    }

    public void sendButtonOnAction() {
        String str = sendTextArea.getText();
        if (!str.equals("")) {
            String date = dateFormat.format(new Date());
            String msg = String.format("""
                    #%s#
                    %s: %s
                    
                    """, date, userName, str);
            Client.sendMessage(msg, userId, friend.get("user_id"));

            sendTextArea.setText("");
            FileInteraction.storage(friend.get("user_id"), msg.getBytes(StandardCharsets.UTF_8));
            ChatController.isRefresh = true;
        }
    }

    public void clearButtonOnAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("清除消息");
        alert.setHeaderText("警告");
        alert.setContentText("清除后将无法恢复，真的很久。");
        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        alert.show();
        ok.setOnAction(event -> {
            FileInteraction.clear(friend.get("user_id"));
            mainTextArea.setText(FileInteraction.read(friend.get("user_id")));
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // message log
        friendNameLabel.setText(String.format("与%s聊天中...", friend.get("user_name")));
        // you can 't edit it!
        // mainTextArea.setText(FileInteraction.read(friend.get("user_id")));
        ChatController.isRefresh = true;
        sendTextArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                KeyCode kc = keyEvent.getCode();
                if (kc.equals(KeyCode.ENTER) && !sendButton.isDisable()) {
                    sendButtonOnAction();
                }
            }
        });
        resetThread();
    }
    // listen to the information of friends
    @Override
    public void run() {
        while (ChatController.isRunning) {
            try {
                // TODO: wait?
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
            if (ChatController.isRefresh) {
                Platform.runLater(() -> mainTextArea.setText(FileInteraction.read(friend.get("user_id"))));
                if (friend.get("status").equals("0")) {
                    sendButton.setDisable(true);
                } else {
                    sendButton.setDisable(false);
                }
                ChatController.isRefresh = false;
            }
        }
    }

    public void resetThread() {
        isRunning = true;
        // receive message sub thread
        Thread receiveMessageThread = new Thread(this);
        // start!
        receiveMessageThread.start();
    }
}
