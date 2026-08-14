package com.warrantywise.service.impl;

import com.warrantywise.dto.auth.AuthResponse;
import com.warrantywise.dto.auth.LoginRequest;
import com.warrantywise.dto.auth.RefreshTokenRequest;
import com.warrantywise.dto.auth.RegisterRequest;
import com.warrantywise.dto.auth.UserSummaryResponse;
import com.warrantywise.entity.ActivityLog;
import com.warrantywise.entity.User;
import com.warrantywise.entity.UserSettings;
import com.warrantywise.enums.ActionType;
import com.warrantywise.enums.Role;
import com.warrantywise.exception.DuplicateResourceException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.exception.UnauthorizedException;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.repository.UserSettingsRepository;
import com.warrantywise.security.JwtTokenProvider;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest loginRequest, String ipAddress) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getEmail()));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String accessToken = tokenProvider.generateAccessTokenFromUser(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .action(ActionType.LOGIN)
                .entityType("USER")
                .entityId(user.getId())
                .description("User logged in")
                .ipAddress(ipAddress)
                .build();
        activityLogRepository.save(activityLog);

        UserSummaryResponse userSummary = mapToUserSummary(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiryMs())
                .user(userSummary)
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest, String ipAddress) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phone(registerRequest.getPhone())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        UserSettings defaultSettings = UserSettings.builder()
                .user(savedUser)
                .build();
        userSettingsRepository.save(defaultSettings);

        ActivityLog activityLog = ActivityLog.builder()
                .user(savedUser)
                .action(ActionType.CREATE)
                .entityType("USER")
                .entityId(savedUser.getId())
                .description("User registered")
                .ipAddress(ipAddress)
                .build();
        activityLogRepository.save(activityLog);

        String accessToken = tokenProvider.generateAccessTokenFromUser(savedUser);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser);

        UserSummaryResponse userSummary = mapToUserSummary(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiryMs())
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        if (!tokenProvider.validateToken(refreshTokenRequest.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Long userId = tokenProvider.getUserIdFromJWT(refreshTokenRequest.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String newAccessToken = tokenProvider.generateAccessTokenFromUser(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        UserSummaryResponse userSummary = mapToUserSummary(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiryMs())
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        return mapToUserSummary(user);
    }

    private UserSummaryResponse mapToUserSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }
}
