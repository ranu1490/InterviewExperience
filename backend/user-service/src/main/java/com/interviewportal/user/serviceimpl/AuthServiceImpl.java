package com.interviewportal.user.serviceimpl;

import com.interviewportal.common.security.JwtService;
import com.interviewportal.user.dto.AuthResponse;
import com.interviewportal.user.dto.GoogleLoginRequest;
import com.interviewportal.user.dto.LoginRequest;
import com.interviewportal.user.dto.RefreshTokenRequest;
import com.interviewportal.user.dto.SignupRequest;
import com.interviewportal.user.entity.AuthProvider;
import com.interviewportal.user.entity.RefreshToken;
import com.interviewportal.user.entity.Role;
import com.interviewportal.user.entity.User;
import com.interviewportal.user.exception.ConflictException;
import com.interviewportal.user.exception.UnauthorizedException;
import com.interviewportal.user.mapper.UserMapper;
import com.interviewportal.user.repository.RefreshTokenRepository;
import com.interviewportal.user.repository.UserRepository;
import com.interviewportal.user.service.AuthService;
import com.interviewportal.user.service.GoogleTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Core authentication logic: registration, credential/Google login and refresh-token rotation.
 *
 * <p>Uses constructor injection (all dependencies final) — the recommended style because it makes
 * dependencies explicit, supports immutability and allows the class to be unit tested without a
 * Spring context.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserMapper userMapper;
    private final long refreshTokenExpirationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           GoogleTokenVerifier googleTokenVerifier,
                           UserMapper userMapper,
                           com.interviewportal.common.security.JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.userMapper = userMapper;
        this.refreshTokenExpirationMs = jwtProperties.getRefreshTokenExpirationMs();
    }

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .provider(AuthProvider.LOCAL)
                .roles(Set.of(Role.USER))
                .build();
        user = userRepository.save(user);
        log.info("Registered new user id={} username={}", user.getId(), user.getUsername());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleUserInfo info = googleTokenVerifier.verify(request.idToken());

        User user = userRepository.findByEmail(info.email())
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(generateUsername(info.email()))
                        .email(info.email())
                        .fullName(info.name())
                        .avatarUrl(info.picture())
                        .provider(AuthProvider.GOOGLE)
                        .providerId(info.subject())
                        .roles(Set.of(Role.USER))
                        .build()));
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isValid(token)
                || !JwtService.TOKEN_TYPE_REFRESH.equals(jwtService.extractTokenType(token))) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not recognised"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        // Rotation: invalidate the used refresh token and mint a brand-new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse issueTokens(User user) {
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getUsername(), user.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(
                user.getId(), user.getUsername(), user.getEmail(), roles);

        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build());

        return AuthResponse.of(accessToken, refreshToken, userMapper.toResponse(user));
    }

    private String generateUsername(String email) {
        String base = email.substring(0, email.indexOf('@'));
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
