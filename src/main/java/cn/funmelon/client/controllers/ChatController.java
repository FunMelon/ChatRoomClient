package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
import cn.funmelon.client.FileInteraction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
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
    public static boolean isRunning = true;
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
            mainTextArea.setText(FileInteraction.read(friend.get("user_id")));
        }
    }

    public void clearButtonOnAction() {
        FileInteraction.clear(friend.get("user_id"));
        mainTextArea.setText(FileInteraction.read(friend.get("user_id")));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // message log
        friendNameLabel.setText(String.format("与%s聊天中...", friend.get("user_name")));
        // you can 't edit it!
        mainTextArea.setText(FileInteraction.read(friend.get("user_id")));
        System.out.println("好友的状态是" + friend.get("status"));
        if (friend.get("status").equals("0")) {
            sendButton.setDisable(true);
        } else {
            sendButton.setDisable(false);
        }
        resetThread();
    }

    // listen to the information of friends
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (ChatController.isRunning) {
            try {
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length, address, Client.SERVER_PORT);
                Client.socket.receive(packet);
                String str = new String(buffer, 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("从服务器接受到： " + str);

                JSONObject jsonObject = new JSONObject(str);
                String msg = (String)jsonObject.get("message");
                Platform.runLater(() -> {
                    FileInteraction.storage(friend.get("user_id"), msg.getBytes(StandardCharsets.UTF_8));
                    mainTextArea.setText(FileInteraction.read(friend.get("user_id")));
                });
            } catch (Exception ignored) {
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
