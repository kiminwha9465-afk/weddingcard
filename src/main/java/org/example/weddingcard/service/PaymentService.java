package org.example.weddingcard.service;

import org.example.weddingcard.entity.Payment;
import org.example.weddingcard.entity.WeddingCard;
import org.example.weddingcard.exception.CardNotFoundException;
import org.example.weddingcard.exception.ForbiddenException;
import org.example.weddingcard.repository.PaymentRepository;
import org.example.weddingcard.repository.WeddingCardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentService {

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Value("${toss.price}")
    private int price;

    private final PaymentRepository paymentRepository;
    private final WeddingCardRepository cardRepository;
    private final WeddingCardService cardService;
    private final RestClient restClient = RestClient.create();

    public PaymentService(PaymentRepository paymentRepository,
                          WeddingCardRepository cardRepository,
                          WeddingCardService cardService) {
        this.paymentRepository = paymentRepository;
        this.cardRepository = cardRepository;
        this.cardService = cardService;
    }

    public void confirm(String paymentKey, String orderId, int amount, String requesterEmail) {
        if (amount != price) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        // orderId 형식: {cardId}_{uuid}
        String cardId = orderId.contains("_") ? orderId.substring(0, orderId.indexOf('_')) : null;
        if (cardId == null) {
            throw new IllegalArgumentException("잘못된 주문 ID 형식입니다.");
        }

        WeddingCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!requesterEmail.equals(card.getOwnerEmail())) {
            throw new ForbiddenException();
        }

        if (card.isPaid()) {
            return; // 이미 결제 완료
        }

        // 토스 결제 승인 API 호출
        String encodedKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/confirm")
                    .header("Authorization", "Basic " + encodedKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("결제 승인 중 오류가 발생했습니다: " + e.getMessage());
        }

        if (response == null || !"DONE".equals(response.get("status"))) {
            throw new IllegalStateException("결제가 완료되지 않았습니다.");
        }

        LocalDateTime paidAt = parsePaidAt(response.get("approvedAt"));

        paymentRepository.save(new Payment(cardId, requesterEmail, orderId, paymentKey, amount, paidAt));
        cardService.markAsPaid(cardId, paidAt);
    }

    @Transactional(readOnly = true)
    public List<Payment> getMyPayments(String ownerEmail) {
        return paymentRepository.findByOwnerEmailOrderByCreatedAtDesc(ownerEmail);
    }

    private LocalDateTime parsePaidAt(Object value) {
        if (value == null) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(value.toString()).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
