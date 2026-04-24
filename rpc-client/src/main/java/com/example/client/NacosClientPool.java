package com.example.client;

import com.example.RpcService;
import com.example.config.NacosProperties;
import com.example.nameservice.NacosNameResolverProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Component
public class NacosClientPool {

    private final NacosProperties nacosProperties;
    private final Map<String,ManagedChannel> cacheChannel = new ConcurrentHashMap<>();
    public NacosClientPool(NacosProperties nacosProperties) {
        this.nacosProperties = nacosProperties;
    }

    public ManagedChannel getChannel(String serverName) throws Exception {
        if(StringUtils.hasText(serverName)){
            ManagedChannel channel = cacheChannel.get(serverName);
            if(channel == null || channel.isShutdown() || channel.isTerminated()){
                // 1. 注册自定义 Resolver
                NameResolverRegistry.getDefaultRegistry()
                        .register(new NacosNameResolverProvider(nacosProperties.getServerAddr()));

                // 2. 创建 Channel（注意 nacos://）
                channel = ManagedChannelBuilder
                        .forTarget("nacos://"+serverName)
                        .defaultLoadBalancingPolicy("round_robin")
                        .usePlaintext()
                        .executor(Executors.newFixedThreadPool(5))
                        .build();
                cacheChannel.putIfAbsent(serverName,channel);
                return channel;
            }else{
                return channel;
            }
        }else {
            return null;
        }
    }

}
