package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.UserResponse;
import com.hikmetsuicmez.eventverse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserResponse> getCurrentUser() {
        UserResponse userResponse = userService.loggedInUser();
        return ApiResponse.success(userResponse, "Kullanıcı bilgileri başarıyla getirildi");
    }
}
