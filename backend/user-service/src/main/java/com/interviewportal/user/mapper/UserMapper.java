package com.interviewportal.user.mapper;

import com.interviewportal.user.dto.UserResponse;
import com.interviewportal.user.entity.Role;
import com.interviewportal.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Translates between the {@link User} entity and its API-facing DTO.
 *
 * <p>Why a hand-written mapper: there is exactly one non-trivial mapping and no inbound entity
 * construction from DTOs beyond the service layer, so a full library like MapStruct would be
 * over-engineering. A tiny, explicit method is easier to read and debug.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getProvider().name(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet()),
                user.isBanned(),
                user.getCreatedAt());
    }
}
