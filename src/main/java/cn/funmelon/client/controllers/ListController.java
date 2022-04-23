package cn.funmelon.client.controllers;

import cn.funmelon.client.COMMAND;
import cn.funmelon.client.Client;
import cn.funmelon.client.FileInteraction;
import cn.funmelon.client.GUIEntrance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ListController implements Initializable, Runnable {

    // user 's all information
    static private Map user;
    // stage
    static private Stage stage;
    // friends
    static private List<Map<String, String>> friends;

    @FXML
    private Label userNameLabel;
    @FXML
    private VBox friendPanel;
    // thread running status
    public static boolean isRunning = true;
    // blocks
    private ArrayList<HBox> blocks;
    // set the information of the user
    public static void setUser(Map<String, String> user) {
        ListController.user = user;
        friends = (List<Map<String, String>>) ListController.user.get("friends");
    }

    public static void setStage(Stage stage) {
        ListController.stage = stage;
        // overload the stage close event
        stage.setOnCloseRequest(event -> {
            System.out.print("监听list到窗口关闭\n");
            Client.logout((String) user.get("user_id"));
            ListController.isRunning = false;
            System.exit(0);
        });
    }
    // initialize blocks(this) by users(static)
    public void initializeBlocks() {
        this.blocks = new ArrayList<>();
        for (Map<String, String >friend : friends) {
            Label lbFriend = new Label(friend.get("user_name"));
            lbFriend.setTooltip(new Tooltip(friend.get("user_id")));
            lbFriend.setFont(Font.font("Consoles",18));
            lbFriend.setStyle("-fx-text-fill:pink");
            Label statusLabel = new Label(Objects.equals(friend.get("status"), "0") ?"离线":"在线");
            statusLabel.setFont(Font.font("Consoles",16));
            statusLabel.setStyle("-fx-text-fill:white");

            Button chatButton = new Button("聊天");
            // override the push button event
            chatButton.setOnAction(event -> {
                setButtonEvent(friend);
            });
            HBox block = new HBox();
            block.setPadding(new Insets(5, 30, 5, 30));
            block.setSpacing(30);
            block.setAlignment(Pos.valueOf("CENTER_LEFT"));
            block.setBackground(Background.fill(Color.color(0.1, 0.2, 0.4)));
            block.getChildren().addAll(lbFriend, statusLabel, chatButton);
            this.blocks.add(block);
        }
    }
    // refresh blocks(this) by input user_id
    public void refreshBlocks(String user_id, String status) {
        // outer
        for (Map<String, String> friend : friends) {
            if (friend.get("user_id").equals(user_id)) {
                friend.put("status", status);
                System.out.println("friend已经被修改");
                System.out.println(friends);
            }
        }

        for (HBox block : this.blocks) {
           Label nameLabel = (Label) block.getChildren().get(0);
           if (nameLabel.getTooltip().getText().equals(user_id)) {
               Label statusLabel = (Label) block.getChildren().get(1);
               statusLabel.setText(Objects.equals(status, "0") ?"离线":"在线");
               System.out.println(user_id + "的状态以更新为 + " + status);
//               Button button = (Button) block.getChildren().get(1);
//               if (Objects.equals(status, "0")) {
//                   //button.setDisable(true);
//               } else {
//                   button.setDisable(false);
//               }
           }
        }
    }

    public void exitButtonOnAction() {
//        try {
//            Scene swiftScene = new Scene(new FXMLLoader(getClass().getResource("/login-in-view.fxml")).load()
//                    , 520, 400);
//            stage.setScene(swiftScene);
//        } catch (Exception e) {
//            System.out.println("切换页面失败");
//        }
        ListController.isRunning = false;
        Client.logout((String) user.get("user_id"));
        System.exit(0);
    }

    public void PublicChannelButtonOnAction() {
        Map<String, String> fakeFriend = new HashMap<>();
        fakeFriend.put("user_id", "0");
        fakeFriend.put("user_name", "所有人");
        fakeFriend.put("status", "1");
        setButtonEvent(fakeFriend);
    }
    // set the event of the button
    public void setButtonEvent(Map<String, String> friend) {
        System.out.println(friend);
        // ListController.stage.hide();
        // ListController.isRunning = false;
        FXMLLoader fxmlLoader = new FXMLLoader(GUIEntrance.class.getResource("/chat-view.fxml"));
        ChatController.setFriend(friend);
        ChatController.setUser(user);
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 600, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage chatStage = new Stage();

        chatStage.setTitle("与" + friend.get("user_name") + "聊天中");
        chatStage.getIcons().add(new Image("/icon.png"));
        //stage.initStyle(StageStyle.UNDECORATED);
        chatStage.setResizable(false);
        chatStage.setScene(scene);
        chatStage.show();
        chatStage.setOnCloseRequest(event1 -> {
            System.out.println("监听到chat窗口关闭");
            ChatController.isRunning = false;
//            ListController.stage.show();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userNameLabel.setText((String) user.get("user_name"));
        // initialize the list of friends
        System.out.println(user + "成功登录");
        // get the friends
        initializeBlocks();
        friendPanel.getChildren().clear();
        friendPanel.setSpacing(5);
        for (HBox block : this.blocks) {
            friendPanel.getChildren().add(block);
        }
        resetThread();
    }

    // listen to the online information
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (isRunning) {
            try {
                // Thread.sleep(1000);
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                // receive the datagram
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length, address, Client.SERVER_PORT);
                // start receive
                Client.socket.receive(packet);
                String str = new String(buffer, 0, packet.getLength());
                System.out.println("接收到数据报：" + str);

                JSONObject jsonObject = new JSONObject(str);
                COMMAND command = COMMAND.valueOf((String) jsonObject.get("command"));
                if (command == COMMAND.LOGIN || command == COMMAND.LOGOUT) {
                    String userid = (String) jsonObject.get("user_id");
                    String status = (String) jsonObject.get("status");
                    // refresh friends list
                    Platform.runLater(() -> refreshBlocks(userid, status));
                } else if (command == COMMAND.SENDMSG) {
                    FileInteraction.storage(jsonObject.get("sender_id").toString(),
                            jsonObject.get("message").toString().getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception ignored) {}
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