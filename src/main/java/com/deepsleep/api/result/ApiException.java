package com.deepsleep.api.result;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final Integer code;
    private final ResultCode resultCode;
    private final int httpStatus;

    public ApiException(Integer code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.resultCode = ResultCode.of(code);
        this.httpStatus = httpStatus;
    }

    public ApiException(ResultCode resultCode, int httpStatus) {
        this(resultCode.getCode(), resultCode.getMsg(), httpStatus);
    }

}
