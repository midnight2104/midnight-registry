package com.midnight.registry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务实例的云数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {
    private String schema;
    private String host;
    private Integer port;
    private String context;
    /**
     * online or offline
     */
    private boolean status;

    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath().substring(1));
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toURL() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "midnight-rpc");
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }

    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }
}
