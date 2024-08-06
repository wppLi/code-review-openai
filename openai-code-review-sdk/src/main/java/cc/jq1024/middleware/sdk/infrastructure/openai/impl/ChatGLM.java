package cc.jq1024.middleware.sdk.infrastructure.openai.impl;


import cc.jq1024.middleware.sdk.infrastructure.openai.IOpenai;
import cc.jq1024.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cc.jq1024.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import cc.jq1024.middleware.sdk.types.utils.BearerTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ChatGLM implements IOpenai {

    private static final Logger log = LoggerFactory.getLogger(ChatGLM.class);
    private final String apiHost;
    private final String apiKeySecret;

    public ChatGLM(String apiHost, String apiKey) {
        this.apiHost = apiHost;
        this.apiKeySecret = apiKey;
    }

    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception {
        String token = BearerTokenUtils.getToken(apiKeySecret);
        URL url = new URL(apiHost);
        // 开启连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        // 参数 json 转换
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(requestDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = connection.getResponseCode();
        log.info("请求状态码: {}",responseCode);
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
        // 返回结果
        return JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
    }
}