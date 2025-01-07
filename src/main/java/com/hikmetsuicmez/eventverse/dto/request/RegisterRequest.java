package com.hikmetsuicmez.eventverse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Ad alanı boş bırakılamaz")
    private String firstName;
    
    @NotBlank(message = "Soyad alanı boş bırakılamaz")
    private String lastName;
    
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @NotBlank(message = "E-posta alanı boş bırakılamaz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, max = 20, message = "Şifre 6-20 karakter arasında olmalıdır")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zçğıöşü])(?=.*[A-ZÇĞİÖŞÜ])(?=.*[@#$%^&+=.*])(?=\\S+$).{8,}$", message = "Şifre en az bir rakam, bir küçük harf, bir büyük harf ve bir özel karakter içermelidir")
    private String password;
    
    @Pattern(regexp = "^\\+90[0-9]{10}$", message = "Geçerli bir telefon numarası giriniz (+90 ile başlamalı)")
    private String phoneNumber;
    
    @NotBlank(message = "Adres alanı boş bırakılamaz")
    private String address;

    @NotNull(message = "Doğum tarihi seçilmelidir")
    private String birthDate;

}