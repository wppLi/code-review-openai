package cc.jq1024.middleware;

import cc.jq1024.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import cc.jq1024.middleware.sdk.types.utils.BearerTokenUtils;
import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) {
        String token = BearerTokenUtils.getToken();
        System.out.println(token);
    }


    @Test
    public void test_http() throws IOException {
        String token = BearerTokenUtils.getToken();

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        // 开启连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String code = "1+1";

        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";


        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("请求状态码：" + responseCode);

        // 读取结果
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }
        // 关闭连接
        in.close();
        connection.disconnect();

        // 转换结果
        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());

    }

}
