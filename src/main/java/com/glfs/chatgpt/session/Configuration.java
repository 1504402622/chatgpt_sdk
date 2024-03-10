package com.glfs.chatgpt.session;

import com.glfs.chatgpt.IOpenAiApi;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;

/**
 * @description 配置信息
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    @Getter
    @Setter
    private IOpenAiApi openAiApi;

    @Getter
    @Setter
    private OkHttpClient okHttpClient;

    @Getter
    @NotNull
    private String apiKey;

    @Getter
    private String apiHost;

    @Getter
    private String authToken;

    public EventSource.Factory createRequestFactory() {
        return EventSources.createFactory(okHttpClient);
    }
}
