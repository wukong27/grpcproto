package com.nameservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 缓存 Spring 配置中的 Nacos 地址，避免注册逻辑重复解析配置。
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
