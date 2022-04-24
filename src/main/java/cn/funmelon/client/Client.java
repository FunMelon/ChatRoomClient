package cn.funmelon.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import cn.funmelon.client.controllers.ChatController;
import cn.funmelon.client.controllers.ListController;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import json.JSONObject;

public class Client implements Runnable{

    public static boolean isRunning = false;
    // server IP
    public static String SERVER_IP = "FunMelon";
    // server port
    public static int SERVER_PORT = 7788;
    // UDP Datagram
    public static DatagramSocket socket;
    // server address
    private static InetAddress address;
    // start client
    public static void main(String[] args) {
        if (args.length == 2) {
            SERVER_IP = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
        }
        try { // create new datagramSocket and allocate new port
            socket = new DatagramSocket();
            // time restrict 5s
            socket.setSoTimeout(1000);
            System.out.println("客户端运行...");
            // start the gui
            GUIEntrance.main(args);
            // start the thread
        } catch (SocketException e) {
            System.out.println("建立DatagramSocket失败");
        }
    }

    // client send login request to server
    public static Map login(String userId, String password) {
        byte[] buffer = new byte[2048];

        try {
            address = InetAddress.getByName(SERVER_IP);
            // wrap
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", COMMAND.LOGIN);
            jsonObject.put("user_id", userId);
            jsonObject.put("user_pwd", DigestUtils.sha1Hex(password));
            // send
            byte[] info = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(info, info.length, address, SERVER_PORT);
            socket.send(packet);
            // receive
            packet = new DatagramPacket(buffer, buffer.length, address, SERVER_PORT);
            socket.receive(packet);

            String str = new String(buffer, 0, packet.getLength(), StandardCharsets.UTF_8);
            System.out.println("receiveJsonObj = " + str);
            jsonObject = new JSONObject(str);

            // if ((Integer) jsonObject.get("result") == 0) {
                return jsonObject.toMap();
            // }
        } catch (IOException e) {
            System.out.println("登陆失败");
        }
        return null;
    }
    // send message to server
    public static void sendMessage(String str, String senderId, String receiverId) {
            Map<String, String> msg = new HashMap<>();
            msg.put("receiver_id", receiverId);
            msg.put("sender_id", senderId);
            msg.put("message", str);

            JSONObject jsonObject = new JSONObject(msg);
            jsonObject.put("command", COMMAND.SENDMSG);
            try {
                address = InetAddress.getByName(SERVER_IP);
                byte[] info = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(info, info.length, address, SERVER_PORT);
                Client.socket.send(packet);
                System.out.println("已成功发送报文到服务器");
            } catch (IOException e) {
                System.out.println("发送消息失败");
            }
    }
    // logout
    public static void logout(String userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", COMMAND.LOGOUT);
        jsonObject.put("user_id", userId);

        byte[] msg = jsonObject.toString().getBytes();
        try {
            address = InetAddress.getByName(Client.SERVER_IP);
            DatagramPacket packet = new DatagramPacket(msg, msg.length, address, SERVER_PORT);
            socket.send(packet);
            System.out.println("成功向服务端发送下线消息");
        } catch (IOException e) {
            System.out.println("发送下线消息失败");
        }
    }

    // listen to all
    @Override
    public void run() {
        while (Client.isRunning) {
            byte[] buffer = new byte[1024];
            try {
                // Thread.sleep(1000);
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                // receive the datagram
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length, address, Client.SERVER_PORT);
                // start receive
                Client.socket.receive(packet);
                String str = new String(buffer, 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("接收到数据报：" + str);

                JSONObject jsonObject = new JSONObject(str);
                COMMAND command = COMMAND.valueOf((String) jsonObject.get("command"));
                if (command == COMMAND.LOGIN || command == COMMAND.LOGOUT) {
                    String userId = (String) jsonObject.get("user_id");
                    String status = (String) jsonObject.get("status");
                    // refresh friends list
                    ListController.changeFriends(userId, status);
                    ChatController.isRefresh = true;
                } else if (command == COMMAND.SENDMSG) {
                    FileInteraction.storage(jsonObject.get("sender_id").toString(),
                            jsonObject.get("message").toString().getBytes(StandardCharsets.UTF_8));
                } else if (command == COMMAND.KICK) {
                    logout((String) jsonObject.get("user_id"));
                    System.out.println("您已被强制下线，可能为异地登陆，请联系服务器管理员");
                    System.exit(0);
                }
            } catch (Exception ignored) {}
        }
    }

    public void resetThread() {
        System.out.println("启动线程");
        Client.isRunning = true;
        // receive message sub thread
        Thread receiveMessageThread = new Thread(this);
        // start!
        receiveMessageThread.start();
    }
}
