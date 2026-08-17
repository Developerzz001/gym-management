package com.gymmanagement.auth;

import com.gymmanagement.auth.dto.ChangePasswordRequest;
import com.gymmanagement.auth.dto.LoginRequest;
import com.gymmanagement.auth.dto.LoginResponse;
import com.gymmanagement.auth.dto.RefreshTokenRequest;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.UnauthorizedException;
import com.gymmanagement.security.JwtService;
import com.gymmanagement.security.UserPrincipal;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import com.gymmanagement.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userService.getUserEntityByEmail(request.getEmail());
        UserPrincipal principal = new UserPrincipal(user);

        String accessToken = jwtService.generateAccessToken(principal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildLoginResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = storedToken.getUser();
        UserPrincipal principal = new UserPrincipal(user);

        String accessToken = jwtService.generateAccessToken(principal);
        refreshTokenService.revokeToken(storedToken.getToken());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return buildLoginResponse(user, accessToken, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userService.getUserEntityByEmail(userEmail);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
