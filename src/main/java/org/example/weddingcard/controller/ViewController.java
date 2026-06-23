package org.example.weddingcard.controller;

import org.example.weddingcard.exception.CardNotFoundException;
import org.example.weddingcard.service.WeddingCardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    private final WeddingCardService cardService;

    @Value("${kakao.js-key:}")
    private String kakaoJsKey;

    public ViewController(WeddingCardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/c/{id}")
    public String view(@PathVariable String id, Model model) {
        try {
            cardService.get(id);
        } catch (CardNotFoundException e) {
            return "card-not-found";
        }
        model.addAttribute("cardId", id);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        return "view";
    }
}
