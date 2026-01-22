package com.nyihtuun.bentosystem.subscriptionservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RestClient restClient(SubscriptionConfigData configData) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(configData.connectTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(configData.readTimeoutSeconds()));

        return RestClient.builder()
                         .baseUrl(configData.planManagementServiceUrl())
                         .requestFactory(requestFactory)
                         .build();
    }
}
