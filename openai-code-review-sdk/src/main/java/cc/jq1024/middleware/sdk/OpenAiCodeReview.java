package cc.jq1024.middleware.sdk;

import cc.jq1024.middleware.sdk.domain.model.ChatCompletionRequest;
import cc.jq1024.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import cc.jq1024.middleware.sdk.domain.model.Model;
import cc.jq1024.middleware.sdk.types.utils.BearerTokenUtils;
import cc.jq1024.middleware.sdk.types.utils.RandomUtils;
import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class OpenAiCodeReview {


    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    public static void main(String[] args) throws Exception {
        System.out.println("openai-code-review 测试开始");

        // 0. 获取token
        String githubToken = System.getenv("GITHUB_TOKEN");
        if (null == githubToken || githubToken.isEmpty()) {
            throw new RuntimeException("githubToken is null!");
        }

        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        // 设置工作目录 .代表当前目录
        processBuilder.directory(new File("."));
        // 启动，
        Process process = processBuilder.start();

        // 读取数据
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }
        // 等待完成
        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);


        // 2. chatglm 代码评审
        String log = chatGlmCodeReview(diffCode.toString());
        System.out.println("code review logs：" + log);

        String logUrl = writeLog(githubToken, log);
        logger.info("log write url: {}", logUrl);
    }

    /**
     * chatglm 代码评审
     */
    private static String chatGlmCodeReview(String diffCode) throws Exception {
        String token = BearerTokenUtils.getToken();

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        // 开启连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
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
        List<ChatCompletionSyncResponse.Choice> choices = response.getChoices();
        ChatCompletionSyncResponse.Choice choice = choices.get(0);
        ChatCompletionSyncResponse.Message message = choice.getMessage();
        return message.getContent();
    }

    /**
     * 日志写入
     */
    private static String writeLog(String token, String log) throws Exception {
        String baseFilePath = "repo";
        Git git = Git.cloneRepository()
                .setURI("https://github.com/wppLi/opena-code-review-log.git") // 仓库地址
                .setDirectory(new File(baseFilePath)) // 设置日志存储路径
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();

        // 每天的日志放在一个文件夹
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath = baseFilePath + "/" + dateFolderName;
        File dateFolder = new File(filePath);
        if (!dateFolder.exists()) {
            // 目录不存在，则创建
            boolean mkdir = dateFolder.mkdir();
            logger.info("目录【{}】不存在，创建结果：{}", filePath, mkdir);
        }
        // 文件名 - 后续可以通过【工程名+提交的人+分支】等信息作为名字的一部分
        String fileName = RandomUtils.generateRandomString(12) + ".md";
        File newFile = new File(dateFolder, fileName);

        // 将日志写入文件
        try(FileWriter writer = new FileWriter(newFile)) {
            writer.write(log);
        }

        // git命令进行提交
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new log file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""));
        logger.info("Changes have been pushed to the repository.");

        return "https://github.com/wppLi/opena-code-review-log/blob/main/" + dateFolderName + "/" + fileName;
    }

}