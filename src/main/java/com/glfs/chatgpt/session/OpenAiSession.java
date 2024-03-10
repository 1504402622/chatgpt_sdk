package com.glfs.chatgpt.session;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

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

    /**
     * 问答模型 GPT-3.5/4.0 & 流式反馈
     * @param chatCompletionRequest 请求信息
     * @param eventSourceListener   实现监听；通过监听的 onEvent 方法接收数据
     * @return                      返回结果
     */
    EventSource chatCompletions(ChatCompletionRequest chatCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException;
}
