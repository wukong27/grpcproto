package com.example.client;

import com.alibaba.fastjson2.JSON;
import com.example.RpcService;
import com.example.universal.Universal;
import com.example.universal.UniversalServiceGrpc;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Proxy;

public class RpcProxyFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private final Class<T> interfaceType;
    private ApplicationContext applicationContext;
    private Environment environment;

    public RpcProxyFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
    }

    private GrpcClientPool channelPool;

    @Override
    public T getObject() {
        // 在此处进行配置检查
        if(!initializeChannelPool()){
            return null;
        }
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    // === 核心: 将方法调用转发到 gRPC 服务 ===
                    String serviceName = interfaceType.getSimpleName();
                    String methodName = method.getName();
                    System.out.println("调用远程 gRPC 服务: " + serviceName + "." + methodName);

                    // 假设这里通过 gRPC stub 调用远程服务
                    // return grpcClient.call(serviceName, methodName, args);
                    Struct.Builder structBuilder = Struct.newBuilder();
                    String argsJson = JSON.toJSONString(args[0]);
                    JsonFormat.parser().merge(argsJson, structBuilder);
                    Struct payload = structBuilder.build();
                    Universal.CallRequest callRequest = Universal.CallRequest.newBuilder()
                            .setService(serviceName)  // 根据实际服务名调整
                            .setMethod(methodName)  // 根据实际方法名调整
                            .setPayload(payload)
                            .build();

                    // 调用 gRPC 服务
                    Object ret = channelPool.execute(channel -> UniversalServiceGrpc.newBlockingStub(channel),
                            stub -> {
                                Universal.CallResponse response = stub.invoke(callRequest);
                                // 获取方法的返回类型
                                Class<?> returnType = method.getReturnType();

                                if (returnType == Universal.CallResponse.class) {
                                    // 如果返回类型就是 CallResponse，直接返回
                                    return response;
                                } else if (returnType == Void.TYPE || returnType == Void.class) {
                                    // 如果返回类型是 void，返回 null
                                    return null;
                                } else {
                                    Struct data = response.getData();
                                    // 将 Struct 数据转换为期望的返回类型
                                    String dataStr = null;
                                    try {
                                        dataStr = JsonFormat.printer().includingDefaultValueFields().print(data);
                                        // 方案一：使用 ObjectMapper 反序列化
                                        return JSON.parseObject(dataStr, returnType);
                                    } catch (InvalidProtocolBufferException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                    return ret;
                }
        );
    }

    private boolean initializeChannelPool() {
        // 获取服务名称 - 可以来自 @RpcService 注解或者默认使用类名
        String serviceName = getServiceName();

        // 构造配置键
        String configKey = "grpc.service." + serviceName + ".url";

        // 从环境变量中获取配置
        String serviceUrl = environment.getProperty(configKey);

        if (serviceUrl == null || serviceUrl.isEmpty()) {
            // 如果没有找到配置，可以使用默认值或者抛出异常
            System.out.println("未找到服务配置: " + configKey + "，使用默认地址");
            return false;
        }

        // 解析主机和端口
        String[] parts = serviceUrl.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        // 初始化连接池
        this.channelPool = new GrpcClientPool(host, port, 1);
        return true;
    }

    private String getServiceName() {
        // 检查接口上是否有 @RpcService 注解并获取 name 属性
        RpcService rpcServiceAnnotation = interfaceType.getAnnotation(RpcService.class);
        if (rpcServiceAnnotation != null && !rpcServiceAnnotation.serverName().isEmpty()) {
            return rpcServiceAnnotation.serverName();
        }
        // 默认使用简单类名
        return interfaceType.getSimpleName();
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
