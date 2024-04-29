package com.midnight.registry.cluster;

import com.midnight.registry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {
    private LinkedMultiValueMap<String, InstanceMeta> registry;
    private Map<String, Long> versions;
    private Map<String, Long> timestamps;
    private long version;
}
