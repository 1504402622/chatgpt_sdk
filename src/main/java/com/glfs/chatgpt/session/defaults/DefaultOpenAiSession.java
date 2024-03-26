package com.glfs.chatgpt.session.defaults;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glfs.chatgpt.IOpenAiApi;
import com.glfs.chatgpt.common.Constants;
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
import com.glfs.chatgpt.session.Configuration;
import com.glfs.chatgpt.session.OpenAiSession;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class DefaultOpenAiSession  implements OpenAiSession {
    /** 配置信息 */
    private final Configuration configuration;

    /** 工厂事件 */
    private final EventSource.Factory factory;

    public DefaultOpenAiSession(Configuration configuration) {
        this.configuration = configuration;
        this.factory = configuration.createRequestFactory();
    }

    /**
     * chatgpt3.5
     * @param chatCompletionRequest 请求信息
     * @return
     */
    @Override
    public ChatCompletionResponse completions(ChatCompletionRequest chatCompletionRequest) {
        return this.configuration.getOpenAiApi().completions(chatCompletionRequest).blockingGet();
    }

    /**
     * chatgpt3.5流式
     * @param chatCompletionRequest 请求信息
     * @param eventSourceListener   实现监听；通过监听的 onEvent 方法接收数据
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public EventSource chatCompletions(ChatCompletionRequest chatCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException {
        return chatCompletions(Constants.NULL, Constants.NULL, chatCompletionRequest, eventSourceListener);
    }

    @Override
    public EventSource chatCompletions(String apiHostByUser, String apiKeyByUser, ChatCompletionRequest chatCompletionRequest, EventSourceListener eventSourceListener) throws JsonProcessingException {
        //核心参数校验；不对用户传参进行更改，只修改返回错误信息
        if (!chatCompletionRequest.isStream()) {
            throw new RuntimeException("illegal parameter stream is false!");
        }
        // 动态设置 Host、Key，便于用户传递自己的信息
        String apiHost = Constants.NULL.equals(apiHostByUser) ? configuration.getApiHost() : apiHostByUser;
        String apiKey = Constants.NULL.equals(apiKeyByUser) ? configuration.getApiKey() : apiKeyByUser;
        // 构建请求信息
        Request request = new Request.Builder()
                // url: https://api.openai.com/v1/chat/completions - 通过 IOpenAiApi 配置的 POST 接口，用这样的方式从统一的地方获取配置信息
                //拼接字符串
                .url(apiHost.concat(IOpenAiApi.v1_chat_completions))
                .addHeader("apiKey", apiKey)
                // 封装请求参数信息，如果使用了 Fastjson 也可以替换 ObjectMapper 转换对象
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), new ObjectMapper().writeValueAsString(chatCompletionRequest)))
                .build();

        // 返回结果信息；EventSource 对象可以取消应答
        return factory.newEventSource(request, eventSourceListener);
    }

    @Override
    public EditResponse edit(EditRequest editRequest) {
        return this.configuration.getOpenAiApi().edits(editRequest).blockingGet();
    }

    @Override
    public ImageResponse genImages(String prompt) {
        ImageRequest imageRequest = ImageRequest.builder().prompt(prompt).build();
        return this.genImages(imageRequest);
    }

    @Override
    public ImageResponse genImages(ImageRequest imageRequest) {
        return this.configuration.getOpenAiApi().genImages(imageRequest).blockingGet();
    }

    @Override
    public ImageResponse editImages(File image, String prompt) {
        ImageEditRequest imageEditRequest = ImageEditRequest.builder().prompt(prompt).build();
        return this.editImages(image, null, imageEditRequest);
    }

    @Override
    public ImageResponse editImages(File image, ImageEditRequest imageEditRequest) {
        return this.editImages(image, null, imageEditRequest);
    }

    @Override
    public ImageResponse editImages(File image, File mask, ImageEditRequest imageEditRequest) {
        // 1. imageMultipartBody
        RequestBody imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), image);
        MultipartBody.Part imageMultipartBody = MultipartBody.Part.createFormData("image", image.getName(), imageBody);
        // 2. maskMultipartBody
        MultipartBody.Part maskMultipartBody = null;
        if (Objects.nonNull(mask)) {
            RequestBody maskBody = RequestBody.create(MediaType.parse("multipart/form-data"), mask);
            maskMultipartBody = MultipartBody.Part.createFormData("mask", mask.getName(), maskBody);
        }
        // requestBodyMap
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        requestBodyMap.put("prompt", RequestBody.create(MediaType.parse("multipart/form-data"), imageEditRequest.getPrompt()));
        requestBodyMap.put("n", RequestBody.create(MediaType.parse("multipart/form-data"), imageEditRequest.getN().toString()));
        requestBodyMap.put("size", RequestBody.create(MediaType.parse("multipart/form-data"), imageEditRequest.getSize()));
        requestBodyMap.put("response_format", RequestBody.create(MediaType.parse("multipart/form-data"), imageEditRequest.getResponseFormat()));
        if (!(Objects.isNull(imageEditRequest.getUser()) || "".equals(imageEditRequest.getUser()))) {
            requestBodyMap.put("user", RequestBody.create(MediaType.parse("multipart/form-data"), imageEditRequest.getUser()));
        }
        return this.configuration.getOpenAiApi().editImages(imageMultipartBody, maskMultipartBody, requestBodyMap).blockingGet();
    }

    @Override
    public EmbeddingResponse embeddings(String input) {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder().input(new ArrayList<String>() {{
            add(input);
        }}).build();
        return this.embeddings(embeddingRequest);
    }

    @Override
    public EmbeddingResponse embeddings(String... inputs) {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder().input(Arrays.asList(inputs)).build();
        return this.embeddings(embeddingRequest);
    }

    @Override
    public EmbeddingResponse embeddings(List<String> inputs) {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder().input(inputs).build();
        return this.embeddings(embeddingRequest);
    }

    @Override
    public EmbeddingResponse embeddings(EmbeddingRequest embeddingRequest) {
        return this.configuration.getOpenAiApi().embeddings(embeddingRequest).blockingGet();
    }

    @Override
    public OpenAiResponse<File> files() {
        return this.configuration.getOpenAiApi().files().blockingGet();
    }

    @Override
    public UploadFileResponse uploadFile(File file) {
        return this.uploadFile("fine-tune", file);
    }

    @Override
    public UploadFileResponse uploadFile(String purpose, File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        RequestBody purposeBody = RequestBody.create(MediaType.parse("multipart/form-data"), purpose);
        return this.configuration.getOpenAiApi().uploadFile(multipartBody, purposeBody).blockingGet();
    }

    @Override
    public DeleteFileResponse deleteFile(String fileId) {
        return this.configuration.getOpenAiApi().deleteFile(fileId).blockingGet();
    }

    @Override
    public WhisperResponse speed2TextTranscriptions(File file, TranscriptionsRequest transcriptionsRequest) {
        // 1. 语音文件
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        // 2. 参数封装
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        if (StrUtil.isNotBlank(transcriptionsRequest.getLanguage())) {
            requestBodyMap.put(TranscriptionsRequest.Fields.language, RequestBody.create(MediaType.parse("multipart/form-data"), transcriptionsRequest.getLanguage()));
        }
        if (StrUtil.isNotBlank(transcriptionsRequest.getModel())) {
            requestBodyMap.put(TranscriptionsRequest.Fields.model, RequestBody.create(MediaType.parse("multipart/form-data"), transcriptionsRequest.getModel()));
        }
        if (StrUtil.isNotBlank(transcriptionsRequest.getPrompt())) {
            requestBodyMap.put(TranscriptionsRequest.Fields.prompt, RequestBody.create(MediaType.parse("multipart/form-data"), transcriptionsRequest.getPrompt()));
        }
        if (StrUtil.isNotBlank(transcriptionsRequest.getResponseFormat())) {
            requestBodyMap.put(TranscriptionsRequest.Fields.responseFormat, RequestBody.create(MediaType.parse("multipart/form-data"), transcriptionsRequest.getResponseFormat()));
        }
        requestBodyMap.put(TranscriptionsRequest.Fields.temperature, RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(transcriptionsRequest.getTemperature())));
        return this.configuration.getOpenAiApi().speed2TextTranscriptions(multipartBody, requestBodyMap).blockingGet();
    }

    @Override
    public WhisperResponse speed2TextTranslations(File file, TranslationsRequest translationsRequest) {
        // 1. 语音文件
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        // 2. 参数封装
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        if (StrUtil.isNotBlank(translationsRequest.getModel())) {
            requestBodyMap.put(TranslationsRequest.Fields.model, RequestBody.create(MediaType.parse("multipart/form-data"), translationsRequest.getModel()));
        }
        if (StrUtil.isNotBlank(translationsRequest.getPrompt())) {
            requestBodyMap.put(TranslationsRequest.Fields.prompt, RequestBody.create(MediaType.parse("multipart/form-data"), translationsRequest.getPrompt()));
        }
        if (StrUtil.isNotBlank(translationsRequest.getResponseFormat())) {
            requestBodyMap.put(TranslationsRequest.Fields.responseFormat, RequestBody.create(MediaType.parse("multipart/form-data"), translationsRequest.getResponseFormat()));
        }
        requestBodyMap.put(TranslationsRequest.Fields.temperature, RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(translationsRequest.getTemperature())));
        requestBodyMap.put(TranscriptionsRequest.Fields.temperature, RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(translationsRequest.getTemperature())));
        return this.configuration.getOpenAiApi().speed2TextTranscriptions(multipartBody, requestBodyMap).blockingGet();
    }

    @Override
    public Subscription subscription() {
        return this.configuration.getOpenAiApi().subscription().blockingGet();
    }

    @Override
    public BillingUsage billingUsage(@NotNull LocalDate starDate, @NotNull LocalDate endDate) {
        return this.configuration.getOpenAiApi().billingUsage(starDate, endDate).blockingGet();
    }
}
