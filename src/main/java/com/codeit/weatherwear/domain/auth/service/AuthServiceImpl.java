package com.codeit.weatherwear.domain.auth.service;

import com.codeit.weatherwear.domain.security.repository.JwtSessionRepository;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtSessionService jwtSessionService;
    private final JwtSessionRepository jwtSessionRepository;
    private final UserRepository userRepository;

    // 비밀번호 초기화
    @Override
    public void resetPassword(String email) {

    }
}
