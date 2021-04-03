package org.javahelper.core.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author shenguangyang
 */
public class ResponseHelper {

    /**
     * 输出到浏览器中
     * @param response 响应体
     * @param r
     */
    public static void out(HttpServletResponse response, Object result) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            mapper.writeValue(response.getWriter(), result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
