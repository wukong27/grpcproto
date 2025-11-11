package com.example;

import com.example.universal.Universal;
import com.example.universal.UniversalServiceGrpc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GrpcGatewayFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 仅拦截 /grpc/hello
        if (!path.startsWith("/universal/Invoke")) {
            return chain.filter(exchange);
        }

        return exchange.getRequest().getBody()
                .reduce(new StringBuilder(), (sb, dataBuffer) -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    sb.append(new String(bytes, StandardCharsets.UTF_8));
                    return sb;
                })
                .flatMap(body -> {
                    try {
                        // 解析 JSON 请求
                        JsonNode jsonNode = objectMapper.readTree(body.toString());
                        // 构建 Universal.CallRequest
                        String jsonString = jsonNode.get("payload").toString();
                        Struct.Builder structBuilder = Struct.newBuilder();
                        JsonFormat.parser().merge(jsonString, structBuilder);
                        Struct payload = structBuilder.build();
                        Universal.CallRequest callRequest = Universal.CallRequest.newBuilder()
                                .setService(jsonNode.get("service").asText())  // 根据实际服务名调整
                                .setMethod(jsonNode.get("method").asText())  // 根据实际方法名调整
                                .setPayload(payload)
                                .build();

                        // 调用 gRPC 服务
                        ManagedChannel channel = ManagedChannelBuilder
                                .forAddress("localhost", 9090)
                                .usePlaintext()
                                .build();

                        UniversalServiceGrpc.UniversalServiceBlockingStub stub = UniversalServiceGrpc.newBlockingStub(channel);

                        // 调用 UniversalServiceImpl 的 invoke 方法
                        Universal.CallResponse response = stub.invoke(callRequest);

                        channel.shutdown();
                        // 将结果返回 HTTP 响应
                        String responseJson = JsonFormat.printer().includingDefaultValueFields().print(response);
                        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponse().setStatusCode(HttpStatus.OK);
                        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                        return exchange.getResponse()
                                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));

                    } catch (Exception e) {
                        e.printStackTrace();
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return -1; // 优先级高
    }
}
