package com.interviewportal.user.serviceimpl;

import com.interviewportal.user.dto.UpdateProfileRequest;
import com.interviewportal.user.dto.UserResponse;
import com.interviewportal.user.entity.User;
import com.interviewportal.user.exception.NotFoundException;
import com.interviewportal.user.mapper.UserMapper;
import com.interviewportal.user.repository.RefreshTokenRepository;
import com.interviewportal.user.repository.UserRepository;
import com.interviewportal.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Profile management and admin ban/unban.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findOrThrow(userId);
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse setBanned(Long targetUserId, boolean banned) {
        User user = findOrThrow(targetUserId);
        user.setBanned(banned);
        userRepository.save(user);
        if (banned) {
            // Revoke sessions so the ban takes effect immediately for long-lived refresh tokens.
            refreshTokenRepository.revokeAllForUser(targetUserId);
            log.info("Admin banned user id={}", targetUserId);
        } else {
            log.info("Admin unbanned user id={}", targetUserId);
        }
        return userMapper.toResponse(user);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
