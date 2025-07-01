package com.codeit.weatherwear.domain.security.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class JwtSessionNotFoundException extends CustomException {

    public JwtSessionNotFoundException() {
        super(ErrorCode.JWT_SESSION_NOT_FOUND);
    }
}
