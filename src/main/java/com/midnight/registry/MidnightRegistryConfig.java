package com.midnight.registry;

import com.midnight.registry.cluster.Cluster;
import com.midnight.registry.service.MidnightRegistryService;
import com.midnight.registry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidnightRegistryConfig {

    @Bean
    public RegistryService registryService() {
        return new MidnightRegistryService();
    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired MidnightRegistryConfigProperties registryConfigProperties) {
        return new Cluster(registryConfigProperties);
    }
}
