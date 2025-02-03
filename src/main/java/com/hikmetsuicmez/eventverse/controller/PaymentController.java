package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.request.PaymentRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.enums.PaymentStatus;
import com.hikmetsuicmez.eventverse.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{eventId}")
    public ApiResponse<String> makePayment(
            @PathVariable UUID eventId,
            @Valid @RequestBody PaymentRequest request) {
        
        String result = paymentService.processIyzicoPayment(eventId, request);
        return ApiResponse.success(result, "Ödeme işlemi tamamlandı");
    }

    @GetMapping("/{eventId}/status")
    public ApiResponse<PaymentStatus> getPaymentStatus(@PathVariable UUID eventId) {
        PaymentStatus status = paymentService.getPaymentStatus(eventId);
        return ApiResponse.success(status, "Ödeme durumu başarıyla getirildi");
    }
} 