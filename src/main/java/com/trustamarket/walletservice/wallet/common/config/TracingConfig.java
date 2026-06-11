package com.trustamarket.walletservice.wallet.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
public class TracingConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    @Bean
    public Sender zipkinSender(@Value("${management.zipkin.tracing.endpoint:http://zipkin:9411/api/v2/spans}") String endpoint) {
        return OkHttpSender.create(endpoint);
    }
}
