package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
import cn.funmelon.client.GUIEntrance;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

public class ListController implements Initializable, Runnable {

    // user 's all information
    static private Map user;
    // stage
    static private Stage stage;
    // blocks

    @FXML
    private Label userNameLabel;
    @FXML
    private VBox friendPanel;
    @FXML
    private Button refreshButton;

    // thread running status
    public static boolean isRunning = true;
    // blocks
    private ArrayList<HBox> blocks;

    // set the information of the user
    public static void setUser(Map<String, String> user) {
        ListController.user = user;
    }

    public static void setStage(Stage stage) {
        ListController.stage = stage;
        // overload the stage close event
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("监听list到窗口关闭\n");
                Client.logout((String) user.get("user_id"));
                ListController.isRunning = false;
            }
        });
    }

    // initialize blocks(this) by users(static)
    public void initializeBlocks() {
        List<Map<String, String>> friends = (List<Map<String, String>>) ListController.user.get("friends");
        int i = 0;
        this.blocks = new ArrayList<>();
        for (Map<String, String >friend : friends) {
            Label lbFriend = new Label(friend.get("user_name"));
            lbFriend.setTooltip(new Tooltip(friend.get("user_id")));
            lbFriend.setFont(Font.font("Consolas",18));

            Label statusLabel = new Label(Objects.equals(friend.get("status"), "0") ?"离线":"在线");
            statusLabel.setFont(Font.font("Consolas",16));

            Button chatButton = new Button("聊天");
            if (friend.get("status").equals("0")) {
                //chatButton.setDisable(true);
            }
            // override the push button event
            chatButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ListController.stage.hide();
                    ListController.isRunning = false;
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
                    chatStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            System.out.println("监听到chat窗口关闭");
                            ChatController.isRunning = false;
                            ListController.stage.show();
                        }
                    });
                }
            });
            HBox block = new HBox();
            block.setAlignment(Pos.valueOf("CENTER_LEFT"));
            block.getChildren().addAll(lbFriend, chatButton, statusLabel);
            this.blocks.add(block);
        }
    }
    // refresh blocks(this) by input user_id
    public void refreshBlocks(String user_id, String status) {
        for (HBox block : this.blocks) {
           Label nameLabel = (Label) block.getChildren().get(0);
           if (nameLabel.getTooltip().getText().equals(user_id)) {
               Label statusLabel = (Label) block.getChildren().get(2);
               statusLabel.setText(Objects.equals(status, "0") ?"离线":"在线");
               System.out.println(user_id + "的状态以更新为 + " + status);

               Button button = (Button) block.getChildren().get(1);
               if (Objects.equals(status, "0")) {
                   //button.setDisable(true);
               } else {
                   button.setDisable(false);
               }
           }
        }
    }

    public void exitButtonOnAction(ActionEvent event) {
        // TODO unable the cancel button
        //Stage stage = (Stage)exitButton.getScene().getWindow();
//        try {
//            Scene swiftScene = new Scene(new FXMLLoader(getClass().getResource("/login-in-view.fxml")).load()
//                    , 520, 400);
//            stage.setScene(swiftScene);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        // restart the thread
        // ListController.isRunning = false;
    }

    public void RefreshButtonOnAction(ActionEvent event) {
        initializeBlocks();
        friendPanel.getChildren().clear();
        for (HBox block : this.blocks) {
            friendPanel.getChildren().add(block);
        }
        resetThread();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userNameLabel.setText((String) user.get("user_name"));
        // initialize the list of friends
        System.out.println(user);
        // get the friends
        initializeBlocks();
        friendPanel.getChildren().clear();
        for (HBox block : this.blocks) {
            friendPanel.getChildren().add(block);
        }
        resetThread();
    }

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
                System.out.println("客户端： " + str);

                JSONObject jsonObject = new JSONObject(str);
                String userid = (String) jsonObject.get("user_id");
                String status = (String) jsonObject.get("status");
                // refresh friends list
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshBlocks(userid, status);
                    }
                });
            } catch (Exception e) {
                // System.out.println("未收好友上线消息");
                // e.printStackTrace();
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