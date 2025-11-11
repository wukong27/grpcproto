package com.example.service;

import com.example.service.user.ResponseDto;
import com.example.service.user.User;
import com.example.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    public UserController() {

    }

    @PostMapping("/user/create")
    public ResponseDto<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}
