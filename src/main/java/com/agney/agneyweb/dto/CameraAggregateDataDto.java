package com.agney.agneyweb.dto;

import com.agney.agneyweb.constant.UrlType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CameraAggregateDataDto {
    private final Long    id;
    private final UrlType urlType;
    private final String  videoUrl;
    private final String  value;
    private final Integer ttl;

    public CameraAggregateDataDto(Long id, CameraSourceDataDto sourceData, CameraTokenDataDto tokenData) {
        this.id       = id;
        this.urlType  = sourceData.getUrlType();
        this.videoUrl = sourceData.getVideoUrl();
        this.value    = /*tokenData.getValue()*/ null;
        this.ttl      = /*tokenData.getTtl()*/ null;
    }
}
