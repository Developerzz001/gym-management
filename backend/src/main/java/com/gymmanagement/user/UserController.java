package com.gymmanagement.user;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.user.dto.UserRequest;
import com.gymmanagement.user.dto.UserProfileRequest;
import com.gymmanagement.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.gymmanagement.common.exception.BadRequestException;

import java.util.Set;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin APIs to manage all system users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.success(userService.getMyProfile());
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMyProfile(@Valid @RequestBody UserProfileRequest request) {
        return ApiResponse.success("Profile updated successfully", userService.updateMyProfile(request));
    }

    @GetMapping(value = "/me/profile-image", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp" })
    public ResponseEntity<byte[]> getMyProfileImage(Authentication authentication) {
        User user = userService.getUserEntityByEmail(authentication.getName());
        if (user.getProfileImage() == null || user.getProfileImageContentType() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(user.getProfileImageContentType()))
                .body(user.getProfileImage());
    }

    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadMyProfileImage(Authentication authentication,
                                                   @RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Profile image must be between 1 byte and 5 MB");
        }
        if (file.getContentType() == null || !Set.of("image/jpeg", "image/png", "image/webp").contains(file.getContentType())) {
            throw new BadRequestException("Only JPG, PNG, and WEBP images are supported");
        }
        try {
            User user = userService.getUserEntityByEmail(authentication.getName());
            user.setProfileImage(file.getBytes());
            user.setProfileImageContentType(file.getContentType());
            userService.saveUser(user);
            return ApiResponse.message("Profile image uploaded successfully");
        } catch (java.io.IOException ex) {
            throw new BadRequestException("Profile image could not be read");
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Create a new user")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success("User created successfully", userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Update an existing user")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success("User updated successfully", userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Delete a user")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.message("User deleted successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Get user by id")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER')")
    @Operation(summary = "Search / list users with pagination")
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.getUsers(keyword, role, page, size));
    }
}
