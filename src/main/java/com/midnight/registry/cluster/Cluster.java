package com.midnight.registry.cluster;

import com.midnight.registry.MidnightRegistryConfigProperties;
import com.midnight.registry.http.HttpInvoker;
import com.midnight.registry.service.MidnightRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 注册中心集群
 */

@Slf4j
public class Cluster {

    @Value("${server.port}")
    private String port;

    private String host;

    private Server myself;

    @Getter
    private List<Server> servers;

    private MidnightRegistryConfigProperties registryConfigProperties;


    public Cluster(MidnightRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.debug("=====> findFirstNonLoopbackHostInfo: " + host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        myself = new Server("http://" + host + ":" + port, true, false, -1L);
        log.debug("=====> myself = : " + myself);

        this.servers = getServerList();

        new ServerHealth(this).checkServerHealth();
    }

    @NotNull
    private List<Server> getServerList() {
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }

            if (url.equals(myself.getUrl())) {
                servers.add(myself);
            } else {
                Server server = Server.builder().url(url).status(false).leader(false).version(-1L).build();
                servers.add(server);
            }
        }
        return servers;
    }



    public Server self() {
        myself.setVersion(MidnightRegistryService.VERSION.get());
        return myself;
    }

    public Server leader() {
        return servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .findFirst()
                .orElse(null);
    }
}
