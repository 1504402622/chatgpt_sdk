package com.glfs.chatgpt.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import com.glfs.chatgpt.session.Configuration;
import com.glfs.chatgpt.session.OpenAiSession;
import com.glfs.chatgpt.session.OpenAiSessionFactory;
import com.glfs.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.Before;
import org.junit.Test;

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
        configuration.setApiKey("sk-mzSoSjdj9TjpDUKhD3B9Df1317644857A1341bA3433fF939");
        // 可以根据课程首页评论置顶说明获取 apihost、apikey；https://t.zsxq.com/0d3o5FKvc
        //configuration.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODMyODE2NzEsImlhdCI6MTY4MzI3ODA3MSwianRpIjoiMWUzZTkwYjYtY2UyNy00NzNlLTk5ZTYtYWQzMWU1MGVkNWE4IiwidXNlcm5hbWUiOiJ4ZmcifQ.YgQRJ2U5-9uydtd6Wbkg2YatsoX-y8mS_OJ3FdNRaX0");
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
     * 此对话模型 3.5 接近于官网体验 & 流式应答
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
}