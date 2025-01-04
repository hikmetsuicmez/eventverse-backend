package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.request.UpdateProfileRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.UserResponse;
import com.hikmetsuicmez.eventverse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        UserResponse updatedUser = userService.updateProfile(request);
        return ApiResponse.success(updatedUser, "Profil bilgileri başarıyla güncellendi");
    }

    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        if (!isImageFile(file)) {
            return ApiResponse.error("Sadece JPEG, JPG ve PNG formatları desteklenmektedir.");
        }
        return userService.updateProfilePicture(file);
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                contentType.equals(MediaType.IMAGE_PNG_VALUE) ||
                contentType.equals("image/jpg")
        );
    }
}
