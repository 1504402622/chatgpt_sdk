package com.glfs.chatgpt.interceptor;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OpenAiInterceptor implements Interceptor {
    /** OpenAi apiKey 需要在官网申请 */
    private String apiKey;
    /** 访问授权接口的认证 Token */
    private String authToken;
    public OpenAiInterceptor(String apiKey, String authToken) {
        this.apiKey = apiKey;
        this.authToken = authToken;
    }


    //作用是在发送请求之前，通过拦截器将身份验证信息添加到请求中
    //使用了 Java 中的注解 @NotNull 来标记该方法不接受 null 参数
    /*
    在方法中调用了 this.auth(apiKey, chain.request()) 方法来添加认证信息，
    然后通过 chain.proceed() 方法继续处理请求链。
    从代码逻辑上看，这段代码的作用是在拦截器中添加认证信息，并将带有认证信息的请求继续传递下去。
     */
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(this.auth(apiKey, chain.request()));
    }

    //认证方法
    private Request auth(String apiKey, Request original) {
        // 设置Token信息；如果没有此类限制，是不需要设置的。
        HttpUrl url = original.url().newBuilder()
                .addQueryParameter("token", authToken)
                .build();

        // 创建请求
        return original.newBuilder()
                .url(url)
                .header(Header.AUTHORIZATION.getValue(), "Bearer " + apiKey)
                .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                .method(original.method(), original.body())
                .build();
    }
}
