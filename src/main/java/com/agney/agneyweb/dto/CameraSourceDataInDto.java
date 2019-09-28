package com.agney.agneyweb.dto;

import com.agney.agneyweb.constant.UrlType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CameraSourceDataInDto {
    private final UrlType urlType;
    private final String  videoUrl;

    @JsonCreator
    public CameraSourceDataInDto(@JsonProperty("urlType")  UrlType urlType,
                                 @JsonProperty("videoUrl") String  videoUrl) {
        this.urlType  = urlType;
        this.videoUrl = videoUrl;
    }
}
