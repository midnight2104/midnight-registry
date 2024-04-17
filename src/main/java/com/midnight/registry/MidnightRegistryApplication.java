package com.midnight.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({MidnightRegistryConfigProperties.class})
public class MidnightRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MidnightRegistryApplication.class, args);
    }

}
