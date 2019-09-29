package com.agney.agneyweb.service;

import com.agney.agneyweb.dto.CameraAggregateDataOutDto;
import com.agney.agneyweb.dto.CameraGeneralInfoInDto;
import com.agney.agneyweb.dto.CameraSourceDataInDto;
import com.agney.agneyweb.dto.CameraTokenDataInDto;
import com.agney.agneyweb.http.HttpClient;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CameraService {
    private final Logger logger = LoggerFactory.getLogger(CameraService.class);
    private final static int THREAD_COUNT = 10;

    private ExecutorService service;
    private ScheduledExecutorService scheduler;

    @Autowired
    private HttpClient httpClient;

    @Value("${camera.info.url}")
    private String cameraInfoUrl;
    @Value("${camera.future.timeout.single}")
    private Long cameraSingleTimeout;
    @Value("${camera.future.timeout.aggregate}")
    private Long cameraAggregateTimeout;

    /** Получить агрегированную информацию по камерам */
    public List<CameraAggregateDataOutDto> getInfoAggregate() {

        long start = System.currentTimeMillis();
        service = Executors.newFixedThreadPool(THREAD_COUNT);
        scheduler = Executors.newScheduledThreadPool(THREAD_COUNT);

        try {
            List<CameraGeneralInfoInDto> generalInfoes = Arrays.asList(httpClient.executeGetRequest(cameraInfoUrl, CameraGeneralInfoInDto[].class));
            List<CameraAggregateDataOutDto> list = new ArrayList<>(generalInfoes.size());

            CompletableFuture.allOf(
                generalInfoes.stream().map(c -> {

                    CameraAggregateDataOutDto.CameraAggregateDataOutDtoBuilder builder = CameraAggregateDataOutDto.builder();

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
                            .thenRun(() -> list.add(builder.id(c.getId()).build()));

                }).toArray(CompletableFuture[]::new)
            ).get(cameraAggregateTimeout, TimeUnit.MILLISECONDS);

            logger.info(String.format("Camera data aggregated in %s ms", System.currentTimeMillis() - start));
            return list;
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
            Assert.notNull(url, "url is null");
            Assert.notNull(clazz, "clazz is null");

            this.url = url;
            this.clazz = clazz;
        }

        @Override
        public Optional<A> call() {
            try {
                return Optional.of(httpClient.executeGetRequest(url, clazz));
            } catch (IOException | ResponseStatusException e) {
                logger.warn("Url request error: " + e.getMessage());
                return Optional.empty();
            }
        }
    }
}
