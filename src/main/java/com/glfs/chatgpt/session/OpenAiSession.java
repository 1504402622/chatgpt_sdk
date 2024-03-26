package com.glfs.chatgpt.session;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.domain.billing.BillingUsage;
import com.glfs.chatgpt.domain.billing.Subscription;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.edits.EditRequest;
import com.glfs.chatgpt.domain.edits.EditResponse;
import com.glfs.chatgpt.domain.embedd.EmbeddingRequest;
import com.glfs.chatgpt.domain.embedd.EmbeddingResponse;
import com.glfs.chatgpt.domain.files.DeleteFileResponse;
import com.glfs.chatgpt.domain.files.UploadFileResponse;
import com.glfs.chatgpt.domain.images.ImageEditRequest;
import com.glfs.chatgpt.domain.images.ImageRequest;
import com.glfs.chatgpt.domain.images.ImageResponse;
import com.glfs.chatgpt.domain.other.OpenAiResponse;
import com.glfs.chatgpt.domain.whisper.TranscriptionsRequest;
import com.glfs.chatgpt.domain.whisper.TranslationsRequest;
import com.glfs.chatgpt.domain.whisper.WhisperResponse;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

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

    /**
     * 问答模型 GPT-3.5/4.0 & 流式反馈
     *
     * @param apiHostByUser         自定义host
     * @param apiKeyByUser          自定义Key
     * @param chatCompletionRequest 请求信息
     * @param eventSourceListener   实现监听；通过监听的 onEvent 方法接收数据
     * @return 应答结果
     */
    EventSource chatCompletions(String apiHostByUser, String apiKeyByUser, ChatCompletionRequest chatCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException;

    /**
     * 文本修复
     *
     * @param editRequest 请求信息
     * @return 应答结果
     */
    EditResponse edit(EditRequest editRequest);

    /**
     * 生成图片
     *
     * @param prompt 图片描述
     * @return 应答结果
     */
    ImageResponse genImages(String prompt);

    /**
     * 生成图片
     *
     * @param imageRequest 图片描述
     * @return 应答结果
     */
    ImageResponse genImages(ImageRequest imageRequest);

    /**
     * 修改图片
     *
     * @param image  图片对象
     * @param prompt 修改描述
     * @return 应答结果
     */
    ImageResponse editImages(File image, String prompt);

    /**
     * 修改图片
     *
     * @param image            图片对象
     * @param imageEditRequest 图片参数
     * @return 应答结果
     */
    ImageResponse editImages(File image, ImageEditRequest imageEditRequest);

    /**
     * 修改图片
     *
     * @param image            图片对象，小于4M的PNG图片
     * @param mask             图片对象，小于4M的PNG图片
     * @param imageEditRequest 图片参数
     * @return 应答结果
     */
    ImageResponse editImages(File image, File mask, ImageEditRequest imageEditRequest);

    /**
     * 向量计算；单个文本
     * 文本向量计算是一种在自然语言处理（NLP）领域中用于测量和比较文本相似性的技术。在这种方法中，每个单词或短语都被转换为一个向量，可以使用这些向量来比较不同文本之间的相似性，并在需要时进行推荐或分类
     *
     * @param input 文本信息
     * @return 应答结果
     */
    EmbeddingResponse embeddings(String input);

    /**
     * 向量计算；多个文本
     * 文本向量计算是一种在自然语言处理（NLP）领域中用于测量和比较文本相似性的技术。在这种方法中，每个单词或短语都被转换为一个向量，可以使用这些向量来比较不同文本之间的相似性，并在需要时进行推荐或分类
     *
     * @param inputs 多个文本
     * @return 应答结果
     */
    EmbeddingResponse embeddings(String... inputs);


    /**
     * 向量计算；多个文本
     * 文本向量计算是一种在自然语言处理（NLP）领域中用于测量和比较文本相似性的技术。在这种方法中，每个单词或短语都被转换为一个向量，可以使用这些向量来比较不同文本之间的相似性，并在需要时进行推荐或分类
     *
     * @param inputs 多个文本
     * @return 应答结果
     */
    EmbeddingResponse embeddings(List<String> inputs);

    /**
     * 向量计算；入参
     * 文本向量计算是一种在自然语言处理（NLP）领域中用于测量和比较文本相似性的技术。在这种方法中，每个单词或短语都被转换为一个向量，可以使用这些向量来比较不同文本之间的相似性，并在需要时进行推荐或分类
     *
     * @param embeddingRequest 请求结果
     * @return 应答结果
     */
    EmbeddingResponse embeddings(EmbeddingRequest embeddingRequest);

    /**
     * 获取文件
     *
     * @return 应答结果
     */
    OpenAiResponse<File> files();

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 应答结果
     */
    UploadFileResponse uploadFile(File file);

    /**
     * 上传文件
     *
     * @param purpose Use "fine-tune" for Fine-tuning. This allows us to validate the format of the uploaded file.
     * @param file    文件
     * @return 应答结果
     */
    UploadFileResponse uploadFile(String purpose, File file);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 应答结果
     */
    DeleteFileResponse deleteFile(String fileId);

    /**
     * 语音转文字
     *
     * @param file                  语音文件
     * @param transcriptionsRequest 请求信息
     * @return 应答结果
     */
    WhisperResponse speed2TextTranscriptions(File file, TranscriptionsRequest transcriptionsRequest);

    /**
     * 语音翻译
     *
     * @param file                语音文件
     * @param translationsRequest 请求信息
     * @return 应答结果
     */
    WhisperResponse speed2TextTranslations(File file, TranslationsRequest translationsRequest);

    /**
     * 账单查询
     *
     * @return 应答结果
     */
    Subscription subscription();

    /**
     * 消耗查询
     *
     * @param starDate 开始时间
     * @param endDate  结束时间
     * @return  应答数据
     */
    BillingUsage billingUsage(@NotNull LocalDate starDate, @NotNull LocalDate endDate);

}
