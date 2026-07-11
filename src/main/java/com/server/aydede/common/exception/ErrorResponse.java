package com.server.aydede.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String message, String code, String path, LocalDateTime timestamp) {

}