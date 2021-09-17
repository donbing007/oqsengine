package com.xforceplus.ultraman.oqsengine.testcontainer.utils.rest;

import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class RestSender {
    private static final RestTemplate restTemplate;

    static {
        restTemplate = new RestTemplate(factoryGenerator());
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    private static ClientHttpRequestFactory factoryGenerator() {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout(600_000);
        httpRequestFactory.setConnectTimeout(5_000);

        return httpRequestFactory;
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static Map<ContainerSupport, Integer> gets(String url, ParameterizedTypeReference<Map<ContainerSupport, Integer>> parameterizedTypeReference) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map<ContainerSupport, Integer>> postEntity = restTemplate.exchange(url,
            HttpMethod.GET,
            new HttpEntity<>(httpHeaders),
            parameterizedTypeReference);

        return postEntity.getBody();
    }

    public static <T, V> V get(String url, Class<V> vClass) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<V> postEntity = restTemplate.exchange(url,
            HttpMethod.GET,
            new HttpEntity<>(httpHeaders),
            vClass);

        return postEntity.getBody();
    }
}
