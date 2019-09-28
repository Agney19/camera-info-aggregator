package com.agney.agneyweb.dto;

import com.agney.agneyweb.constant.UrlType;
import com.agney.agneyweb.model.CameraDataResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CameraSourceDataDto implements CameraDataResponse {
    private final UrlType urlType;
    private final String  videoUrl;

    @JsonCreator
    public CameraSourceDataDto(@JsonProperty("urlType")  UrlType urlType,
                               @JsonProperty("videoUrl") String  videoUrl) {
        this.urlType  = urlType;
        this.videoUrl = videoUrl;
    }
}
