package com.agney.agneyweb.service;

import com.agney.agneyweb.dto.CameraAggregateDataOutDto;
import com.agney.agneyweb.dto.CameraGeneralInfoInDto;
import com.agney.agneyweb.dto.CameraSourceDataInDto;
import com.agney.agneyweb.dto.CameraTokenDataInDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

@Service
public class CameraService {
    private final Logger logger = LoggerFactory.getLogger(CameraService.class);
    private final static int THREAD_COUNT = 10;

    private HttpClient httpClient = HttpClientBuilder.create().build();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${camera.info.url}")
    private String cameraInfoUrl;
    @Value("${camera.future.timeout.single}")
    private Long cameraSingleTimeout;
    @Value("${camera.future.timeout.aggregate}")
    private Long cameraAggregateTimeout;

    private ExecutorService service;
    private ScheduledExecutorService scheduler;

    /** Получить агрегированную информацию по камерам */
    public List<CameraAggregateDataOutDto> getInfoAggregate() {

        long start = System.currentTimeMillis();
        service = Executors.newFixedThreadPool(THREAD_COUNT);
        scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);

        try {
            HttpResponse response = httpClient.execute(new HttpGet(cameraInfoUrl));
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(responseCode),
                        String.format("Error response status from service. Code: %s", responseCode));
            }
            Stream<CameraGeneralInfoInDto> stream = Arrays.stream(objectMapper.readValue(response.getEntity().getContent(), CameraGeneralInfoInDto[].class));

            List<CameraAggregateDataOutDto> l = new ArrayList<>();
            CompletableFuture.allOf(
                stream.map(c -> {

                    CameraAggregateDataOutDto.CameraAggregateDataDtoBuilder builder = CameraAggregateDataOutDto.builder();

                    CompletableFuture<Optional<CameraSourceDataInDto>> sourceFuture = executeWithTimeout(
                        CompletableFuture.supplyAsync(() -> new CameraUrlRequest<>(c.getSourceDataUrl(), CameraSourceDataInDto.class).call(), service),
                        Optional.empty(),
                        cameraSingleTimeout
                    );
                    sourceFuture.thenApply(opt -> {
                        opt.ifPresent(r -> {
                            builder.urlType(r.getUrlType());
                            builder.videoUrl(r.getVideoUrl());
                        });
                        return opt;
                    });

                    CompletableFuture<Optional<CameraTokenDataInDto>> tokenFuture = executeWithTimeout(
                        CompletableFuture.supplyAsync(() -> new CameraUrlRequest<>(c.getTokenDataUrl(), CameraTokenDataInDto.class).call(), service),
                        Optional.empty(),
                        cameraSingleTimeout
                    );
                    tokenFuture.thenApply(opt -> {
                        opt.ifPresent(r -> {
                            builder.value(r.getValue());
                            builder.ttl(r.getTtl());
                        });
                        return opt;
                    });

                    return CompletableFuture
                            .allOf(sourceFuture, tokenFuture)
                            .thenRun(() -> l.add(builder.id(c.getId()).build()));

                }).toArray(CompletableFuture[]::new)
            ).get(cameraAggregateTimeout, TimeUnit.MILLISECONDS);

            logger.info(String.format("Camera data aggregated in %s ms", System.currentTimeMillis() - start));
            return l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Возвращает {@link CompletableFuture} в зависимости от времени выполнения {@param future}
     * @param future      Future для выполнения за {@param timeout}
     * @param backupValue Значение в случае невыполнения {@param future}
     * @param timeout     Время ожидания {@param future}
     */
    @SuppressWarnings("unchecked")
    private <A> CompletableFuture<A> executeWithTimeout(CompletableFuture<A> future, A backupValue, Long timeout) {
        ScheduledFuture<A> timeoutF = scheduler.schedule(
                () -> backupValue, timeout, TimeUnit.MILLISECONDS
        );
        CompletableFuture<A> timeoutFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return timeoutF.get();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
        return (CompletableFuture<A>)CompletableFuture.anyOf(future, timeoutFuture);
    }

    @Getter
    @ToString
    public class CameraUrlRequest<A> implements Callable<Optional<A>> {
        private final String url;
        private final Class<A> clazz;

        CameraUrlRequest(String url, Class<A> clazz) {
            this.url = url;
            this.clazz = clazz;
        }

        @Override
        public Optional<A> call() {
            try {
                final HttpResponse response = HttpClientBuilder.create().build().execute(new HttpGet(url));
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.OK.value()) {
                    throw new ResponseStatusException(HttpStatus.valueOf(responseCode),
                            String.format("Error response status from service. Code: %s", responseCode));
                }
                final A data = objectMapper.readValue(response.getEntity().getContent(), clazz);
                return Optional.of(data);
            } catch (IOException | ResponseStatusException e) {
                logger.warn("Url request error: " + e.getMessage());
                return Optional.empty();
            }
        }
    }
}
