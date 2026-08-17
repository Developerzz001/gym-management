package com.gymmanagement.auth;

import com.gymmanagement.auth.dto.ChangePasswordRequest;
import com.gymmanagement.auth.dto.LoginRequest;
import com.gymmanagement.auth.dto.LoginResponse;
import com.gymmanagement.auth.dto.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void changePassword(String userEmail, ChangePasswordRequest request);
}
