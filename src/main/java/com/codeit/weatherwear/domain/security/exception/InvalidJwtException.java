package com.codeit.weatherwear.domain.security.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class InvalidJwtException extends CustomException {
    
    public InvalidJwtException() {
        super(ErrorCode.INVALID_JWT);
    }
}
