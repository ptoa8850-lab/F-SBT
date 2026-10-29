package com.inspien.toto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** @Scheduled 가 동작하도록 스케줄링 기능을 켠다. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
