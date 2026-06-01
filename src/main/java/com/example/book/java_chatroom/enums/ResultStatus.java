package com.example.book.java_chatroom.enums;

public enum ResultStatus {
   SUCCESS(200),
    FAIL(500),
    NOLOGIN(-2),
    ;
   private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    ResultStatus(int code){this.code=code;}


}
