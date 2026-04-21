package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 缓存 Spring 配置中的 Nacos 地址，避免业务代码重复读取配置源。
 */
@Component
public class NacosProperties {

    private final String serverAddr;

    public NacosProperties(@Value("${cloud.nacos.discovery.server-addr:127.0.0.1:8848}") String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getServerAddr() {
        return serverAddr;
    }
}
