package com.agney.agneyweb.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/** Бин, через который проводятся все http-запросы */
@Component
public class HttpClient {

    private org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
    private ObjectMapper objectMapper = new ObjectMapper();

    public <A> A executeGetRequest(String url, Class<A> clazz) throws IOException {
        Assert.notNull(url, "url is null");
        Assert.notNull(clazz, "clazz is null");

        final HttpResponse response = httpClient.execute(new HttpGet(url));
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != HttpStatus.OK.value()) {
            throw new ResponseStatusException(HttpStatus.valueOf(responseCode),
                    String.format("Error response status from service. Code: %s", responseCode));
        }

        return objectMapper.readValue(response.getEntity().getContent(), clazz);
    }
}
