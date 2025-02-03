package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.request.PaymentRequest;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.Payment;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import com.hikmetsuicmez.eventverse.enums.PaymentStatus;
import com.hikmetsuicmez.eventverse.event.PaymentRequiredEvent;
import com.hikmetsuicmez.eventverse.exception.PaymentException;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.repository.ParticipantRepository;
import com.hikmetsuicmez.eventverse.repository.PaymentRepository;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final Options options;
    private final ZoneId TURKEY_ZONE = ZoneId.of("Europe/Istanbul");

    @Scheduled(fixedRate = 300000) // 5 dakikada bir çalışır
    public void checkExpiredPayments() {
        LocalDateTime now = LocalDateTime.now(TURKEY_ZONE);
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);
        
        for (Payment payment : pendingPayments) {
            if (payment.getExpirationDate() != null && now.isAfter(payment.getExpirationDate())) {
                // Ödeme süresini geçmiş ödemeleri iptal et
                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
                
                // Katılımcı durumunu güncelle
                processPayment(payment.getEvent().getId(), payment.getUser().getId(), PaymentStatus.EXPIRED);
                
                // Bildirim gönder
                Participant participant = participantRepository.findByEventIdAndUserId(
                    payment.getEvent().getId(), 
                    payment.getUser().getId()
                ).orElse(null);
                
                if (participant != null) {
                    notificationService.createPaymentStatusNotification(
                        participant, 
                        "Ödeme süresi dolduğu için katılımınız iptal edildi"
                    );
                }
            }
        }
    }

    @Transactional
    public String processIyzicoPayment(UUID eventId, PaymentRequest request) {
        User currentUser = userService.getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Participant participant = participantRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        // Katılımcının daha önce başarısız ödemesi varsa kontrol et
        Optional<Payment> lastPayment = paymentRepository.findFirstByEventIdAndUserIdOrderByPaymentDateDesc(eventId, currentUser.getId());
        if (lastPayment.isPresent() && lastPayment.get().getStatus() == PaymentStatus.FAILED) {
            throw new PaymentException("Bu etkinlik için daha önce başarısız bir ödeme işlemi gerçekleşti. Tekrar katılım sağlayamazsınız.");
        }

        if (participant.getStatus() != ParticipantStatus.PAYMENT_PENDING) {
            throw new PaymentException("Invalid payment status");
        }

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setLocale(Locale.TR.getValue());
        paymentRequest.setConversationId(eventId.toString());
        paymentRequest.setPrice(BigDecimal.valueOf(event.getPrice()));
        paymentRequest.setPaidPrice(BigDecimal.valueOf(event.getPrice()));
        paymentRequest.setCurrency(Currency.TRY.name());
        paymentRequest.setInstallment(Integer.parseInt(request.getInstallment()));
        paymentRequest.setBasketId(eventId.toString());
        paymentRequest.setPaymentChannel(PaymentChannel.WEB.name());
        paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(request.getCardHolderName());
        paymentCard.setCardNumber(request.getCardNumber());
        paymentCard.setExpireMonth(request.getExpireMonth());
        paymentCard.setExpireYear(request.getExpireYear());
        paymentCard.setCvc(request.getCvc());
        paymentCard.setRegisterCard(0);
        paymentRequest.setPaymentCard(paymentCard);

        Buyer buyer = new Buyer();
        buyer.setId(currentUser.getId().toString());
        buyer.setName(currentUser.getFirstName());
        buyer.setSurname(currentUser.getLastName());
        buyer.setEmail(currentUser.getEmail());
        buyer.setIdentityNumber("74300864791");
        buyer.setRegistrationAddress(request.getAddress());
        buyer.setIp("85.34.78.112");
        buyer.setCity(event.getCity());
        buyer.setCountry("Turkey");
        paymentRequest.setBuyer(buyer);

        // Fatura adresi
        Address billingAddress = new Address();
        billingAddress.setContactName(buyer.getName() + " " + buyer.getSurname());
        billingAddress.setCity(buyer.getCity());
        billingAddress.setCountry(buyer.getCountry());
        billingAddress.setAddress(request.getAddress());
        paymentRequest.setBillingAddress(billingAddress);

        // Teslimat adresi (sanal ürün olduğu için fatura adresi ile aynı)
        Address shippingAddress = new Address();
        shippingAddress.setContactName(buyer.getName() + " " + buyer.getSurname());
        shippingAddress.setCity(buyer.getCity());
        shippingAddress.setCountry(buyer.getCountry());
        shippingAddress.setAddress(request.getAddress());
        paymentRequest.setShippingAddress(shippingAddress);

        BasketItem item = new BasketItem();
        item.setId(event.getId().toString());
        item.setName(event.getTitle());
        item.setCategory1(event.getCategory());
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(BigDecimal.valueOf(event.getPrice()));

        ArrayList<BasketItem> basketItems = new ArrayList<>();
        basketItems.add(item);
        paymentRequest.setBasketItems(basketItems);

        try {
            com.iyzipay.model.Payment iyzipayResponse = com.iyzipay.model.Payment.create(paymentRequest, options);

            // Ödeme kaydını oluştur
            Payment payment = Payment.builder()
                    .event(event)
                    .user(currentUser)
                    .amount(event.getPrice())
                    .installmentCount(Integer.parseInt(request.getInstallment()))
                    .paymentDate(LocalDateTime.now(TURKEY_ZONE))
                    .expirationDate(LocalDateTime.now(TURKEY_ZONE).plusHours(24)) // 24 saat süre
                    .status(PaymentStatus.PENDING)
                    .build();

            if (iyzipayResponse.getStatus().equals("success")) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(iyzipayResponse.getPaymentId());
                processPayment(eventId, currentUser.getId(), PaymentStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(iyzipayResponse.getErrorMessage());
                processPayment(eventId, currentUser.getId(), PaymentStatus.FAILED);
                throw new PaymentException("Ödeme başarısız: " + iyzipayResponse.getErrorMessage());
            }

            paymentRepository.save(payment);
            return payment.getStatus() == PaymentStatus.COMPLETED ? 
                   "Ödeme başarıyla tamamlandı" : 
                   "Ödeme başarısız oldu";

        } catch (Exception e) {
            processPayment(eventId, currentUser.getId(), PaymentStatus.FAILED);
            throw new PaymentException("Ödeme işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    @EventListener
    public void handlePaymentRequiredEvent(PaymentRequiredEvent event) {
        Participant participant = participantRepository.findByEventIdAndUserId(event.getEventId(), event.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        // Ödeme bildirimi gönder
        notificationService.createPaymentReminderNotification(participant);
    }

    @Transactional
    public void processPayment(UUID eventId, UUID userId, PaymentStatus paymentStatus) {
        if (userId == null) {
            userId = userService.getCurrentUserId();
        }

        Participant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        Event event = participant.getEvent();
        event.setPaymentStatus(paymentStatus);
        eventRepository.save(event);

        ParticipantStatus newStatus;
        String paymentMessage;

        switch (paymentStatus) {
            case COMPLETED:
                newStatus = ParticipantStatus.APPROVED;
                paymentMessage = "Ödeme başarıyla tamamlandı";
                break;
            case FAILED:
                newStatus = ParticipantStatus.PAYMENT_FAILED;
                paymentMessage = "Ödeme başarısız oldu";
                break;
            case EXPIRED:
                newStatus = ParticipantStatus.CANCELLED;
                paymentMessage = "Ödeme süresi doldu";
                break;
            default:
                return;
        }

        participant.setStatus(newStatus);
        participantRepository.save(participant);

        // Ödeme durumu bildirimi gönder
        notificationService.createPaymentStatusNotification(participant, paymentMessage);
    }

    public PaymentStatus getPaymentStatus(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        return event.getPaymentStatus();
    }

    public Payment getLastPayment(UUID eventId, UUID userId) {
        return paymentRepository.findFirstByEventIdAndUserIdOrderByPaymentDateDesc(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
} 