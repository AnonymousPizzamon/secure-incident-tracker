package com.alden.logmonitoringapp;

import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

// VIA_DTO renders Page responses in the stable PagedModel json shape instead of PageImpl internals
// https://docs.spring.io/spring-data/commons/reference/repositories/core-extensions.html
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class LogMonitoringAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogMonitoringAppApplication.class, args);
    }

}
