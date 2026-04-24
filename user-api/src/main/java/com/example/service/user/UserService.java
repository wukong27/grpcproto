package com.example.service.user;

import com.example.RpcService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RpcService(serviceName = "userService", serverName = "userService")
public interface UserService {

//    ResponseDto<User> createUser(User name);
    ResponseDto<User> createUser(User name);


    default CompletableFuture<ResponseDto<User>> createUserAsync(User name){
        return new CompletableFuture<ResponseDto<User>>();
    };
}
