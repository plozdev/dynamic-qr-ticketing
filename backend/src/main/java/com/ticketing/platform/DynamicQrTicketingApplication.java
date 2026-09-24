package com.ticketing.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DynamicQrTicketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicQrTicketingApplication.class, args);
    }

}
