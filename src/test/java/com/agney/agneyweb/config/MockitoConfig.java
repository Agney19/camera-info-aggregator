package com.agney.agneyweb.config;

import com.agney.agneyweb.constant.UrlType;
import com.agney.agneyweb.dto.CameraGeneralInfoInDto;
import com.agney.agneyweb.dto.CameraSourceDataInDto;
import com.agney.agneyweb.dto.CameraTokenDataInDto;
import com.agney.agneyweb.http.HttpClient;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MockitoConfig {
    @Value("${camera.info.url}")
    private String cameraInfoUrl;
    @Value("${camera.future.timeout.single}")
    private Long maxSingleTimeout;

    @Bean
    public HttpClient httpClient() throws IOException {
        final String okSourceUrl = "okSourceUrl";
        final String okTokenUrl = "okTokenUrl";

        final String timeoutSourceUrl = "timeoutSourceUrl";
        final String timeoutTokenUrl = "timeoutTokenUrl";

        final CameraGeneralInfoInDto[] generalInfo = {
                new CameraGeneralInfoInDto(1L, "okSourceUrl", "okTokenUrl"),
                new CameraGeneralInfoInDto(2L, "timeoutSourceUrl", "timeoutTokenUrl")
        };

        final CameraSourceDataInDto okSourceInfo =
                new CameraSourceDataInDto(UrlType.LIVE, "videoOkSourceUrl");
        final CameraTokenDataInDto okTokenInfo =
                new CameraTokenDataInDto("tokenOkValue", 1);
        final CameraSourceDataInDto timeoutSourceInfo =
                new CameraSourceDataInDto(UrlType.ARCHIVE, "videoOkSourceUrl");
        final CameraTokenDataInDto timeoutTokenInfo =
                new CameraTokenDataInDto("tokenTimeoutValue", 2);

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.when(httpClient.executeGetRequest(cameraInfoUrl, CameraGeneralInfoInDto[].class)).thenReturn(generalInfo);
        Mockito.when(httpClient.executeGetRequest(okSourceUrl, CameraSourceDataInDto.class)).thenReturn(okSourceInfo);
        Mockito.when(httpClient.executeGetRequest(okTokenUrl, CameraTokenDataInDto.class)).thenReturn(okTokenInfo);
        Mockito.when(httpClient.executeGetRequest(timeoutSourceUrl, CameraSourceDataInDto.class))
                .thenAnswer((Answer) invocation -> {
                    Thread.sleep(maxSingleTimeout + 1000);
                    return timeoutSourceInfo;
                });
        Mockito.when(httpClient.executeGetRequest(timeoutTokenUrl, CameraTokenDataInDto.class))
                .thenAnswer((Answer) invocation -> {
                    Thread.sleep(maxSingleTimeout + 1000);
                    return timeoutTokenInfo;
                });
        return httpClient;
    }
}
