package com.glfs.chatgpt.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用类(1.设置Roke枚举类)
 */
public class Constants {

    @Getter
    @AllArgsConstructor
    public enum Role{
        //Role.SYSTEM==new Role("system)
        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant"),
        ;
        private String code;
    }
}
