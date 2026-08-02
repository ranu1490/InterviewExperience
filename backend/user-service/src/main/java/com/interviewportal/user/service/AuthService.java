package com.interviewportal.user.service;

import com.interviewportal.user.dto.AuthResponse;
import com.interviewportal.user.dto.GoogleLoginRequest;
import com.interviewportal.user.dto.LoginRequest;
import com.interviewportal.user.dto.RefreshTokenRequest;
import com.interviewportal.user.dto.SignupRequest;

/**
 * Authentication use-cases. Programming to an interface (with a separate {@code *Impl}) keeps
 * controllers decoupled from implementation details and makes the service easy to mock in tests —
 * a direct application of the Dependency Inversion principle.
 */
public interface AuthService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    AuthResponse googleLogin(GoogleLoginRequest request);

    void logout(String refreshToken);
}
