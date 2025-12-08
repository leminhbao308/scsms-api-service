package com.kltn.scsms_api_service.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServerSideException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;
    private final Object[] messageArgs;
    private final String category;
    private final Object data;
    private final Exception exception;

    @Builder
    private ServerSideException(
            ErrorCode errorCode,
            String message,
            Object[] messageArgs,
            String category,
            Object data,
            Exception exception) {
        super(message, exception);
        this.errorCode = errorCode;
        this.message = message;
        this.messageArgs = messageArgs;
        this.category = category;
        this.data = data;
        this.exception = exception;
    }

    public ServerSideException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
        this.message = null;
        this.messageArgs = null;
        this.category = null;
        this.data = null;
        this.exception = null;
    }

    public ServerSideException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.messageArgs = null;
        this.category = null;
        this.data = null;
        this.exception = null;
    }

    public ServerSideException(ErrorCode errorCode, String message, Exception exception) {
        super(message, exception);
        this.errorCode = errorCode;
        this.message = message;
        this.messageArgs = null;
        this.category = null;
        this.data = null;
        this.exception = exception;
    }
}
