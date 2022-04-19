package cn.funmelon.client.controllers;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.*;

public class ListController implements Initializable {

    static private Map<String, String> user = new HashMap<>();
    // user status
    static private List<Map<String, String>> friends = new ArrayList<Map<String, String>>();
    // stage
    static private Stage stage;

    @FXML
    private Label userNameLabel;
    @FXML
    private VBox friendPanel;

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
                System.out.print("监听list到窗口关闭");
            }
        });
    }

    public void exitButtonOnAction(ActionEvent event) {
        //Stage stage = (Stage)exitButton.getScene().getWindow();
        try {
            Scene swiftScene = new Scene(new FXMLLoader(getClass().getResource("/login-in-view.fxml")).load()
                    , 520, 400);
            stage.setScene(swiftScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userNameLabel.setText(user.get("user_name"));
        // initialize the list of friends
        System.out.println(user);
        for (Map<String, String> item : friends) {
            Label lbFriend = new Label(item.get("user_name"));
            lbFriend.setTooltip(new Tooltip(item.get("user_id")));
            lbFriend.setFont(Font.font("Consolas",18));

            Button button = new Button("聊天");

            HBox block = new HBox();
            block.setAlignment(Pos.valueOf("CENTER_LEFT"));
            block.getChildren().add(lbFriend);
            block.getChildren().add(button);

            friendPanel.getChildren().add(block);
        }
    }
}
