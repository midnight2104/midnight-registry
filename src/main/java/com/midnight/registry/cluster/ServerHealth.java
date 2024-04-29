package com.midnight.registry.cluster;

import com.midnight.registry.http.HttpInvoker;
import com.midnight.registry.service.MidnightRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务状态探活
 */
@Slf4j
public class ServerHealth {
    private Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final long interval = 5_000;

    public void checkServerHealth() {

        executor.scheduleAtFixedRate(() -> {
            try {
                // 1.更新每个服务的信息
                updateServers();

                // 2.选主
                doElect();

                // 3.从主节点同步快照信息
                syncSnapshotFromLeader();

            } catch (Exception e) {
                log.error("选举出错了", e);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);

    }

    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        log.debug(" ===>>> leader version: " + leader.getVersion()
                + ", my version: " + self.getVersion());

        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ===>>> sync snapshot from leader: " + leader);
            Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);

            log.debug(" ===>>> sync and restore snapshot: " + snapshot);
            MidnightRegistryService.restore(snapshot);
        }
    }

    private void doElect() {
        new Election().electLeader(cluster.getServers());
    }

    private void updateServers() {
        List<Server> servers = cluster.getServers();
        servers.stream().parallel().forEach(server -> {
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
        });

    }

}
