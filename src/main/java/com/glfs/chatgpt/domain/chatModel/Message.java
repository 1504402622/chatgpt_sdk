package com.glfs.chatgpt.domain.chatModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.glfs.chatgpt.common.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*信息描述*/
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)/*当一个 Java 对象被序列化为 JSON 时，有些字段可能为 null，在某些场景下可能希望排除这些空值字段，以减少 JSON 的大小*/
public class Message implements Serializable {
    private String role;
    private String content;
    private String name;

    //role
    private Message(Builder builder) {
        this.role = builder.role;
        this.content = builder.content;
        this.name = builder.name;
    }

    public static Builder builder(){return  new Builder();}

    /**
     * 建造者模式
     */
    public static final class Builder{
        private String role;
        private String content;
        private String name;
        public Builder(){}

        //请求角色类型；system、user、assistant
        public Builder role(Constants.Role role){
            this.role = role.getCode();
            return this;
        }

        public Builder content(String content){
            this.content = content;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        //建造完成返回对象Message
        public Message build(){return new Message(this);}

    }

}
