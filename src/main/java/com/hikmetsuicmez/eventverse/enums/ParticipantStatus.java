package com.hikmetsuicmez.eventverse.enums;

public enum ParticipantStatus {
    PENDING,         // Organizatör onayı bekliyor
    APPROVED,        // Onaylandı
    REJECTED,        // Reddedildi
    PAYMENT_PENDING, // Ödeme bekliyor
    PAYMENT_FAILED,  // Ödeme başarısız
    CANCELLED       // İptal edildi
} 