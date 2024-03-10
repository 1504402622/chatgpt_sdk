package com.glfs.chatgpt.session;


import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;

/**
 * @author 小傅哥，微信：fustack
 * @description OpenAi 会话接口
 */
public interface OpenAiSession {


    /**
     * 默认 GPT-3.5 问答模型
     * @param chatCompletionRequest 请求信息
     * @return                      返回结果
     */
    ChatCompletionResponse completions(ChatCompletionRequest chatCompletionRequest);

}
