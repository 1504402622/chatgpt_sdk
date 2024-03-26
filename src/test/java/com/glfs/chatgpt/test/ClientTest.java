package com.glfs.chatgpt.test;


import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import com.glfs.chatgpt.session.Configuration;
import com.glfs.chatgpt.session.OpenAiSession;
import com.glfs.chatgpt.session.OpenAiSessionFactory;
import com.glfs.chatgpt.session.defaults.DefaultOpenAiSessionFactory;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author 小傅哥，微信：fustack
 * @description 客户端输入测试
 * @github https://github.com/fuzhengwei
 * @Copyright 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class ClientTest {

    /*
    流式交流
     */
    public static void main(String[] args) throws InterruptedException {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://pro-share-aws-api.zcyai.com/");
        configuration.setApiKey("sk-mzSoSjdj9TjpDUKhD3B9Df1317644857A1341bA3433fF939");
        // 测试时候，需要先获得授权token：http://api.xfg.im:8080/authorize?username=xfg&password=123 - 此地址暂时有效，后续根据课程首页说明获取token；https://t.zsxq.com/0d3o5FKvc
        configuration.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODQ2MzEwNjAsImlhdCI6MTY4NDYyNzQ2MCwianRpIjoiMGU2M2Q3NDctNDk1YS00NDU3LTk1ZTAtOWVjMzkwNTlkNmQzIiwidXNlcm5hbWUiOiJ4ZmcifQ.xX4kaw-Pz2Jm4LBSvADzijud4nlNLFQUOaN6UgxrK9E");

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        OpenAiSession openAiSession = factory.openSession();

        System.out.println("我是 OpenAI ChatGPT，请输入你的问题：");

        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .messages(new ArrayList<>())
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .user("testUser01")
                .build();

        // 3. 等待输入
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String text = scanner.nextLine();
            chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content(text).build());
            ChatCompletionResponse chatCompletionResponse = openAiSession.completions(chatCompletion);
            // 输出结果
            System.out.println(chatCompletionResponse.getChoices().get(0).getMessage().getContent());
            //联系上文进行继续提问
            chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content(chatCompletionResponse.getChoices().get(0).getMessage().getContent()).build());
            System.out.println("请输入你的问题：");
        }

    }

}
