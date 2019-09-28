package com.agney.agneyweb.service;

import com.agney.agneyweb.constant.UrlType;
import com.agney.agneyweb.dto.CameraAggregateDataDto;
import com.agney.agneyweb.dto.CameraGeneralInfoDto;
import com.agney.agneyweb.dto.CameraSourceDataDto;
import com.agney.agneyweb.model.CameraDataResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CameraService {
    private final Logger logger = LoggerFactory.getLogger(CameraService.class);

    private HttpClient httpClient = HttpClientBuilder.create().build();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${camera.info.url}")
    private String cameraInfoUrl;
    @Value("${camera.future.timeout.single}")
    private Long cameraSingleTimeout;
    @Value("${camera.future.timeout.aggregate}")
    private Long cameraAggregateTimeout;

    int threadCount;
    ExecutorService service;
    ScheduledExecutorService scheduler;

    public List<CameraAggregateDataDto> getInfoAggregate() {

        threadCount = /*Runtime.getRuntime().availableProcessors();*/10;//TODO
        service = Executors.newFixedThreadPool(threadCount);
        scheduler = Executors.newScheduledThreadPool(threadCount);

        try {
            HttpResponse response = httpClient.execute(new HttpGet(cameraInfoUrl));
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.valueOf(responseCode),
                        String.format("Error response status from service. Code: %s", responseCode));
            }
            Stream<CameraGeneralInfoDto> stream = Arrays.stream(objectMapper.readValue(response.getEntity().getContent(), CameraGeneralInfoDto[].class));
//
//            List<CameraAggregateDataDto> list = stream
//              .map(c -> service.submit(new CameraUrlRequest(c.getSourceDataUrl())))
//              .map(c -> {
//                  try {
//                      return c.get(cameraTimeout, TimeUnit.MILLISECONDS);
//                  } catch (Exception e) {
//                      logger.warn("Obtaining value from future failed!");
//                      CameraDataResponse a = new CameraSourceDataDto(UrlType.LIVE, "asd");
//                      return Optional.of(a);
//                  }
//              })
//              .filter(Optional::isPresent)
//              .map(c -> new CameraAggregateDataDto(14L, (CameraSourceDataDto)c.get(), null))
//              .collect(Collectors.toList());

            List<CameraSourceDataDto> l = new ArrayList<>();


            CompletableFuture.allOf(
                    stream.map(c -> {

                        ScheduledFuture<Optional<CameraSourceDataDto>> timeoutF = scheduler.schedule(new CameraUrlStubRequest(), cameraSingleTimeout, TimeUnit.MILLISECONDS);
                        CompletableFuture<Optional<CameraSourceDataDto>> f1 = CompletableFuture.supplyAsync(() -> {
                            try {
                                return timeoutF.get();
                            } catch (Exception e) {
                                throw new RuntimeException();
                            }
                        });

                        CompletableFuture<Optional<CameraSourceDataDto>> f0 = CompletableFuture.supplyAsync(
                                () -> new CameraUrlRequest(c.getSourceDataUrl()).call(), service
                        );

                        return CompletableFuture.anyOf(f0, f1).thenApply(r -> {
                            ((Optional<CameraSourceDataDto>)r).ifPresent(l::add);
                            return r;
                        });

                    }).toArray(CompletableFuture[]::new)
            ).get(cameraAggregateTimeout, TimeUnit.MILLISECONDS);
            /*(() -> l.stream()
                    .filter(Optional::isPresent)
                    .map(d -> new CameraAggregateDataDto(14L, (CameraSourceDataDto) d.get(), null))
                    .collect(Collectors.toList())
            ).get(2000, TimeUnit.MILLISECONDS)*/
            ;

//            Thread.sleep(2000);
//            System.out.println(a.get());

//            List<CameraAggregateDataDto> b = l.stream().filter(Optional::isPresent)
//                    .map(d -> new CameraAggregateDataDto(14L, (CameraSourceDataDto) d.get(), null))
//                    .collect(Collectors.toList());

//            System.out.println(b);
            return l.stream()
                    .map(a -> new CameraAggregateDataDto(123L, a, null))
                    .collect(Collectors.toList());
//            stream.forEach(e -> {
//                Optional<CameraDataResponse> data = service.submit(new CameraUrlRequest(e.getSourceDataUrl())).get(cameraTimeout, TimeUnit.MILLISECONDS);
//            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <A> CompletableFuture<A> executeWithTimeout(Future<A> f, Long timeout) {
        ScheduledFuture<Optional<CameraSourceDataDto>> timeoutF = scheduler.schedule(new CameraUrlStubRequest(), cameraSingleTimeout, TimeUnit.MILLISECONDS);
        CompletableFuture<Optional<CameraSourceDataDto>> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                return timeoutF.get();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });

        CompletableFuture<Optional<CameraSourceDataDto>> f0 = CompletableFuture.supplyAsync(
                () -> new CameraUrlRequest(c.getSourceDataUrl()).call(), service
        );
    }

    @Getter
    @ToString
    public class CameraUrlRequest implements Callable<Optional<CameraSourceDataDto>> {
        private final String url;

        public CameraUrlRequest(String url) {
            this.url = url;
        }

        @Override
        public Optional<CameraSourceDataDto> call() {
            try {
                // TODO: для пороверки, что фьючи выполняются параллельно
                System.out.println(url + " start");

                final HttpResponse response = HttpClientBuilder.create().build().execute(new HttpGet(url));
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.OK.value()) {
                    throw new ResponseStatusException(HttpStatus.valueOf(responseCode),
                            String.format("Error response status from service. Code: %s", responseCode));
                }
                final CameraSourceDataDto data = objectMapper.readValue(response.getEntity().getContent(), CameraSourceDataDto.class);
                System.out.println(url + " finish; data: " + data);
                return Optional.of(data);
            } catch (IOException | ResponseStatusException e) {
                logger.warn("Url request error: " + e.getMessage());
                System.out.println(url + " finish");
                return Optional.empty();
            }
        }
    }

    @Getter
    @ToString
    public class CameraUrlStubRequest implements Callable<Optional<CameraSourceDataDto>> {

        @Override
        public Optional<CameraSourceDataDto> call() {
            return Optional.of(new CameraSourceDataDto(UrlType.LIVE, "empty"));
        }
    }
}
