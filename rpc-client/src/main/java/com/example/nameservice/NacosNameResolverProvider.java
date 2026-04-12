package com.example.nameservice;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;

import java.net.URI;
import java.util.Properties;

public class NacosNameResolverProvider extends NameResolverProvider {

    private final NamingService namingService;

    public NacosNameResolverProvider(String serverAddr) throws Exception {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        this.namingService = NamingFactory.createNamingService(properties);
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (!"nacos".equals(targetUri.getScheme())) {
            return null;
        }

        String serviceName = targetUri.getAuthority();
        return new NacosNameResolver(serviceName, namingService);
    }

    @Override
    public String getDefaultScheme() {
        return "nacos";
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }
}
