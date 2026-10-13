package com.licensis.notaire.config;

import com.licensis.notaire.adapter.out.pdf.ReportTheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Shared clock for deterministic PDF timestamps (issue #1593).
 */
@Configuration
public class ReportInfrastructureConfig {

    @Bean
    public Clock reportClock() {
        return Clock.system(ReportTheme.ZONE);
    }
}
