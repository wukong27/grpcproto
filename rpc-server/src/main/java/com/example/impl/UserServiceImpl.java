package com.example.impl;

import com.example.service.user.ResponseDto;
import com.example.service.user.User;
import com.example.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service(value = "userService")
public class UserServiceImpl implements UserService {
    private final AtomicLong idGenerator = new AtomicLong(1000);

    @Override
    public ResponseDto<User> createUser(User user) {
        user.setUserId(idGenerator.incrementAndGet());
        ResponseDto<User> ret = new ResponseDto<User>();
        ret.setCode(200);
        ret.setMessage("success");
        ret.setData(user);
        return ret;
    }
    /*@Override
    public void createUser(Universal.CallRequest request, StreamObserver<Universal.CallResponse> responseObserver) {
        try {
            // 模拟业务逻辑
            request.getPayload().getFieldsMap().get("name");
            User user = User.newBuilder()
                    .setId(idGenerator.incrementAndGet())
                    .setName(request.getPayload().getFieldsMap().get("name").getStringValue() + " 已成功注册")
                    .setAge((int)request.getPayload().getFieldsMap().get("age").getNumberValue())
                    .setEmail(request.getPayload().getFieldsMap().get("email").getStringValue())
                    .setCreateTime(Timestamps.fromMillis(System.currentTimeMillis()))
                    .setUpdateTime(Timestamps.fromMillis(System.currentTimeMillis()))
                    .build();
            String userJson = JsonFormat.printer().print(user);
            // 替换为：
            Struct.Builder structBuilder = Struct.newBuilder();
            JsonFormat.parser().merge(userJson, structBuilder);
            Struct userStruct = structBuilder.build();

            // 构建返回值（重点！）
            Universal.CallResponse response = Universal.CallResponse.newBuilder()
                    .setCode(0)
                    .setMessage("创建成功")
                    .setData(userStruct)
                    .build();

            // 重点：通过 onNext 返回！
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL
                    .withDescription("创建用户失败: " + e.getMessage())
                    .asRuntimeException());
        }
    }*/

}
