package com.agney.agneyweb.dto;

import com.agney.agneyweb.model.CameraDataResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CameraTokenDataDto implements CameraDataResponse {
    private final String  value;
    private final Integer ttl;

    @JsonCreator
    public CameraTokenDataDto(@JsonProperty("value") String  value,
                              @JsonProperty("ttl")   Integer ttl) {
        this.value = value;
        this.ttl   = ttl;
    }
}
