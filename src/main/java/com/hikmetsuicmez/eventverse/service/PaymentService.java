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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Transactional
    public String processIyzicoPayment(UUID eventId, PaymentRequest request) {
        User currentUser = userService.getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Participant participant = participantRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

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
        buyer.setRegistrationAddress("Nidakule Göztepe, Merdivenköy Mah. Bora Sok. No:1");
        buyer.setIp("85.34.78.112");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        paymentRequest.setBuyer(buyer);

        // Fatura adresi
        Address billingAddress = new Address();
        billingAddress.setContactName(buyer.getName() + " " + buyer.getSurname());
        billingAddress.setCity(buyer.getCity());
        billingAddress.setCountry(buyer.getCountry());
        billingAddress.setAddress(buyer.getRegistrationAddress());
        paymentRequest.setBillingAddress(billingAddress);

        // Teslimat adresi (sanal ürün olduğu için fatura adresi ile aynı)
        Address shippingAddress = new Address();
        shippingAddress.setContactName(buyer.getName() + " " + buyer.getSurname());
        shippingAddress.setCity(buyer.getCity());
        shippingAddress.setCountry(buyer.getCountry());
        shippingAddress.setAddress(buyer.getRegistrationAddress());
        paymentRequest.setShippingAddress(shippingAddress);

        BasketItem item = new BasketItem();
        item.setId(event.getId().toString());
        item.setName(event.getTitle());
        item.setCategory1("Etkinlik");
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
                    .paymentDate(LocalDateTime.now())
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