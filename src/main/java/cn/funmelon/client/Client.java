package cn.funmelon.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import json.JSONObject;

public class Client {

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
            jsonObject.put("user_pwd", password);
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

            if ((Integer) jsonObject.get("result") == 0) {
                 return jsonObject.toMap();
            }
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
}
