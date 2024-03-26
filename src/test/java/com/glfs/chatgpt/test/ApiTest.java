package com.glfs.chatgpt.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.common.Constants;
import com.alibaba.fastjson.JSON;
import com.glfs.chatgpt.domain.billing.BillingUsage;
import com.glfs.chatgpt.domain.billing.Subscription;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import com.glfs.chatgpt.domain.edits.EditRequest;
import com.glfs.chatgpt.domain.edits.EditResponse;
import com.glfs.chatgpt.domain.embedd.EmbeddingResponse;
import com.glfs.chatgpt.domain.files.DeleteFileResponse;
import com.glfs.chatgpt.domain.files.UploadFileResponse;
import com.glfs.chatgpt.domain.images.ImageEnum;
import com.glfs.chatgpt.domain.images.ImageRequest;
import com.glfs.chatgpt.domain.images.ImageResponse;
import com.glfs.chatgpt.domain.other.OpenAiResponse;
import com.glfs.chatgpt.session.Configuration;
import com.glfs.chatgpt.session.OpenAiSession;
import com.glfs.chatgpt.session.OpenAiSessionFactory;
import com.glfs.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApiTest {

    private OpenAiSession openAiSession;


    @Before
    public void test_OpenAiSessionFactory() {
        // 1. 配置文件【如果你从小傅哥获取key会给你提供apihost，你可以分别替换下】
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://pro-share-aws-api.zcyai.com/");
        configuration.setApiKey("sk-x1J6vfuoi5FiBF3y2d8e8fB4E8B94f189140A962D0C0B9Ed");
        // 可以根据课程首页评论置顶说明获取 apihost、apikey；https://t.zsxq.com/0d3o5FKvc
        configuration.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODMyODE2NzEsImlhdCI6MTY4MzI3ODA3MSwianRpIjoiMWUzZTkwYjYtY2UyNy00NzNlLTk5ZTYtYWQzMWU1MGVkNWE4IiwidXNlcm5hbWUiOiJ4ZmcifQ.YgQRJ2U5-9uydtd6Wbkg2YatsoX-y8mS_OJ3FdNRaX0");
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
    }


    //测试v1/chat/completions端口
    @Test
    public void test_chat_completions() {
        // 1. 创建参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                //角色设置
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("给我二分查找模板").build()))
                //模型设置
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .build();
        // 2. 发起请求
        ChatCompletionResponse chatCompletionResponse = openAiSession.completions(chatCompletion);
        // 3. 解析结果
        chatCompletionResponse.getChoices().forEach(e -> {
            log.info("测试结果：{}", e.getMessage());
        });
    }

    /**
     * gtp3.5
     */
    @Test
    public void test_chat_completions_stream() throws JsonProcessingException, InterruptedException {
        // 1. 创建参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .build();

        //增加阻断
        CountDownLatch latch = new CountDownLatch(1);
        // 2. 发起请求
        EventSource eventSource = openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                log.info("测试结果：{}", data);
            }

            //事件关闭时调用计数器减少
            @Override
            public void onClosed(EventSource eventSource){
                // 减少计数器，确保计数器减少到0以便释放阻塞
                latch.countDown();
            }
        });

        // 创建 CountDownLatch，用于等待事件输出完成
        latch.await();
    }

    /**
     * gtp3.5上下文对话（测试）
     */
    @Test
    public void  test_chat_completions_context(){
        //创建请求参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .messages(new ArrayList<>())
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .user("testUser01")
                .build();
        //写入请求信息
        chatCompletion.getMessages().add(Message
                .builder()
                .role(Constants.Role.USER)
                .content("写一个java冒泡排序")
                .build());
        //第一次发起请求
        ChatCompletionResponse completions = openAiSession.completions(chatCompletion);

        //写入第二次请求并在内容中添加第一次请求的响应结果
        chatCompletion.getMessages().add(Message
                .builder()
                .role(Constants.Role.USER)
                .content(completions.getChoices().get(0).getMessage().getContent())
                .build());
        chatCompletion.getMessages().add(Message
                .builder()
                .role(Constants.Role.USER)
                .content("再一个其他的实现语言")
                .build());
        //发送请求
        ChatCompletionResponse completions1 = openAiSession.completions(chatCompletion);
        log.info("测试结果：{}", completions1.getChoices());
    }

    /**
     * gpt3.5/4.0生成图片(不可用)
     * @throws InterruptedException
     */
    @Test
    public void test_images() throws InterruptedException {
        ImageRequest request = ImageRequest.builder()
                .prompt("生成一只柴犬")
                .model(ImageRequest.Model.DALL_E_3.getCode())
                .size(ImageEnum.Size.size_1024.getCode())
                .build();

        ImageResponse imageResponse = openAiSession.genImages(request);

        log.info("测试结果：{}", JSON.toJSONString(imageResponse.getData()));

        // 等待
        new CountDownLatch(1).await();
    }


    
