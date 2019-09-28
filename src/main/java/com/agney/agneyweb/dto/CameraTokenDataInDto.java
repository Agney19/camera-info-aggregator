package com.agney.agneyweb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CameraTokenDataInDto {
    private final String  value;
    private final Integer ttl;

    @JsonCreator
    public CameraTokenDataInDto(@JsonProperty("value") String  value,
                                @JsonProperty("ttl")   Integer ttl) {
        this.value = value;
        this.ttl   = ttl;
    }
}
