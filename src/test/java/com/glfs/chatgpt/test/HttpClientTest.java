package com.glfs.chatgpt.test;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import com.glfs.chatgpt.IOpenAiApi;
import io.reactivex.Single;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.POST;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
    测试chatgpt3.5
 */
@Slf4j
public class HttpClientTest {

    @Test
    public void test_client() {
        //创建一个用于记录http请求和响应信息的HttpLoggingInterceptor
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //创建OkhttpClient实例，配置拦截器
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(chain -> {
                    //从请求链中获得原始请求
                    Request original = chain.request();

                    //从原始请求中获取url，并添加token参数
                    HttpUrl url = original.url().newBuilder()
                            .addQueryParameter("token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODMyNzIyMjAsImlhdCI6MTY4MzI2ODYyMCwianRpIjoiOTkwMmM4MjItNzI2MC00OGEwLWI0NDUtN2UwZGZhOGVhOGYwIiwidXNlcm5hbWUiOiJ4ZmcifQ.Om7SdWdiIevvaWdPn7D9PnWS-ZmgbNodYTh04Tfb124")
                            .build();

                    //创建新的请求，将token添加在url中，并添加Authorization 和 Content-Type 头信息
                    Request now_request = original.newBuilder()
                            .url(url)
                            //添加授权信息"Bearer"令牌
                            .header(Header.AUTHORIZATION.getValue(), "Bearer " + "sk-mzSoSjdj9TjpDUKhD3B9Df1317644857A1341bA3433fF939")
                            //设置返回json类型
                            .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                            //设置了请求的方法（method）和请求体（body）。
                            .method(original.method(),original.body())
                            .build();
                    return chain.proceed(now_request);// 继续处理请求
                })
                .build();

        //返回值是通过动态代理实现接口IOpenAiApi的实体类吗
        IOpenAiApi openAiApi = new Retrofit.Builder()
                //基本 URL
                .baseUrl("https://pro-share-aws-api.zcyai.com/ ")
                //执行网络请求的 OkHttpClient 实例
                .client(okHttpClient)
                //支持在 Retrofit 接口方法中返回 RxJava 的 Observable 对象。
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //以支持 Retrofit 将 JSON 数据转换为 Java 对象。
                .addConverterFactory(JacksonConverterFactory.create())
                //使用 Retrofit.Builder 创建了 Retrofit 接口的实例，并通过 build() 方法完成构建。
                .build().create(IOpenAiApi.class);

        //问题封装
        Message message = Message.builder().role(Constants.Role.USER).content("给我okhttp3的介绍").build();
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                //将返回一个包含唯一元素 message 的列表。这个列表是不可修改的，意味着不能添加、删除或修改其中的元素。这种不可变性可以在某些情况下提供额外的安全性和性能优势。
                .messages(Collections.singletonList(message))
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .build();

        //Single<ChatCompletionResponse> 表示一个异步操作，它将返回一个 ChatCompletionResponse 对象或一个错误通知。
        Single<ChatCompletionResponse> chatCompletionResponseSingle = openAiApi.completions(chatCompletion);
        // 在调用 blockingGet() 方法时，它会等待异步操作完成，并获取最终的结果或错误
        ChatCompletionResponse chatCompletionResponse = chatCompletionResponseSingle.blockingGet();

        //答案输出
        chatCompletionResponse.getChoices().forEach(e -> {
            System.out.println("------回答成功:\n"+e.getMessage().getContent());
        });
    }

    @Test
    public void test_client_stream() throws JsonProcessingException, InterruptedException {
//创建一个用于记录http请求和响应信息的HttpLoggingInterceptor
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //创建OkhttpClient实例，配置拦截器
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(chain -> {
                    //从请求链中获得原始请求
                    Request original = chain.request();

                    //从原始请求中获取url，并添加token参数
                    HttpUrl url = original.url().newBuilder()
                            .addQueryParameter("token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODMyNzIyMjAsImlhdCI6MTY4MzI2ODYyMCwianRpIjoiOTkwMmM4MjItNzI2MC00OGEwLWI0NDUtN2UwZGZhOGVhOGYwIiwidXNlcm5hbWUiOiJ4ZmcifQ.Om7SdWdiIevvaWdPn7D9PnWS-ZmgbNodYTh04Tfb124")
                            .build();

                    //创建新的请求，将token添加在url中，并添加Authorization 和 Content-Type 头信息
                    Request now_request = original.newBuilder()
                            .url(url)
                            //添加授权信息"Bearer"令牌
                            .header(Header.AUTHORIZATION.getValue(), "Bearer " + "sk-mzSoSjdj9TjpDUKhD3B9Df1317644857A1341bA3433fF939")
                            //设置返回json类型
                            .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                            //设置了请求的方法（method）和请求体（body）。
                            .method(original.method(),original.body())
                            .build();
                    return chain.proceed(now_request);// 继续处理请求
                })
                .build();

        Message message = Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build();
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .messages(Collections.singletonList(message))
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .stream(true)//设置可用流式输出
                .build();


        EventSource.Factory factory = EventSources.createFactory(okHttpClient);
        //将 chatCompletion 对象转换为 JSON 字符串
        String requestBody = new ObjectMapper().writeValueAsString(chatCompletion);

        Request request = new Request.Builder()
                .url("https://pro-share-aws-api.zcyai.com/v1/chat/completions")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestBody))
                .build();


        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                log.info("测试结果：{}", data);
            }
        });

        // 阻断主线程，等待其他线程调用(实际上event线程不会返回所以一直阻断)
        new CountDownLatch(1).await();

    }
}
