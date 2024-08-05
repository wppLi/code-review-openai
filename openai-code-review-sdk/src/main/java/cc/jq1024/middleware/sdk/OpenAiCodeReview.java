package cc.jq1024.middleware.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenAiCodeReview {


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("openai-code-review 测试开始");

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

        System.out.println("评审代码：" + diffCode.toString());
    }

}