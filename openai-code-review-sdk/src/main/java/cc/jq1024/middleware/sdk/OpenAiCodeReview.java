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

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("【" + key + "】value is null");
        }
        return value;
    }

    public static void main(String[] args) {
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
        String reviewCodeDesc = getEnv("OPANAI_REVIEW_DESC");
        openAiCodeReviewService.exec(reviewCodeDesc);

        logger.info("openai-code-review done!");
    }
}