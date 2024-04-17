package com.midnight.registry.cluster;

import lombok.*;

/**
 * 注册中心服务实例
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor@EqualsAndHashCode(of={"url"})
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;
}
