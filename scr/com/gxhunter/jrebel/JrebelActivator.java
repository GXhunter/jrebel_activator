package com.gxhunter.jrebel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JrebelActivator {

    private final static String API = "https://headless.zeroturnaround.com/public/api/registrations/add-jrebel-evaluation.php";

    public static void main(String[] args) {
        System.out.println("请输入yes开始，输入其他字符退出");
        try (InputStream in = System.in) {
            Scanner scanner = new Scanner(in);
            if (scanner.hasNext()) {
                String input = scanner.nextLine();
                if (input.equals("yes")) {
                    action();
                } else {
                    System.out.println("再见!");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static int randNumber(int max, int min) {
        return min + (int) (Math.random() * (max - min + 1));
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

    private static void action() throws Exception {
        System.out.println("激活中....");
        final File jrebelHome = new File(System.getProperty("user.home") + "/.jrebel");
        final String[] jrebelFiless = new String[]{
                "jrebel.prefs",
                "jrebel.prefs.lock",
                "jrebel.properties"
        };

        // 删除 jrebel 配置文件
        Arrays.stream(jrebelFiless).map(it -> new File(jrebelHome.getAbsolutePath() + "/" + it)).filter(File::exists).forEach(File::delete);

        //write license jrebel.lic
        StringBuilder phone = new StringBuilder();
        for (int i = 0; i < randNumber(6, 11); i++) {
            phone.append(randNumber(0, 9));
        }

        StringBuilder query = new StringBuilder();
        query.append("referer_url").append("=").append("IDE").append("&").
                append("email").append("=").append(uuid()).append("=").append("%40qq.com").append("&").
                append("first_name").append("=").append(uuid(), 0, randNumber(3, 5)).append("&").
                append("last_name").append("=").append(uuid(), 0, randNumber(3, 6)).append("&").
                append("phone").append("=").append(phone).append("&").
                append("organization").append("=").append(uuid(), 0, randNumber(1, 5)).append("&").
                append("output_format").append("=").append("json").append("&").
                append("client_os").append("=").append("Windows+11").append("&").
                append("guid").append("=").append(uuid()).append("&").
                append("jrebel-version").append("=").append("2023.1.2").append("&").
                append("ide").append("=").append("intellij").append("&").
                append("ide-product").append("=").append("IU").append("&").
                append("ide-version").append("=").append("2022.3.3").append("&").
                append("jvm.version").append("=").append("17.0.").append("=").append(randNumber(0, 20)).append("&").
                append("jvm.vendor").append("=").append("JetBrains+s.r.o").append("&").
                append("os.name").append("=").append("Windows+11");

        URL uri = new URL(API + "?" + query);
        System.out.println("请求url:" + uri);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000 * 30);
        connection.setReadTimeout(1000 * 30);
        connection.connect();
        StringBuilder sb = new StringBuilder();
        String temp;
        if (connection.getResponseCode() == 200) {
            try (InputStream in = connection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)
            ) {
                while ((temp = reader.readLine()) != null) {
                    sb.append(temp);
                }
            }
        }
        String ret = sb.toString();
        String content = subText(ret, "content\":\"", "\"", 0);
        System.out.println(content);
        byte[] bin = Base64.getMimeDecoder().decode(content);
        File jrebelLicFile = new File(jrebelHome.getAbsolutePath() + File.separator + "jrebel.lic");
        try (FileOutputStream fos = new FileOutputStream(jrebelLicFile)) {
            fos.write(bin);
            fos.flush();
        }

        //write jrebel.properties
        try (FileOutputStream fos = new FileOutputStream(jrebelHome.getAbsolutePath() + File.separator + "jrebel.properties")) {
            fos.write(("rebel.license=" + jrebelLicFile.getAbsolutePath() + "\r\n").getBytes());
            fos.write(("rebel.preferred.license=0\r\n").getBytes());
            fos.write(("rebel.properties.version=2\r\n").getBytes());
            fos.flush();
        }

        System.out.println("激活完成，请重启idea ,license : " + jrebelLicFile.getAbsolutePath());
    }


}