package com.glfs.chatgpt.session.defaults;

import com.glfs.chatgpt.interceptor.OpenAiInterceptor;
import com.glfs.chatgpt.session.Configuration;
import com.glfs.chatgpt.IOpenAiApi;
import com.glfs.chatgpt.session.OpenAiSession;
import com.glfs.chatgpt.session.OpenAiSessionFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

public class DefaultOpenAiSessionFactory implements OpenAiSessionFactory {

    private final Configuration configuration;
    public DefaultOpenAiSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }
    @Override
    public OpenAiSession openSession() {
        //1.日志配置
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        //2.开启Http客户端
        // 2. 开启 Http 客户端
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new OpenAiInterceptor(configuration.getApiKey(), configuration.getAuthToken()))
                .connectTimeout(450, TimeUnit.SECONDS)
                .writeTimeout(450, TimeUnit.SECONDS)
                .readTimeout(450, TimeUnit.SECONDS)
                //设置代理服务器
                //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 21284)))
                .build();
        configuration.setOkHttpClient(okHttpClient);

        // 3. 创建 API 服务
        IOpenAiApi openAiApi = new Retrofit.Builder()
                .baseUrl(configuration.getApiHost())
                .client(okHttpClient)
                //支持在 Retrofit 接口方法中返回 RxJava 的 Observable 对象。
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //以支持 Retrofit 将 JSON 数据转换为 Java 对象。
                .addConverterFactory(JacksonConverterFactory.create())
                .build().create(IOpenAiApi.class);
        configuration.setOpenAiApi(openAiApi);

        return new DefaultOpenAiSession(configuration);
    }
}
