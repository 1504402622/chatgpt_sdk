package com.glfs.chatgpt.domain.other;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

//** 响应结果的耗材 */
@Data
public class Usage implements Serializable {

    /**
     * 提示令牌
     */
    @JsonProperty("prompt_tokens")
    private long promptTokens;
    /**
     * 完成令牌
     */
    @JsonProperty("completion_tokens")
    private long completionTokens;
    /**
     * 总量令牌
     */
    @JsonProperty("total_tokens")
    private long totalTokens;
}
