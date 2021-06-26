package io.axoniq.training.labs;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public EventBus eventBus() {
        return SimpleEventBus.builder().build().subscribe();
    }
}
