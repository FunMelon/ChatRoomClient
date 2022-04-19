package cn.funmelon.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import json.JSONObject;

public class Client {

    // server IP
    public static String SERVER_IP = "FunMelon";
    // server port
    public static int SERVER_PORT = 7788;
    // UDP Datagram
    public static DatagramSocket socket;
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
        InetAddress address;

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
}
