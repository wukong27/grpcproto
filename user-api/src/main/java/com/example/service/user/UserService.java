package com.example.service.user;

import com.example.RpcService;
import org.springframework.stereotype.Component;

@Component
@RpcService(value = "userService", serverName = "user")
public interface UserService {
    ResponseDto<User> createUser(User name);
}
