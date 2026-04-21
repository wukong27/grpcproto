package com.example.service.user;

import com.example.RpcService;
import org.springframework.stereotype.Component;

@Component
@RpcService(serviceName = "userService", serverName = "userService")
public interface UserService {
    ResponseDto<User> createUser(User name);
}
