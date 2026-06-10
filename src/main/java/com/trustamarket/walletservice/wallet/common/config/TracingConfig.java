package com.trustamarket.walletservice.wallet.common.config;

import java.net.http.HttpClient;

import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinHttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

@Configuration
public class TracingConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    @Bean
    public ZipkinHttpClientBuilderCustomizer zipkinHttp1Customizer() {
        return builder -> builder.version(HttpClient.Version.HTTP_1_1);
    }
}
