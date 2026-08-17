package com.gymmanagement.user;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.user.dto.UserRequest;
import com.gymmanagement.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);

    UserResponse getUserById(Long id);

    PageResponse<UserResponse> getUsers(String keyword, Role role, int page, int size);

    User getUserEntityById(Long id);

    User getUserEntityByEmail(String email);
}
