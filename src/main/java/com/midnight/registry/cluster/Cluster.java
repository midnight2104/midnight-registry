package com.midnight.registry.cluster;

import com.midnight.registry.MidnightRegistryConfigProperties;
import com.midnight.registry.http.HttpInvoker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

    private MidnightRegistryConfigProperties registryConfigProperties;

    @Getter
    private List<Server> servers;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final long timeout = 20_000;

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

        this.servers = servers;

        executor.scheduleAtFixedRate(() -> {
            try {
                // 更新每个服务的信息
                updateServers();

                // 选主
                electLeader();
            } catch (Exception e) {
                log.error("选举出错了", e);
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);

    }

    /**
     * 每个实例自己选，保证最终选择是一样的
     */
    private void electLeader() {
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);

            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    // 选择hash值最小的为主
                    if (candidate.hashCode() > server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
    }

    private void updateServers() {
        for (Server server : servers) {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug("===> health check success for " + serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.debug("===> health check failed for " + server);
                server.setStatus(false);
                server.setLeader(false);
            }
        }
    }

    public Server self() {
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
