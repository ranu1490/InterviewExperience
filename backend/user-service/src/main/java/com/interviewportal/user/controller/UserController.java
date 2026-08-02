package com.interviewportal.user.controller;

import com.interviewportal.user.dto.UpdateProfileRequest;
import com.interviewportal.user.dto.UserResponse;
import com.interviewportal.user.security.CurrentUser;
import com.interviewportal.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profile endpoints plus admin ban/unban.
 *
 * <p>Authorisation model:
 * <ul>
 *   <li>{@code /me} endpoints derive the target from the JWT, so a user can only ever act on
 *       themselves.</li>
 *   <li>Ban/unban are guarded by {@code @PreAuthorize("hasRole('ADMIN')")} — declarative,
 *       method-level checks are clearer and less error-prone than manual role inspection.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Profile management and admin moderation of accounts")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.getById(CurrentUser.requireId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user's profile")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(CurrentUser.requireId(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get any user's public profile")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ban a user (admin only)")
    public ResponseEntity<UserResponse> ban(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setBanned(id, true));
    }

    @PostMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lift a ban (admin only)")
    public ResponseEntity<UserResponse> unban(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setBanned(id, false));
    }
}
