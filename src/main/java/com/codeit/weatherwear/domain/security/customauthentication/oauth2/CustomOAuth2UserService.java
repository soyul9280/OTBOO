package com.codeit.weatherwear.domain.security.customauthentication.oauth2;

import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.domain.user.entity.OAuthProvider;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  private static final String KAKAO_EMAIL_DOMAIN = "@kakao.com";

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();
    OAuthProvider oauthProvider = switch (provider.toLowerCase()) {
      case "google" -> OAuthProvider.google;
      case "kakao" -> OAuthProvider.kakao;
      default ->
          throw new OAuth2AuthenticationException("Unsupported OAuth2 Provider: " + provider);
    };

    String email = null;
    String name = null;

    if (oauthProvider.equals(OAuthProvider.google)) {

      email = oAuth2User.getAttribute("email");
      name = oAuth2User.getAttribute("name");

    } else if (oauthProvider.equals(OAuthProvider.kakao)) {

      Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
      Map<String, Object> profile =
          kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
      name = profile != null ? (String) profile.get("nickname") : null;
      // 카카오의 경우 이메일을 닉네임+kakao.com으로
      email = name + KAKAO_EMAIL_DOMAIN;
    }

    // email, name은 필수로 필요함
    if (email == null || email.isBlank()) {
      throw new OAuth2AuthenticationException("Failed to get email");
    }
    if (name == null || name.isBlank()) {
      throw new OAuth2AuthenticationException("Failed to get name");
    }

    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      // 기존 회원이 소셜 연동되어 있지 않다면 연동 정보 추가
      user.addLinkedOAuthProvider(oauthProvider);
      // 로그인 성공
      return toCustomUserDetails(user);
    }

    // findByEmail을 했을 때 없다면 신규 가입
    try {
      log.info("Start Creating New User From OAuth2 Provider: {}", oauthProvider.name());
      User newUser = userRepository.save(User.builder()
          .name(name)
          .email(email)
          .password("")
          .linkedOAuthProviders(List.of(oauthProvider))
          .role(Role.USER)
          .build());
      userRepository.flush();

      return toCustomUserDetails(newUser);

    } catch (DataIntegrityViolationException e) {
      // 예: name 중복 (DB unique 제약 위반) -> 연동 실패
      throw new OAuth2AuthenticationException(
          new OAuth2Error("INVALID_REQUEST", "이미 존재하는 사용자 정보입니다.", null));
    }
  }

  private CustomUserDetails toCustomUserDetails(User user) {
    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getRole(),
        user.isLocked(),
        user.getTempPasswordExpirationTime()
    );
  }
}