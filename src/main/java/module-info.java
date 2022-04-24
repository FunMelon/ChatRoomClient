module cn.funmelon.chatroomclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;


    opens cn.funmelon.client to javafx.fxml;
    exports cn.funmelon.client;
    exports cn.funmelon.client.controllers;
    opens cn.funmelon.client.controllers to javafx.fxml;
}