package com.kltn.scsms_api_service.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientSideException extends RuntimeException {

    private final ErrorCode code;
    private final String message;
    private final Object[] messageArgs;
    private final Object data;
    private final Exception exception;

    @Builder
    private ClientSideException(
            ErrorCode code, String message, Object[] messageArgs, Object data, Exception exception) {
        super(message, exception);
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
        this.data = data;
        this.exception = exception;
    }

    public ClientSideException(ErrorCode code) {
        super();
        this.code = code;
        this.message = null;
        this.messageArgs = null;
        this.data = null;
        this.exception = null;
    }

    public ClientSideException(ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageArgs = null;
        this.data = null;
        this.exception = null;
    }

    public ClientSideException(ErrorCode code, String message, Exception exception) {
        super(message, exception);
        this.code = code;
        this.message = message;
        this.messageArgs = null;
        this.data = null;
        this.exception = exception;
    }

    public ClientSideException(ErrorCode code, String message, Object... messageArgs) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
        this.data = null;
        this.exception = null;
    }

    public ClientSideException(
            ErrorCode code, String message, Exception exception, Object... messageArgs) {
        super(message, exception);
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
        this.data = null;
        this.exception = exception;
    }
}
