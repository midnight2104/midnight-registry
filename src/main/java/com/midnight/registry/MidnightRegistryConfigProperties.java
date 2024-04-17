package com.midnight.registry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "midnightregistry")
public class MidnightRegistryConfigProperties {

    private List<String> serverList;
}
