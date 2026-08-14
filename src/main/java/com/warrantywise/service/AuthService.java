package com.warrantywise.service;

import com.warrantywise.dto.auth.AuthResponse;
import com.warrantywise.dto.auth.LoginRequest;
import com.warrantywise.dto.auth.RefreshTokenRequest;
import com.warrantywise.dto.auth.RegisterRequest;
import com.warrantywise.dto.auth.UserSummaryResponse;
import com.warrantywise.security.UserPrincipal;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest, String ipAddress);

    AuthResponse register(RegisterRequest registerRequest, String ipAddress);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    UserSummaryResponse getCurrentUser(UserPrincipal currentUser);
}
