package cc.jq1024.middleware.sdk.infrastructure.openai;


import cc.jq1024.middleware.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cc.jq1024.middleware.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenai {

    /** openai 评审接口 */
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO chatCompletionRequest) throws Exception;

}