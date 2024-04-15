package com.midnight.registry;

import com.midnight.registry.service.MidnightRegistryService;
import com.midnight.registry.service.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidnightRegistryConfig {

    @Bean
    public RegistryService registryService(){
        return new MidnightRegistryService();
    }


}
