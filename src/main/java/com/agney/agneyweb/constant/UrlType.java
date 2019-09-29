package com.agney.agneyweb.constant;

import lombok.Getter;

@Getter
public enum UrlType {
    LIVE("LIVE"),
    ARCHIVE("ARCHIVE");

    String desc;

    UrlType(String desc) {
        this.desc = desc;
    }
}
