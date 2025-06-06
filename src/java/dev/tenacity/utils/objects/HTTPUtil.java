package dev.tenacity.utils.objects;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPUtil {

    @SneakyThrows
    public static void download(String fileUrl, File outputFile) {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        // 模拟谷歌浏览器的User-Agent
        httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        int responseCode = httpConn.getResponseCode();

        // 检查HTTP响应码是否正常
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 打开一个输入流用于从服务器获取数据
            InputStream inputStream = httpConn.getInputStream();

            // 打开一个输出流用于将数据写入本地文件
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
        } else {
            System.out.println("下载失败，响应码：" + responseCode);
        }
        httpConn.disconnect();
    }
}
