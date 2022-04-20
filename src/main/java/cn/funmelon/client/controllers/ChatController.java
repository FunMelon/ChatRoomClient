package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

public class ChatController implements Initializable, Runnable {
    // stage
    static private Stage stage;
    // user id
    private static String userId;
    // user name
    private static String userName;
    // friend 's all information
    private static Map<String, String> friend;
    // running status
    public static boolean isRunning = true;
    // message log
    private StringBuffer infoLog;
    // receive message thread
    private Thread receiveMessageThread;
    // date format
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    }

    public static void setUser(Map<String, String> user) {
        userId = user.get("user_id");
        userName = user.get("user_name");
    }

    public static void setFriend(Map<String, String> friend) {
        ChatController.friend = friend;
    }

    public void sendButtonOnAction(ActionEvent event) {
        String str = sendTextArea.getText();
        if (!str.equals("")) {
            String date = dateFormat.format(new Date());
            String msg = String.format("#%s#" + "\n" + "%s: %s", date, userName, str);
            sendTextArea.setText("");
            mainTextArea.appendText(msg + "\n\n");

            Client.sendMessage(msg, userId, friend.get("user_id"));
        }
    }

    public void exitButtonOnAction(ActionEvent event) {
        // TODO exit the window
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.infoLog = new StringBuffer();
        friendNameLabel.setText(String.format("与%s聊天中...", friend.get("user_name")));
        // you can 't edit it!
        mainTextArea.cancelEdit();
        resetThread();
//        stage = (Stage) mainTextArea.getScene().getWindow();
//        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                System.out.print("监听到chat窗口关闭");
//                isRunning = false;
//            }
//        });
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (ChatController.isRunning) {
            try {
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length, address, Client.SERVER_PORT);
                Client.socket.receive(packet);
                String str = new String(buffer, 0, packet.getLength());
                System.out.println("从服务器接受到： " + str);

                JSONObject jsonObject = new JSONObject(str);
                String msg = (String)jsonObject.get("message");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        mainTextArea.appendText(msg + "\n\n");
                    }
                });
            } catch (Exception e) {
                //System.out.printf("未接受到%s发送消息\n", friend.get("user_name"));
                //e.printStackTrace();
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
