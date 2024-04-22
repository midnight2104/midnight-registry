package com.midnight.registry.service;

import com.midnight.registry.cluster.Snapshot;
import com.midnight.registry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class MidnightRegistryService implements RegistryService {
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();

    private final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    private final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static AtomicLong VERSION = new AtomicLong(0);

    public static synchronized long restore(Snapshot snapshot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getRegistry());

        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVersions());

        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTimestamps());

        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }

    public static synchronized Snapshot snapshot() {
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        HashMap<String, Long> versions = new HashMap<>(VERSIONS);
        HashMap<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (!CollectionUtils.isEmpty(metas) && metas.contains(instance)) {
            log.info(" ===>  instance {} already registered", instance.toURL());
            instance.setStatus(true);
            return instance;
        }

        log.info(" ===>  register instance {}", instance.toURL());
        REGISTRY.add(service, instance);
        instance.setStatus(true);

        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());

        return instance;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (CollectionUtils.isEmpty(metas)) {
            return null;
        }

        log.info(" ===>  unregister instance {}", instance.toURL());
        metas.removeIf(m -> m.equals(instance));
        instance.setStatus(false);

        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());

        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }

    @Override
    public long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toURL(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> b));
    }
}
