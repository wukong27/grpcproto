package com.example.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 通用的 gRPC 客户端连接池封装
 */
public class GrpcClientPool {

    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private final int maxPoolSize;
    private final String host;
    private final int port;
    private final Queue<ManagedChannel> channelPool = new ConcurrentLinkedQueue<>();
    private final Map<ManagedChannel, Long> lastUsedMap = new ConcurrentHashMap<>();

    public GrpcClientPool(String host, int port) {
        this(host, port, DEFAULT_MAX_POOL_SIZE);
    }

    public GrpcClientPool(String host, int port, int maxPoolSize) {
        this.host = host;
        this.port = port;
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * 获取一个可用的 ManagedChannel
     */
    private synchronized ManagedChannel getChannel() {
        ManagedChannel channel = channelPool.poll();
        if (channel == null || channel.isShutdown() || channel.isTerminated()) {
            channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext() // 视情况改为 TLS
                    .build();
        }
        lastUsedMap.put(channel, System.currentTimeMillis());
        return channel;
    }

    /**
     * 归还 Channel 到池中
     */
    private void returnChannel(ManagedChannel channel) {
        if (channelPool.size() < maxPoolSize && !channel.isShutdown()) {
            lastUsedMap.put(channel, System.currentTimeMillis());
            channelPool.offer(channel);
        } else {
            shutdownChannel(channel);
        }
    }

    /**
     * 通用执行逻辑
     * @param stubFactory 负责从 Channel 创建 stub
     * @param action      执行具体 RPC 调用的逻辑
     */
    public <T extends AbstractStub<T>, R> R execute(
            Function<ManagedChannel, T> stubFactory,
            Function<T, R> action
    ) {
        ManagedChannel channel = getChannel();
        try {
            T stub = stubFactory.apply(channel);
            return action.apply(stub);
        } finally {
            returnChannel(channel);
        }
    }

    /**
     * 清理闲置 Channel
     */
    public void cleanIdleChannels(long maxIdleMillis) {
        long now = System.currentTimeMillis();
        for (ManagedChannel ch : channelPool) {
            Long lastUsed = lastUsedMap.getOrDefault(ch, 0L);
            if (now - lastUsed > maxIdleMillis) {
                channelPool.remove(ch);
                shutdownChannel(ch);
            }
        }
    }

    /**
     * 关闭所有连接
     */
    public void shutdown() {
        for (ManagedChannel ch : channelPool) {
            shutdownChannel(ch);
        }
        channelPool.clear();
        lastUsedMap.clear();
    }

    private void shutdownChannel(ManagedChannel ch) {
        try {
            ch.shutdown().awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

