
package com.mediator.auth.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupConfig {

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi studentApi() {
        return GroupedOpenApi.builder()
                .group("Student")
                .pathsToMatch("/api/v1/students/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tutorApi() {
        return GroupedOpenApi.builder()
                .group("Tutor")
                .pathsToMatch("/api/v1/tutors/**")
                .build();
    }

    @Bean
    public GroupedOpenApi matchingApi() {
        return GroupedOpenApi.builder()
                .group("Matching")
                .pathsToMatch("/api/v1/matches/**")
                .build();
    }

    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
                .group("Tutor Search")
                .pathsToMatch("/api/v1/search/**")
                .build();
    }

    @Bean
    public GroupedOpenApi subscriptionApi() {
        return GroupedOpenApi.builder()
                .group("Subscription")
                .pathsToMatch("/api/v1/subscriptions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi masterApi() {
        return GroupedOpenApi.builder()
                .group("Master Data")
                .pathsToMatch("/api/v1/master/**")
                .build();
    }
}
