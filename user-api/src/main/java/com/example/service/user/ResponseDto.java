package com.example.service.user;

import lombok.Data;

@Data
public class ResponseDto<T> {
    private Integer code;
    private String message;
    private T data;

}
