package com.example.service;

import com.example.service.user.ResponseDto;
import com.example.service.user.User;
import com.example.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    public UserController() {

    }

/*    @PostMapping("/user/create")
    public ResponseDto<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }*/

    @PostMapping("/user/create")
    public CompletableFuture<ResponseDto<User>> getUserInfo(@RequestBody User request) {
        CompletableFuture<ResponseDto<User>> ret = userService.createUserAsync(request)
                .thenApply(response -> response)
                .exceptionally(error ->{
                    ResponseDto<User> r = new ResponseDto<User>();
                    r.setCode(500);
                    r.setMessage(error.getMessage());
                    return r;
                });
        return ret;
    }
}
