package com.agney.agneyweb.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@Getter
@ToString
public class CameraGeneralInfoDto {
    private final Long   id;
    private final String sourceDataUrl;
    private final String tokenDataUrl;

    @JsonCreator
    public CameraGeneralInfoDto(@JsonProperty("id")            Long   id,
                                @JsonProperty("sourceDataUrl") String sourceDataUrl,
                                @JsonProperty("tokenDataUrl")  String tokenDataUrl) {
        this.id            = id;
        this.sourceDataUrl = sourceDataUrl;
        this.tokenDataUrl  = tokenDataUrl;
    }
}
