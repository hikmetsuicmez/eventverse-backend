package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.request.UpdateProfileRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.UserResponse;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.repository.UserRepository;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String UPLOAD_BASE_DIR = "D:/uploads/eventverse";
    private final String PROFILE_PICTURES_DIR = "/profile-pictures";

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UserResponse loggedInUser() {
        User currentUser = getCurrentUser();
        return UserResponse.builder()
                .id(currentUser.getId())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .email(currentUser.getEmail())
                .phoneNumber(currentUser.getPhoneNumber())
                .address(currentUser.getAddress())
                .profilePicture(currentUser.getProfilePicture())
                .birthDate(currentUser.getBirthDate())
                .build();
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();

        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress());
        }
        if (request.getBirthDate() != null) {
            currentUser.setBirthDate(request.getBirthDate());
        }

        User updatedUser = userRepository.save(currentUser);

        return UserResponse.builder()
                .id(updatedUser.getId())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .email(updatedUser.getEmail())
                .phoneNumber(updatedUser.getPhoneNumber())
                .address(updatedUser.getAddress())
                .profilePicture(updatedUser.getProfilePicture())
                .birthDate(updatedUser.getBirthDate())
                .build();
    }

    public void deleteUser(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Şifre boş olamaz");
        }

        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            throw new IllegalArgumentException("Şifre yanlış");
        }
        try {
            // Kullanıcıya ait diğer verileri temizle (yorumlar, yanıtlar, etkinlikler vs.)
            userRepository.delete(currentUser);
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            throw new RuntimeException("Hesap silinirken bir hata oluştu");
        }
    }

    public ApiResponse<String> updateProfilePicture(MultipartFile file) {
        try {
            User currentUser = getCurrentUser();

            if (!isSupportedFileFormat(file)) {
                throw new IllegalArgumentException("Unsupported file format. Only JPEG and PNG.");
            }

            Path baseDir = Paths.get(UPLOAD_BASE_DIR);
            Path profilePicsDir = baseDir.resolve(PROFILE_PICTURES_DIR.substring(1));

            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            if (!Files.exists(profilePicsDir)) {
                Files.createDirectories(profilePicsDir);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = profilePicsDir.resolve(fileName);

            String oldPicture = currentUser.getProfilePicture();
            if (oldPicture != null && !oldPicture.contains("gravatar.com")) {
                try {
                    String oldFileName = oldPicture.substring(oldPicture.lastIndexOf("/") + 1);
                    Path oldFilePath = profilePicsDir.resolve(oldFileName);
                    Files.deleteIfExists(oldFilePath);
                } catch (Exception e) {
                    System.err.println("Eski profil resmi silinirken hata: " + e.getMessage());
                }
            }

            file.transferTo(filePath);

            String photoUrl = "http://localhost:8080/uploads/eventverse" + PROFILE_PICTURES_DIR + "/" + fileName;
            currentUser.setProfilePicture(photoUrl);
            userRepository.save(currentUser);

            return ApiResponse.success(photoUrl, "Profil fotoğrafı başarıyla güncellendi");
        } catch (Exception e) {
            return ApiResponse.error("Dosya yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    private boolean isSupportedFileFormat(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png"));
    }

}
