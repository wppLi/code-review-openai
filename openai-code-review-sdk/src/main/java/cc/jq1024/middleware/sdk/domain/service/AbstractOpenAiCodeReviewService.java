package cc.jq1024.middleware.sdk.domain.service;

import cc.jq1024.middleware.sdk.infrastructure.git.GitCommand;
import cc.jq1024.middleware.sdk.infrastructure.openai.IOpenai;
import cc.jq1024.middleware.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author li--jiaqiang
 */
public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {

    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenai openAI;
    protected final WeiXin weiXin;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenai openAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.weiXin = weiXin;
    }


    @Override
    public void exec(String reviewCodeDesc) {
        try {
            // 1. 获取提交代码
            String diffCode = getDiffCode();
            // 2. 评审代码
            String recommend = codeReview(diffCode, reviewCodeDesc);
            // 3. 记录评审结果，写入日志仓库，返回仓库地址
            String logUrl = recordCodeReview(recommend);
            // 4. 发送消息通知
            pushMessage(logUrl);
        } catch (Exception e) {
            logger.error("openai-code-review error ", e);
        }
    }

    protected abstract String getDiffCode() throws IOException, InterruptedException;

    protected abstract String codeReview(String diffCode, String reviewCodeDesc) throws Exception;

    protected abstract String recordCodeReview(String recommend) throws Exception;

    protected abstract void pushMessage(String logUrl) throws Exception;

}