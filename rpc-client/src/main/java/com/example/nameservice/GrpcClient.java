package com.example.nameservice;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;

/**
 * 测试注册当前
 */
public class GrpcClient {

    public static void main(String[] args) throws Exception {

        // 1. 注册自定义 Resolver
        NameResolverRegistry.getDefaultRegistry()
                .register(new NacosNameResolverProvider("127.0.0.1:8848"));

        // 2. 创建 Channel（注意 nacos://）
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("nacos://test-service")
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        // 3. 这里就可以正常调用 gRPC 了
        // YourGrpcServiceGrpc.newBlockingStub(channel)...

        System.out.println("gRPC client started with Nacos discovery");
    }
}