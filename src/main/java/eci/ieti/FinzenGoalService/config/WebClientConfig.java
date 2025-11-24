package eci.ieti.FinzenGoalService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${transactions.service.url}")
    private String transactionServiceUrl;

    @Bean
    public WebClient transactionWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(transactionServiceUrl)
                .build();
    }
}