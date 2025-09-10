package com.nttdata.loanvalidation.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides an injectable Clock to ease time-dependent tests. */
@Configuration
public class ClockConfig {
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
