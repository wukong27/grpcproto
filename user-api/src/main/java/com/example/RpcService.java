package com.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    String serviceName() default ""; // 可选的服务名

    /**
     * 服务器名称，用于匹配配置文件中的 grpc.service.{serverName}.url
     * 此属性为必填项
     */
    String serverName();
}
