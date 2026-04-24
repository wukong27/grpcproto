package com.example.client;

import com.alibaba.fastjson2.JSON;
import com.example.RpcService;
import com.example.universal.Universal;
import com.example.universal.UniversalServiceGrpc;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@SuppressWarnings("unchecked")
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
    public T getObject() throws Exception {
        NacosClientPool nacosClientPool = applicationContext.getBean(NacosClientPool.class);
        String serverName = getServerName();
        ManagedChannel channel = nacosClientPool.getChannel(serverName);
        if(channel==null){
            log.error("serverName:{},channel is null,please check your config for the {} server.",serverName,serverName);
            return null;
        }
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(),new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    // === 核心: 将方法调用转发到 gRPC 服务 ===
                    String serviceName = interfaceType.getSimpleName();
                    String methodName = method.getName();
                    System.out.println("调用远程 gRPC 服务: " + serviceName + "." + methodName);
                    
                    // 构建请求参数
                    Struct.Builder structBuilder = Struct.newBuilder();
                    JsonFormat.parser().merge(JSON.toJSONString(args[0]), structBuilder);
                    Struct payload = structBuilder.build();
                    Universal.CallRequest callRequest = Universal.CallRequest.newBuilder()
                            .setService(serviceName)
                            .setMethod(methodName)
                            .setPayload(payload)
                            .build();
                    
                    // 判断是否为异步方法（返回CompletableFuture且方法名以Async结尾）
                    if (isAsyncMethod(method)) {
                        return executeAsync(channel, callRequest, method, args);
                    } else {
                        return executeSync(channel, callRequest, method);
                    }
                }
        );
    }

    /**
     * 判断是否为异步方法（返回CompletableFuture且方法名以Async结尾）
     */
    private boolean isAsyncMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        String methodName = method.getName();
        
        // 返回类型是CompletableFuture且方法名以Async结尾
        return CompletableFuture.class.isAssignableFrom(returnType) && methodName.endsWith("Async");
    }
    
    /**
     * 执行异步调用（使用gRPC newStub）
     */
    private Object executeAsync(ManagedChannel channel, Universal.CallRequest callRequest, Method method, Object[] args) {
        // 获取方法返回类型的泛型参数（CompletableFuture<T>中的T）
        Class<?> genericType = resolveGenericReturnType(method);
        
        CompletableFuture<Object> future = new CompletableFuture<>();
        
        UniversalServiceGrpc.UniversalServiceStub stub = UniversalServiceGrpc.newStub(channel);
        stub.invoke(callRequest, new StreamObserver<Universal.CallResponse>() {
            private boolean completed = false;
            @Override
            public void onNext(Universal.CallResponse response) {
                if (!completed) {
                    try {
                        Object result = convertResponse(response, genericType);
                        future.complete(result);
                        completed = true;
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                        completed = true;
                    }
                }
            }
            
            @Override
            public void onError(Throwable t) {
                if (!completed) {
                    future.completeExceptionally(new RuntimeException("gRPC异步调用失败: " + t.getMessage(), t));
                    completed = true;
                }
            }
            
            @Override
            public void onCompleted() {
                // 确保future一定会完成
                if (!completed) {
                    // 如果没有收到任何响应，完成一个null值或抛出异常
                    if (genericType == Void.TYPE || genericType == Void.class) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new RuntimeException("gRPC调用完成但未收到响应数据"));
                    }
                    completed = true;
                }
            }
        });
        
        return future;  // 直接返回CompletableFuture，不阻塞
    }
    
    /**
     * 执行同步调用（使用gRPC newBlockingStub）
     */
    private Object executeSync(ManagedChannel channel, Universal.CallRequest callRequest, Method method) {
        UniversalServiceGrpc.UniversalServiceBlockingStub stub = UniversalServiceGrpc.newBlockingStub(channel);
        Universal.CallResponse response = stub.invoke(callRequest);
        
        // 获取方法的返回类型
        Class<?> returnType = method.getReturnType();
        Object result;
        
        if (returnType == Universal.CallResponse.class) {
            // 如果返回类型就是 CallResponse，直接返回
            result = response;
        } else if (returnType == Void.TYPE || returnType == Void.class) {
            // 如果返回类型是 void，返回 null
            result = null;
        } else {
            // 转换为期望的返回类型
            result = convertResponse(response, returnType);
        }
        
        return result;
    }
    
    /**
     * 解析方法返回类型的泛型参数
     */
    private Class<?> resolveGenericReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        
        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            
            // 获取CompletableFuture<T>中的T类型
            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
                return (Class<?>) actualTypeArguments[0];
            }
        }
        
        // 如果无法解析泛型，返回Object类型
        return Object.class;
    }
    
    /**
     * 转换响应数据为指定类型
     */
    private Object convertResponse(Universal.CallResponse response, Class<?> targetType) {
        try {
            Struct data = response.getData();
            String dataStr = JsonFormat.printer().includingDefaultValueFields().print(data);
            
            if (targetType == Universal.CallResponse.class) {
                return response;
            } else if (targetType == Void.TYPE || targetType == Void.class) {
                return null;
            } else {
                return JSON.parseObject(dataStr, targetType);
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("响应数据转换失败", e);
        }
    }

    public Object execute(
            ManagedChannel channel,
            Function<ManagedChannel, UniversalServiceGrpc.UniversalServiceBlockingStub> stubFactory,
            Function<UniversalServiceGrpc.UniversalServiceBlockingStub,Object> action) {
        if (channel == null || channel.isShutdown()) {
            throw new IllegalStateException("gRPC channel is null or shutdown");
        }
        
        if (stubFactory == null) {
            throw new IllegalArgumentException("stubFactory cannot be null");
        }
        
        if (action == null) {
            throw new IllegalArgumentException("action cannot be null");
        }

        UniversalServiceGrpc.UniversalServiceBlockingStub stub = stubFactory.apply(channel);
        if (stub == null) {
            throw new IllegalStateException("Failed to create gRPC stub");
        }
        
        return action.apply(stub);
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
        if (rpcServiceAnnotation != null && !rpcServiceAnnotation.serviceName().isEmpty()) {
            return rpcServiceAnnotation.serviceName();
        }
        // 默认使用简单类名
        return interfaceType.getSimpleName();
    }
    private String getServerName() {
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