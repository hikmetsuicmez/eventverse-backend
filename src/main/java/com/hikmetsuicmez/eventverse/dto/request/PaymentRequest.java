package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotBlank(message = "Kart numarası boş olamaz")
    private String cardNumber;

    @NotBlank(message = "Kart sahibi adı boş olamaz")
    private String cardHolderName;

    @NotBlank(message = "Son kullanma ayı boş olamaz")
    private String expireMonth;

    @NotBlank(message = "Son kullanma yılı boş olamaz")
    private String expireYear;

    @NotBlank(message = "CVV boş olamaz")
    private String cvc;

    @NotNull(message = "Tutar boş olamaz")
    private Double price;

    @NotBlank(message = "Adres bilgisi zorunludur")
    private String address;

    private String installment = "1";
} 