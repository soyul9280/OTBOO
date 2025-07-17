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

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();  // google / kakao
    OAuthProvider oauthProvider = OAuthProvider.valueOf(provider);

    String email = null;
    String name = null;

    if (provider.equals("google")) {
      email = oAuth2User.getAttribute("email");
      name = oAuth2User.getAttribute("name");
    } else if (provider.equals("kakao")) {
      Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
      Map<String, Object> profile =
          kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
      name = profile != null ? (String) profile.get("nickname") : "kakao_user";
      // 카카오의 경우 이메일을 닉네임+kakao.com으로
      email = name + "@kakao.com";
    } else {
      throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
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