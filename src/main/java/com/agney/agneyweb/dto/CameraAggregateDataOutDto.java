package com.agney.agneyweb.dto;

import com.agney.agneyweb.constant.UrlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/** Информация по камере */
@Getter
@ToString
@Builder
@AllArgsConstructor
public class CameraAggregateDataOutDto {
    /** ID камеры */
    private final Long    id;
    /** Тип ссылки на видеопоток */
    private final UrlType urlType;
    /** Ссылка на видеопоток */
    private final String  videoUrl;
    /** Токен безопасности */
    private final String  value;
    /** Время жизни токена */
    private final Integer ttl;
}
