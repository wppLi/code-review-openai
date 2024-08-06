package cc.jq1024.middleware.sdk;

import cc.jq1024.middleware.sdk.domain.service.impl.OpenAiCodeReviewService;
import cc.jq1024.middleware.sdk.infrastructure.git.GitCommand;
import cc.jq1024.middleware.sdk.infrastructure.openai.IOpenai;
import cc.jq1024.middleware.sdk.infrastructure.openai.impl.ChatGLM;
import cc.jq1024.middleware.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);


    // 微信配置
    private final String weixin_appid = "wxae5eccc3b6930993";
    private final String weixin_secret = "8fdec049feb30865cc9bb0e1c0b7dad3";
    private final String weixin_touser = "oxd-l6lb0KOF5KRwBy7GDHxeXaeE";
    private final String weixin_template_id = "tuPX8kaM6tglobPGIoJPAR6jYWpeZctg5EFL1YLWcFU";

    // ChatGLM 配置
    private final String chatglm_apiHost = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private final String chatglm_apiKeySecret = "5b0e212444fbaf1ed11cf8cfa9089b5f.RwfbslyKDSEGxDd1";

    // Github 配置
    private String github_review_log_uri;
    private String github_return_log_uri;
    private String github_token;

    // 工程配置 - 自动获取
    private String github_project;
    private String github_branch;
    private String github_author;


    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("【" + key + "】value is null");
        }
        return value;
    }



    public static void main(String[] args) throws Exception {
        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_RETURN_LOG_URI"),

                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        /*
         * 项目：{{repo_name.DATA}} 分支：{{branch_name.DATA}} 作者：{{commit_author.DATA}} 说明：{{commit_message.DATA}}
         */
        WeiXin weiXin = new WeiXin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        IOpenai openAi = new ChatGLM(getEnv("CHATGLM_APIHOST"), getEnv("CHATGLM_APIKEYSECRET"));

        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAi, weiXin);
        openAiCodeReviewService.exec();

        logger.info("openai-code-review done!");


//
//        // 1. 代码检出
//        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
//        // 设置工作目录 .代表当前目录
//        processBuilder.directory(new File("."));
//        // 启动，
//        Process process = processBuilder.start();
//
//        // 读取数据
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        StringBuilder diffCode = new StringBuilder();
//        while ((line = reader.readLine()) != null) {
//            diffCode.append(line);
//        }
//        // 等待完成
//        int exitCode = process.waitFor();
//        System.out.println("Exited with code:" + exitCode);
//
//
//        // 2. chatglm 代码评审
//        String log = chatGlmCodeReview(diffCode.toString());
//        System.out.println("code review logs：" + log);
//
//        // 3. 写入日志
//        String logUrl = writeLog(githubToken, log);
//        logger.info("log write url: {}", logUrl);
//
//        // 4. 消息通知
//        logger.info("push message: {}", logUrl);
//        pushMessage(logUrl);
//    }
//
//    /**
//     * 消息通知
//     */
//    private static void pushMessage(String logUrl) {
//        String accessToken = WXAccessTokenUtils.getAccessToken();
//        System.out.println(accessToken);
//
//        String touser = "oxd-l6lb0KOF5KRwBy7GDHxeXaeE";
//        String template_id = "HwS6_X9qNeOb2j2MWZZfg70xLiPdefcJMrV-xchIr8Y";
//        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO(touser, template_id);
//        templateMessageDTO.put("project","big-market");
//        templateMessageDTO.put("review","feat: 新加功能");
//        templateMessageDTO.setUrl(logUrl);
//
//        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
//        sendPostRequest(url, JSON.toJSONString(templateMessageDTO));
//    }
//
//    private static void sendPostRequest(String urlString, String jsonBody) {
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json; utf-8");
//            conn.setRequestProperty("Accept", "application/json");
//            conn.setDoOutput(true);
//
//            try (OutputStream os = conn.getOutputStream()) {
//                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
//                os.write(input, 0, input.length);
//            }
//
//            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
//                String response = scanner.useDelimiter("\\A").next();
//                System.out.println(response);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * chatglm 代码评审
//     */
//    private static String chatGlmCodeReview(String diffCode) throws Exception {
//        String token = BearerTokenUtils.getToken();
//
//        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
//        // 开启连接
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Authorization", "Bearer " + token);
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
//        connection.setDoOutput(true);
//
//        ChatCompletionRequestDTO chatCompletionRequestDTO = new ChatCompletionRequestDTO();
//        chatCompletionRequestDTO.setModel(Model.GLM_4_FLASH.getCode());
//        chatCompletionRequestDTO.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
//            private static final long serialVersionUID = -7988151926241837899L;
//            {
//                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
//                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
//            }
//        });
//
//        try(OutputStream os = connection.getOutputStream()) {
//            byte[] input = JSON.toJSONString(chatCompletionRequestDTO).getBytes(StandardCharsets.UTF_8);
//            os.write(input);
//        }
//
//        int responseCode = connection.getResponseCode();
//        System.out.println("请求状态码：" + responseCode);
//
//        // 读取结果
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String inputLine;
//        StringBuilder content = new StringBuilder();
//        while ((inputLine = in.readLine()) != null){
//            content.append(inputLine);
//        }
//        // 关闭连接
//        in.close();
//        connection.disconnect();
//
//        // 转换结果
//        ChatCompletionSyncResponseDTO response = JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
//        List<ChatCompletionSyncResponseDTO.Choice> choices = response.getChoices();
//        ChatCompletionSyncResponseDTO.Choice choice = choices.get(0);
//        ChatCompletionSyncResponseDTO.Message message = choice.getMessage();
//        return message.getContent();
//    }
//
//    /**
//     * 日志写入
//     */
//    private static String writeLog(String token, String log) throws Exception {
//        String baseFilePath = "repo";
//        Git git = Git.cloneRepository()
//                .setURI("https://github.com/wppLi/opena-code-review-log.git") // 仓库地址
//                .setDirectory(new File(baseFilePath)) // 设置日志存储路径
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
//                .call();
//
//        // 每天的日志放在一个文件夹
//        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//        String filePath = baseFilePath + "/" + dateFolderName;
//        File dateFolder = new File(filePath);
//        if (!dateFolder.exists()) {
//            // 目录不存在，则创建
//            boolean mkdir = dateFolder.mkdir();
//            logger.info("目录【{}】不存在，创建结果：{}", filePath, mkdir);
//        }
//        // 文件名 - 后续可以通过【工程名+提交的人+分支】等信息作为名字的一部分
//        String fileName = RandomUtils.generateRandomString(12) + ".md";
//        File newFile = new File(dateFolder, fileName);
//
//        // 将日志写入文件
//        try(FileWriter writer = new FileWriter(newFile)) {
//            writer.write(log);
//        }
//
//        // git命令进行提交
//        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
//        git.commit().setMessage("Add new log file via GitHub Actions").call();
//        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();
//        logger.info("Changes have been pushed to the repository.");
//
//        logger.info("git commit and push done! - {}", fileName);
//
//        return "https://github.com/wppLi/opena-code-review-log/blob/main/" + dateFolderName + "/" + fileName;
//    }

    }
}