package com.codeit.weatherwear.domain.user.service;

import com.codeit.weatherwear.domain.location.dto.LocationDto;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.service.LocationService;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import com.codeit.weatherwear.domain.user.dto.request.ChangePasswordRequest;
import com.codeit.weatherwear.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserCreateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserLockUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.weatherwear.domain.user.dto.request.UserSearchRequest;
import com.codeit.weatherwear.domain.user.dto.response.ProfileDto;
import com.codeit.weatherwear.domain.user.dto.response.UserDto;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserAlreadyExistsException;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.mapper.UserMapper;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.event.DomainEventPublisher;
import com.codeit.weatherwear.global.event.dto.RoleChangedEvent;
import com.codeit.weatherwear.global.response.PageResponse;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;


  private final PasswordEncoder passwordEncoder;
  private final LocationService locationService;
  private final JwtSessionService jwtSessionService;
  private final ThumbnailImageStorage thumbnailImageStorage;
  private final DomainEventPublisher domainEventPublisher;


  @Transactional
  @CacheEvict(cacheNames = "users", key = "'default:firstPage'")
  @Override
  public UserDto create(UserCreateRequest userCreateRequest) {
    // 중복 검사
    if (userRepository.existsByName(userCreateRequest.name()) ||
        userRepository.existsByEmail(userCreateRequest.email())) {
      throw new UserAlreadyExistsException();
    }

    User user = userRepository.save(
        User.builder()
            .name(userCreateRequest.name())
            .email(userCreateRequest.email())
            .password(passwordEncoder.encode(userCreateRequest.password()))
            .locked(false)
            .build()
    );

    return userMapper.toUserDto(user);
  }

  @Transactional(readOnly = true)
  @Cacheable(
      cacheNames = "users",
      key = "#userId"
  )
  @Override
  public ProfileDto findProfile(UUID userId) {
    User user = userRepository.findByIdWithLocation(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    if (user.getProfileImageUrl() != null) {
      return userMapper.toProfileDto(user, thumbnailImageStorage.get(user.getProfileImageUrl()));
    } else {
      return userMapper.toProfileDto(user);
    }
  }

  @Transactional
  @Cacheable(
      cacheNames = "users",
      key = "#userId"
  )
  @Override
  public ProfileDto updateProfile(UUID userId, ProfileUpdateRequest profileUpdateRequest,
      MultipartFile profileImage) {
    User user = userRepository.findByIdWithLocation(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // Location 생성
    Location location = null;
    LocationDto locationDto = profileUpdateRequest.location();
    if (locationDto != null) {
      location = locationService.findOrCreateLocation(locationDto);
    }

    // ProfileImageUrl 업로드
    String profileImageUrl = null;
    if (profileImage != null && !profileImage.isEmpty()) {
      log.debug("[Start Uploading Profile Image On S3] - userId: {}", userId);
      profileImageUrl = thumbnailImageStorage.upload(profileImage);
      log.debug("[Uploading Profile Image On S3 Completed] - userId: {}, url: {}", userId,
          profileImageUrl);
    }

    user.updateProfile(
        profileUpdateRequest.name(),
        profileUpdateRequest.gender(),
        profileUpdateRequest.birthDate(),
        location,
        profileUpdateRequest.temperatureSensitivity(),
        profileImageUrl);

    if (user.getProfileImageUrl() != null) {
      return userMapper.toProfileDto(user, thumbnailImageStorage.get(user.getProfileImageUrl()));
    } else {
      return userMapper.toProfileDto(user);
    }
  }

  @CacheEvict(cacheNames = "users", key = "'default:firstPage'")
  @Transactional
  @Override
  public UUID updateLock(UUID userId, UserLockUpdateRequest userLockUpdateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    user.updateLocked(userLockUpdateRequest.locked());

    // 잠긴 계정은 자동으로 로그아웃
    if (userLockUpdateRequest.locked()) {
      jwtSessionService.invalidateToken(userId);
    }

    return user.getId();
  }

  @Transactional
  @Override
  public void updatePassword(UUID userId, ChangePasswordRequest changePasswordRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    String newPassword = passwordEncoder.encode(changePasswordRequest.password());

    user.updatePassword(newPassword);
  }

  @CacheEvict(cacheNames = "users", key = "'default:firstPage'")
  @Transactional
  @Override
  public UserDto updateRole(UUID userId, UserRoleUpdateRequest userRoleUpdateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    Role previousRole = user.getRole();

    // 권한 변경 시 해당 사용자는 자동으로 로그아웃
    jwtSessionService.invalidateToken(userId);

    user.updateRole(userRoleUpdateRequest.role());

    // 권한 변경 알림 전송
    domainEventPublisher.publish(
        new RoleChangedEvent(userId, userRoleUpdateRequest.role(), previousRole));

    return userMapper.toUserDto(user);
  }

  @Cacheable(
      cacheNames = "users",
      key = "'default:firstPage'",
      condition = "#userSearchRequest.cursor == null && " +
          "#userSearchRequest.idAfter == null && " +
          "#userSearchRequest.limit == 20 && " +
          "#userSearchRequest.sortBy?.equals('email') && "
          +
          "#userSearchRequest.sortDirection == T(com.codeit.weatherwear.global.request.SortDirection).ASCENDING && "
          +
          "#userSearchRequest.emailLike == null && " +
          "#userSearchRequest.roleEqual == null && " +
          "#userSearchRequest.locked == null"
  )
  @Transactional(readOnly = true)
  @Override
  public PageResponse<UserDto> searchUsers(UserSearchRequest userSearchRequest) {
    Slice<User> slice = userRepository.searchUsers(
        userSearchRequest.cursor(),
        userSearchRequest.idAfter(),
        userSearchRequest.limit(),
        userSearchRequest.sortBy(),
        userSearchRequest.sortDirection(),
        userSearchRequest.emailLike(),
        userSearchRequest.roleEqual(),
        userSearchRequest.locked()
    );

    List<User> users = slice.getContent();
    List<UserDto> userDtos = users.stream()
        .map(userMapper::toUserDto)
        .toList();

    boolean hasNext = slice.hasNext();
    Object nextCursor = calculateNextCursor(users, hasNext, userSearchRequest.sortBy());
    UUID nextIdAfter = calculateNextId(users, hasNext);

    return new PageResponse<>(
        userDtos,
        nextCursor,
        nextIdAfter,
        hasNext,
        userRepository.getTotalCount(
            userSearchRequest.emailLike(),
            userSearchRequest.roleEqual(),
            userSearchRequest.locked()
        ),
        userSearchRequest.sortBy(),
        userSearchRequest.sortDirection().name()
    );
  }


  private Object calculateNextCursor(List<User> users, boolean hasNext, String sortBy) {
    if (users.isEmpty() || !hasNext) {
      return null;
    }
    return switch (sortBy) {
      case "email" -> users.get(users.size() - 1).getEmail();
      case "createdAt" -> users.get(users.size() - 1).getCreatedAt();
      default -> throw new IllegalArgumentException("Unsupported sortBy: " + sortBy);
    };
  }

  private UUID calculateNextId(List<User> users, boolean hasNext) {
    if (users.isEmpty() || !hasNext) {
      return null;
    }
    return users.get(users.size() - 1).getId();
  }

}
