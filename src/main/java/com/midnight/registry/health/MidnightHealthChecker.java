package com.midnight.registry.health;

import com.midnight.registry.model.InstanceMeta;
import com.midnight.registry.service.MidnightRegistryService;
import com.midnight.registry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MidnightHealthChecker implements HealthChecker {
    public RegistryService registryService;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final long timeout = 20_000;

    public MidnightHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            log.info("Health Checker is running");
            long now = System.currentTimeMillis();
            MidnightRegistryService.TIMESTAMPS.keySet().forEach(serviceAndInst -> {
                long timestamp = MidnightRegistryService.TIMESTAMPS.get(serviceAndInst);
                if (now - timestamp > timeout) {
                    log.info(" ===> health checker: {} is down", serviceAndInst);

                    int index = serviceAndInst.indexOf("@");
                    String service = serviceAndInst.substring(0, index);
                    String url = serviceAndInst.substring(index + 1);
                    InstanceMeta instance = InstanceMeta.from(url);

                    registryService.unregister(service, instance);
                    MidnightRegistryService.TIMESTAMPS.remove(serviceAndInst);
                }
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
