package com.interviewportal.user.service;

import com.interviewportal.user.dto.UpdateProfileRequest;
import com.interviewportal.user.dto.UserResponse;

/** Profile and administrative user use-cases. */
public interface UserService {

    UserResponse getById(Long id);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    UserResponse setBanned(Long targetUserId, boolean banned);
}
