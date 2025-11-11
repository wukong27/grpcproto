package com.example.impl;

import com.alibaba.fastjson2.JSON;
import com.example.RpcService;
import com.example.service.user.ResponseDto;
import com.example.universal.Universal;
import com.example.universal.UniversalServiceGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import com.google.protobuf.util.JsonFormat;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.protobuf.Value;
import org.springframework.stereotype.Component;

@GrpcService
@RequiredArgsConstructor
public class UniversalServiceImpl extends UniversalServiceGrpc.UniversalServiceImplBase {
    private final ApplicationContext ctx;

    @Override
    public void invoke(Universal.CallRequest req, StreamObserver<Universal.CallResponse> observer) {
        try {// 查找添加了自定义注解@RpcService注解的bean
//            Object bean = ctx.getBeansWithAnnotation(RpcService.class).values().stream()
//                    .filter(b -> {
//                        String simpleName = b.getClass().getSimpleName();
//                        // 移除 "Impl" 后缀进行精确匹配
//                        String serviceName = simpleName.replace("Impl", "");
//                        return serviceName.equals(req.getService().toString());
//                    })
//                    .findFirst()
//                    .orElseThrow(() -> Status.NOT_FOUND.asRuntimeException());
            Object bean = ctx.getBean(req.getService());
            // 添加注解检查
//            if (!bean.getClass().isAnnotationPresent(RpcService.class)) {
//                throw new IllegalArgumentException("Bean " + req.getService() + " does not have @RpcService annotation");
//            }
            Method method = Arrays.stream(bean.getClass().getMethods())
                    .filter(m -> m.getName().equalsIgnoreCase(req.getMethod()))
                    .findFirst().orElseThrow(Status.UNIMPLEMENTED::asRuntimeException);

            String payload = JsonFormat.printer().includingDefaultValueFields().print(req.getPayload());
            Object args0 = JSON.parseObject(payload, method.getParameterTypes()[0]);
            Object result = method.invoke(bean, args0);
            //如果方式是本地方法,不是grpc调用, UniversalService作为统一入口，将结果返回给调用方,
            Universal.CallResponse response;
            if(result instanceof ResponseDto<?>){
                var data= ((ResponseDto<?>) result).getData();
                Struct userStruct = getStruct(data);
                response = Universal.CallResponse.newBuilder()
                        .setCode(((ResponseDto<?>) result).getCode())
                        .setMessage(((ResponseDto<?>) result).getMessage())
                        .setData(userStruct)
                        .build();
            }else{
                Struct userStruct = getStruct(result);
                response = Universal.CallResponse.newBuilder()
                        .setCode(200)
                        .setMessage("success")
                        .setData(userStruct)
                        .build();
            }
            observer.onNext(response);
            observer.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private static @NotNull Struct getStruct(Object data) throws JsonProcessingException, InvalidProtocolBufferException {
        String jsonString = new ObjectMapper().writeValueAsString(data);
        Struct.Builder structBuilder = Struct.newBuilder();
        JsonFormat.parser().merge(jsonString, structBuilder);
        Struct userStruct = structBuilder.build();
        return userStruct;
    }
}