//---------------------------------------------------------------------------------

    /**
     * 生成图片/可用
     */
    @Test
    public void test_genImages() {
        // 方式1，简单调用
        ImageResponse imageResponse01 = openAiSession.genImages("画一个996加班的程序员");
        log.info("测试结果：{}", imageResponse01);

//        // 方式2，调参调用
//        ImageResponse imageResponse02 = openAiSession.genImages(ImageRequest.builder()
//                .prompt("画一个996加班的程序员")
//                .size(ImageEnum.Size.size_256.getCode())
//                .responseFormat(ImageEnum.ResponseFormat.B64_JSON.getCode()).build());
//        log.info("测试结果：{}", imageResponse02);
    }

    /**
     * 修改图片/不可用
     * @throws IOException
     */
    @Test
    public void test_editImages() throws IOException {
        ImageResponse imageResponse = openAiSession.editImages(new File("/Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/chatgpt-sdk-java/docs/images/996.png"), "去除图片中的文字");
        log.info("测试结果：{}", imageResponse);
    }


    /**
     * 文本修复/不可用
     */
    @Test
    public void test_edit() {
        // 文本请求
        EditRequest textRequest = EditRequest.builder()
                .input("码农会锁")
                .instruction("帮我修改错字")
                .model(EditRequest.Model.TEXT_DAVINCI_EDIT_001.getCode()).build();
        EditResponse textResponse = openAiSession.edit(textRequest);
        log.info("测试结果：{}", textResponse);

        // 代码请求
        EditRequest codeRequest = EditRequest.builder()
                // j <= 10 应该修改为 i <= 10
                .input("for (int i = 1; j <= 10; i++) {\n" +
                        "    System.out.println(i);\n" +
                        "}")
                .instruction("这段代码执行时报错，请帮我修改").model(EditRequest.Model.CODE_DAVINCI_EDIT_001.getCode()).build();
        EditResponse codeResponse = openAiSession.edit(codeRequest);
        log.info("测试结果：{}", codeResponse);
    }



    /**
     * 向量计算：将文本转换为数字形式/不可用
     */
    @Test
    public void test_embeddings() {
        EmbeddingResponse embeddingResponse = openAiSession.embeddings("哈喽", "嗨", "hi!");
        log.info("测试结果：{}", embeddingResponse);
    }

    /**
     * 文件列表；在你上传文件到服务端后，可以获取列表信息/不可用
     */
    @Test
    public void test_files() {
        OpenAiResponse<File> openAiResponse = openAiSession.files();
        log.info("测试结果：{}", openAiResponse);
    }

    /**
     * 上传文件；上传一个文件/不可用
     */
    @Test
    public void test_uploadFile() {
        UploadFileResponse uploadFileResponse = openAiSession.uploadFile(new File("/Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/chatgpt-sdk-java/docs/files/introduce.md"));
        log.info("测试结果：{}", uploadFileResponse);
    }

    /**
     * 删除文件/不可用
     */
    @Test
    public void test_deleteFile() {
        DeleteFileResponse deleteFileResponse = openAiSession.deleteFile("file id 上传后才能获得");
        log.info("测试结果：{}", deleteFileResponse);
    }


    /**
     * 账单查询/可用
     */
    @Test
    public void test_subscription() {
        Subscription subscription = openAiSession.subscription();
        log.info("测试结果：{}", subscription);
    }

    /**
     * 消耗查询/可用
     */
    @Test
    public void test_billingUsage() {
        BillingUsage billingUsage = openAiSession.billingUsage(LocalDate.of(2024, 3, 10), LocalDate.now());
        log.info("测试结果：{}", billingUsage.getTotalUsage());
    }
}