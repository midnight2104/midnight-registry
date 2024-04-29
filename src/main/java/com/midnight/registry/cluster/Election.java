package com.midnight.registry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 选举
 */
@Slf4j
public class Election {

    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).toList();
        if (masters.isEmpty()) {
            log.warn(" ===>>>  elect for no leader: " + servers);
            elect(servers);
        } else if (masters.size() > 1) {
            log.warn(" ===>>> elect for more than one leader: " + servers);
            elect(servers);
        } else {
            log.debug(" ===>>> no need election for leader: " + masters.get(0));
        }
    }

    /**
     * 每个实例自己选，保证最终选择是一样的
     */
    private void elect(List<Server> servers) {
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

        if (candidate != null) {
            candidate.setLeader(true);
            log.debug(" ===>>> elect for leader: " + candidate);
        } else {
            log.debug(" ===>>> elect failed for no leaders: " + servers);
        }
    }

}
