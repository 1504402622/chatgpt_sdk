package com.glfs.chatgpt.session;

import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 以 ChatGPT 官网 API 模型，定义接口。官网：https://platform.openai.com/playground
 */
public interface IOpenAiApi {
    /**
     * 默认官方 GPT-3.5 问答模型
     * @param chatCompletionRequest 请求信息
     * @return                      返回结果
     */
    //声明了一个名为 completions 的方法，用于发送请求到指定的路径，并接收返回的结果。
    //基础 URL 与请求路径拼接在一起就是实际的请求 URL
    Single<ChatCompletionResponse> completions(@Body ChatCompletionRequest chatCompletionRequest);

}
