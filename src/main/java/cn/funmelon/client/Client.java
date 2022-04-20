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
    public static void main(String[] args) throws SocketException {
        if (args.length == 2) {
            SERVER_IP = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
        }
        try { // create new datagramSocket and allocate new port
            socket = new DatagramSocket();
            // time restrict 5s
            socket.setSoTimeout(1000);
            System.out.println("客户端运行...");
            // TODO start the GUI
            GUIEntrance.main(args);
        } catch (SocketException e) {
            System.out.println("建立DatagramSocket失败");
            e.printStackTrace();
        }
    }

    // client send login request to server
    public static Map login(String userId, String password) {
        // buffer
        byte[] buffer = new byte[2048];

        try {
            address = InetAddress.getByName(SERVER_IP);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", COMMAND.LOGIN);
            jsonObject.put("user_id", userId);
            jsonObject.put("user_pwd", password);

            byte[] msg = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            // create the datagramPacket
            DatagramPacket packet = new DatagramPacket(msg, msg.length, address, SERVER_PORT);
            // send
            socket.send(packet);
            // receive
            packet = new DatagramPacket(buffer, buffer.length, address, SERVER_PORT);
            socket.receive(packet);

            String str = new String(buffer, 0, packet.getLength());
            System.out.println("receiveJsonObj = " + str);
            jsonObject = new JSONObject(str);

            if ((Integer) jsonObject.get("result") == 0) {
                Map user = jsonObject.toMap();
                return user;
            }
        } catch (IOException e) {
            System.out.println("读取错误");
            e.printStackTrace();
        }
        return null;
    }
    // send message to server
    public static void sendMessage(String str, String senderId, String receiverId) {
            Map<String, String> msg = new HashMap<String, String>();
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
            } catch (IOException e) {
                e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
