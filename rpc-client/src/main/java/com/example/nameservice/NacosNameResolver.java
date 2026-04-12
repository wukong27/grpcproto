package com.example.nameservice;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.listener.NamingEvent;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 对接nacos，动态获取服务端信息{[ip:port,ip:port,ip:port]}
 */
public class NacosNameResolver extends NameResolver {

    private final String serviceName;
    private final NamingService namingService;
    private Listener2 listener;

    public NacosNameResolver(String serviceName, NamingService namingService) {
        this.serviceName = serviceName;
        this.namingService = namingService;
    }

    @Override
    public String getServiceAuthority() {
        return "nacos";
    }

    @Override
    public void start(Listener2 listener) {
        this.listener = listener;

        try {
            // 1. 初始化拉取
            List<Instance> instances = namingService.getAllInstances(serviceName);
            updateAddresses(instances);

            // 2. 订阅变化（核心）
            namingService.subscribe(serviceName, event -> {
                if (event instanceof NamingEvent) {
                    List<Instance> newInstances = ((NamingEvent) event).getInstances();
                    updateAddresses(newInstances);
                }
            });

        } catch (Exception e) {
            listener.onError(Status.UNAVAILABLE.withDescription(e.getMessage()));
        }
    }

    private void updateAddresses(List<Instance> instances) {
        List<EquivalentAddressGroup> addresses = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.isHealthy()) {
                InetSocketAddress address =
                        new InetSocketAddress(instance.getIp(), instance.getPort());

                addresses.add(new EquivalentAddressGroup(address));
            }
        }

        ResolutionResult result = ResolutionResult.newBuilder()
                .setAddresses(addresses)
                .build();

        listener.onResult(result);
    }

    @Override
    public void shutdown() {
        // 可选：取消订阅
    }
}