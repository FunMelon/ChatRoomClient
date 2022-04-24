package cn.funmelon.client;

import cn.funmelon.client.controllers.ChatController;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FileInteraction {

    private static final String path = getPath();//FileInteraction.class.getProtectionDomain().getCodeSource().getLocation().getPath();  ;
    // Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("records")).getPath();
    // append msg in the end of the certain file
    public static void storage(String fileName, byte[] msg) {
        File file = new File(path + "/"+ fileName + ".txt");
        System.out.println(file);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e ) {
                System.out.println("创建缓存文件失败");
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file, true);
            out.write(msg);
            out.close();
        } catch (IOException e) {
            System.out.println("更新缓存失败");
        }
        ChatController.isRefresh = true;
    }

    // clear records
    public static void clear(String fileName) {
        File file = new File(path + "/" + fileName + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    // read records
    public static String read(String fileName) {
        File file = new File(path + "/" + fileName + ".txt");
        if (file.exists()) {
            try {
                StringBuilder res = new StringBuilder();
                byte[] data = new byte[1024];
                FileInputStream in = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(in);
                int len;

                while ((len = in.read(data)) != -1) {
                    String str = new String(data,0, len, StandardCharsets.UTF_8);
                    res.append(str);
                }
                reader.close();
                in.close();
                return res.toString();
            } catch (IOException e) {
                System.out.println("读取本地聊天记录失败");
            }
        }
        return null;
    }

    public static String getPath()
    {
        String path = FileInteraction.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if(System.getProperty("os.name").contains("dows"))
        {
            path = path.substring(1,path.length());
        }
        if(path.contains("jar"))
        {
            path = path.substring(0,path.lastIndexOf("."));
            return path.substring(0,path.lastIndexOf("/"));
        }
        return path.replace("target/classes/", "");
    }
}
