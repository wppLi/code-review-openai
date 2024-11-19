package cc.jq1024.middleware.sdk.domain.service.impl;

import cc.jq1024.middleware.sdk.domain.model.Model;
import cc.jq1024.middleware.sdk.domain.service.AbstractOpenAiCodeReviewService;
import cc.jq1024.middleware.sdk.infrastructure.git.GitCommand;
import cc.jq1024.middleware.sdk.infrastructure.openai.IOpenai;
import cc.jq1024.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cc.jq1024.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import cc.jq1024.middleware.sdk.infrastructure.weixin.WeiXin;
import cc.jq1024.middleware.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li--jiaqiang
 */
public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    private final String defaultString = "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，" +
            "从【1.代码的正确性：这些更改是否正确，是否有潜在的错误或遗漏？，2.性能：这些改动对性能有何影响？，3.可维护性：代码的可读性和可维护性如何？" +
            "4.日志记录：对错误和异常的记录是否到位有效？，5.设计模式：是否应用了合适的设计模式，有没有更好的方法？，6.资源管理：代码是否正确地管理了所有资源？如文件、网络连接、数据库连接等，" +
            "7.复用性：代码是否可以在其他项目或模块中复用？是否设计为通用组件？，8.可扩展性：代码是否设计为便于扩展？将来添加新功能时是否需要对现有代码进行大规模修改？】等多个方面，对代码做出评审, " +
            "尽可能的标出最新记录与之上次提交记录的优势，以及最新记录不规范的代码的位置（包路径）以及所在的行数等信息。diff代码如下:";

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenai openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }


    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.gitDiff();
    }

    @Override
    protected String codeReview(final String diffCode, String reviewCodeDesc) throws Exception {
        if (null == reviewCodeDesc || reviewCodeDesc.isEmpty())
            reviewCodeDesc = defaultString;
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        final String finalReviewCodeDesc = reviewCodeDesc;
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequestDTO.Prompt("user", finalReviewCodeDesc));
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });

        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();

        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);

    }
}