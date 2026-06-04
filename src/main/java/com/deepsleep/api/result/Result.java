package com.deepsleep.api.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Result<T> (
        Integer code,
        T data,
        String msg
) {

    @JsonIgnore
    public ResultCode resultCode() {
        return ResultCode.of(code);
    }

    @JsonIgnore
    public boolean success() {
        return ResultCode.SUCCESS.matches(code);
    }
}
