package com.gxhunter.jrebel;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class JrebelActivator {

    private final static String API = "https://headless.zeroturnaround.com/public/api/registrations/add-jrebel-evaluation.php";
    private final static Path JREBEL_DIR = Paths.get(System.getProperty("user.home"), ".jrebel");
    private final static String[] JREBEL_FILES = {
            "jrebel.prefs",
            "jrebel.prefs.lock",
            "jrebel.properties"
    };

    public static void main(String[] args) {
        System.out.println("请输入y开始，输入其他字符退出");
        try (Scanner scanner = new Scanner(System.in)) {
            if (scanner.hasNext()) {
                String input = scanner.nextLine();
                if ("y".equalsIgnoreCase(input)) {
                    activateJrebel();
                } else {
                    System.out.println("再见!");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String subText(String source, String startText, String endText, int offSet) {
        int start = source.indexOf(startText, offSet) + 1;
        int end = source.indexOf(endText, start + offSet + startText.length() - 1);
        if (end == -1) {
            end = source.length();
        }
        return source.substring(start + startText.length() - 1, end);
    }

    private static void activateJrebel() throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Long phone = random.nextLong((long) 1e6, (long) 1e11);
        Path resolve = JREBEL_DIR.resolve("jrebel-version");
        String currentVersion = new String(Files.readAllBytes(resolve));
        System.out.println("正在激活jrebel v " + currentVersion + " ...");
        StringBuilder parameter = new StringBuilder();
        parameter.append("referer_url").append("=").append("IDE").append("&").
                append("email").append("=").append(uuid()).append("=").append("%40qq.com").append("&").
                append("first_name").append("=").append(uuid(), 0, random.nextInt(3, 6)).append("&").
                append("last_name").append("=").append(uuid(), 0, random.nextInt(3, 7)).append("&").
                append("phone").append("=").append(phone).append("&").
                append("organization").append("=").append(uuid(), 0, random.nextInt(1, 8)).append("&").
                append("output_format").append("=").append("json").append("&").
                append("client_os").append("=").append("Windows+11").append("&").
                append("guid").append("=").append(uuid()).append("&").
                append("jrebel-version").append("=").append("2023.1.2").append("&").
                append("ide").append("=").append("intellij").append("&").
                append("ide-product").append("=").append("IU").append("&").
                append("ide-version").append("=").append("2022.3.3").append("&").
                append("jvm.version").append("=").append("17.0.").append("=").append(random.nextInt(0, 21)).append("&").
                append("jvm.vendor").append("=").append("JetBrains+s.r.o").append("&").
                append("os.name").append("=").append("Windows+11");

        URL uri = new URL(API + "?" + parameter);
        System.out.println("正在获取lic:" + uri);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000 * 30);
        connection.setReadTimeout(1000 * 30);
        connection.connect();

        if (connection.getResponseCode() != 200) {
            System.out.println("激活失败，服务端状态码:" + connection.getResponseCode());
            return;
        }


        StringBuilder sb = new StringBuilder();
        String temp;
        try (InputStream in = connection.getInputStream();
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            while ((temp = reader.readLine()) != null) {
                sb.append(temp);
            }
        }
        String content = subText(sb.toString(), "content\":\"", "\"", 0);
        System.out.println("获取到的lic:\n" + content);
        System.out.println("正在重写jrebel配置文件:" + String.join(",", JREBEL_FILES));
        Path jrebelLic = JREBEL_DIR.resolve("jrebel.lic");
        for (String fileName : JREBEL_FILES) {
            Path filePath = JREBEL_DIR.resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
        Files.write(jrebelLic, Base64.getMimeDecoder().decode(content));

        //写入 jrebel.properties
        try (FileOutputStream fos = new FileOutputStream(JREBEL_DIR.resolve("jrebel.properties").toString())) {
            fos.write(("rebel.license=" + jrebelLic.toAbsolutePath() + "\r\n").getBytes());
            fos.write(("rebel.preferred.license=0\r\n").getBytes());
            fos.write(("rebel.properties.version=2\r\n").getBytes());
            fos.flush();
        }
        System.out.println("激活完成，请重启idea");
    }


}
