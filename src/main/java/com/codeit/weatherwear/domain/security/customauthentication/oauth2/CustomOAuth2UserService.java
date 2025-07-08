package com.codeit.weatherwear.domain.security.customauthentication.oauth2;

import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.domain.user.entity.OAuthProvider;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();  // google / kakao
    String email = oAuth2User.getAttribute("email");

    User user = userRepository.findByEmail(email)
        .orElse(null);
    if (user == null) {
      // 새로운 회원으로 추가
      log.info("oauth2: DB에 회원 정보가 없어 새로운 회원으로 추가합니다.");
      user = userRepository.save(User.builder()
          .name(oAuth2User.getAttribute("name"))
          .email(email)
          .password("")
          .linkedOAuthProviders(List.of(OAuthProvider.google))
          .role(Role.USER)
          .build());
    }
    return new CustomUserDetails(user.getId()
        , user.getEmail(), user.getPassword(), user.getRole(), user.isLocked(),
        user.getTempPasswordExpirationTime());
  }
}
