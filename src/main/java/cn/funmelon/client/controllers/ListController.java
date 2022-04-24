package cn.funmelon.client.controllers;

import cn.funmelon.client.Client;
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

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ListController implements Initializable, Runnable {

    // stage
    private static Stage stage;
    // user 's all information
    private static Map user;
    // friends
    private static List<Map<String, String>> friends;

    @FXML
    private Label userNameLabel;
    @FXML
    private VBox friendPanel;

    // thread running status
    public static boolean isRunning = true;
    // new information
    public static boolean isRefresh = true;

    // set the information of the user
    public static void setUser(Map<String, String> user) {
        ListController.user = user;
        ListController.friends = (List<Map<String, String>>) ListController.user.get("friends");
    }

    public static void setStage(Stage stage) {
        ListController.stage = stage;
        // overload the stage close event
        ListController.stage.setOnCloseRequest(event -> {
            System.out.print("监听list到窗口关闭\n");
            Client.logout((String) user.get("user_id"));
            ListController.isRunning = false;
            System.exit(0);
        });
    }
    // change friends
    public static void changeFriends(String userId, String status) {
        for (Map<String, String> friend : friends) {
            if (friend.get("user_id").equals(userId)) {
                friend.put("status", status);
                ListController.isRefresh = true;
            }
        }
    }
    // refresh blocks(this) by input user_id
    public void refreshBlocks() {
        // blocks
        ArrayList<HBox> blocks = new ArrayList<>();
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
                //chatButton.setDisable(true);
                userNameLabel.getScene().getWindow().hide();
                setButtonEvent(friend);
            });
            HBox block = new HBox();
            block.setPadding(new Insets(5, 30, 5, 30));
            block.setSpacing(30);
            block.setAlignment(Pos.valueOf("CENTER_LEFT"));
            block.setBackground(Background.fill(Color.color(0.1, 0.2, 0.4)));
            block.getChildren().addAll(lbFriend, statusLabel, chatButton);
            blocks.add(block);
        }
        friendPanel.getChildren().clear();
        friendPanel.setSpacing(5);
        for (HBox block : blocks) {
            friendPanel.getChildren().add(block);
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
        ListController.stage.hide();
        setButtonEvent(fakeFriend);
    }
    // set the event of the button
    public void setButtonEvent(Map<String, String> friend) {
//        ListController.stage.hide();
//        ListController.isRunning = false;
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
            ListController.stage.show();
//            button.setDisable(false);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userNameLabel.setText((String) user.get("user_name"));
        // initialize the list of friends
        System.out.println(user + "成功登录");
        // load the friends list
        resetThread();
    }
    // listen to the online information
    @Override
    public void run() {
        while (ListController.isRunning) {
            if (ListController.isRefresh) {
                System.out.println("想要刷新列表");
                Platform.runLater(this::refreshBlocks);
                ListController.isRefresh = false;
            }
        }
    }

    public void resetThread() {
        ListController.isRunning = true;
        // receive message sub thread
        Thread receiveMessageThread = new Thread(this);
        // start!
        receiveMessageThread.start();
    }
}