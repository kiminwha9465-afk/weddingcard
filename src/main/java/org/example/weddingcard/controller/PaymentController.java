package org.example.weddingcard.controller;

import org.example.weddingcard.entity.WeddingCard;
import org.example.weddingcard.exception.CardNotFoundException;
import org.example.weddingcard.repository.WeddingCardRepository;
import org.example.weddingcard.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {

    @Value("${toss.client-key}")
    private String tossClientKey;

    @Value("${toss.price}")
    private int price;

    private final WeddingCardRepository cardRepository;
    private final PaymentService paymentService;

    public PaymentController(WeddingCardRepository cardRepository, PaymentService paymentService) {
        this.cardRepository = cardRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/pay/{cardId}")
    public String payPage(@PathVariable String cardId, Model model, Authentication authentication) {
        WeddingCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!authentication.getName().equals(card.getOwnerEmail())) {
            return "redirect:/dashboard";
        }
        if (card.isPaid()) {
            return "redirect:/dashboard";
        }

        model.addAttribute("cardId", cardId);
        model.addAttribute("tossClientKey", tossClientKey);
        model.addAttribute("amount", price);
        return "pay";
    }

    @GetMapping("/pay/success")
    public String paySuccess(@RequestParam String paymentKey,
                             @RequestParam String orderId,
                             @RequestParam int amount,
                             Authentication authentication,
                             RedirectAttributes attrs) {
        try {
            paymentService.confirm(paymentKey, orderId, amount, authentication.getName());
            attrs.addFlashAttribute("paySuccess", true);
        } catch (Exception e) {
            attrs.addFlashAttribute("payError", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/pay/fail")
    public String payFail(@RequestParam(required = false) String message,
                          RedirectAttributes attrs) {
        attrs.addFlashAttribute("payError", message != null ? message : "결제가 취소되었습니다.");
        return "redirect:/dashboard";
    }
}
