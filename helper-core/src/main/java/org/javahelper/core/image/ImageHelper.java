package org.javahelper.core.image;

import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Description:
 *
 * @author shenguangyang
 * @date 2021/03/22
 */
public class ImageHelper {
    /**
     * 获取图片的base64
     * @param imageUrl 图片url
     * @return base64
     */
    public static String getBase64(String imageUrl){
        DataInputStream in = null;
        HttpURLConnection connection = null;
        String base64Str = null;
        try {
            URL url = new URL(imageUrl);
            // 打开链接
            connection = (HttpURLConnection) url.openConnection();
            in = new DataInputStream(connection.getInputStream());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            // 读取图片
            while ((length = in.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            // 转换为 base64
            BASE64Encoder base64Encoder = new BASE64Encoder();
            base64Str = base64Encoder.encode(output.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null){
                connection.disconnect();
            }
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 拼接前缀，可以交给前台
        return "data:image/jpg;base64," + base64Str;
    }
}
