package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, max = 20, message = "Şifre 6-20 karakter arasında olmalıdır")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zçğıöşü])(?=.*[A-ZÇĞİÖŞÜ])(?=.*[@#$%^&+=.*])(?=\\S+$).{8,}$",
        message = "Şifre en az bir rakam, bir küçük harf, bir büyük harf ve bir özel karakter içermelidir"
    )
    private String password;

    @NotBlank(message = "İsim boş olamaz")
    @Size(min = 2, max = 50, message = "İsim 2-50 karakter arasında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyisim boş olamaz")
    @Size(min = 2, max = 50, message = "Soyisim 2-50 karakter arasında olmalıdır")
    private String lastName;

    @Pattern(regexp = "^(\\+90|0)?[0-9]{10}$", message = "Geçerli bir telefon numarası giriniz")
    private String phoneNumber;

    @Size(max = 200, message = "Adres en fazla 200 karakter olabilir")
    private String address;

    @Pattern(regexp = "^(https?://)?[\\w-]+(\\.[\\w-]+)+[/#?]?.*$", 
             message = "Geçerli bir profil resmi URL'si giriniz")
    private String profilePicture;
} 