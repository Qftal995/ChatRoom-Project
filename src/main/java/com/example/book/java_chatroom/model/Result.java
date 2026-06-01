package com.example.book.java_chatroom.model;

import com.example.book.java_chatroom.enums.ResultStatus;
import lombok.Data;

@Data
public class Result<T> {
    private ResultStatus code;
    private String errMessage;
    private T data;

    public static <T> Result<T> success(T data) {
        Result result = new Result<>();
        result.setCode(ResultStatus.SUCCESS);
        result.setData(data);
        return result;
    }

    public static  Result nologin() {
        Result result = new Result<>();
        result.setCode(ResultStatus.NOLOGIN);
        result.setErrMessage("用户未登录");
        return result;
    }
    public static  Result fail(String msg) {
        Result result = new Result<>();
        result.setCode(ResultStatus.FAIL);
        result.setErrMessage(msg);;
        return result;
    }


}
