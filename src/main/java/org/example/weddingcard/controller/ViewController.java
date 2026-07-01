package org.example.weddingcard.controller;

import org.example.weddingcard.dto.CardResponse;
import org.example.weddingcard.exception.CardNotFoundException;
import org.example.weddingcard.service.WeddingCardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@Controller
public class ViewController {

    private final WeddingCardService cardService;

    @Value("${kakao.js-key:}")
    private String kakaoJsKey;

    public ViewController(WeddingCardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/templates")
    public String templates() {
        return "card-templates";
    }

    @GetMapping("/c/{id}")
    public String view(@PathVariable String id, Model model) {
        CardResponse card;
        try {
            card = cardService.get(id);
        } catch (CardNotFoundException e) {
            return "card-not-found";
        }

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        Map<String, Object> data = card.data();
        String names = stringOrDefault(data.get("in-cover-names"), "저희 두 사람");
        String dateText = stringOrDefault(data.get("in-cover-date-text"), "");
        String venue = stringOrDefault(data.get("in-venue"), "");
        String description = (dateText.isBlank() && venue.isBlank())
                ? "소중한 분들을 초대합니다."
                : String.join(" · ", java.util.stream.Stream.of(dateText, venue).filter(s -> !s.isBlank()).toList());

        model.addAttribute("cardId", id);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("ogTitle", names + " 결혼합니다");
        model.addAttribute("ogDescription", description);
        model.addAttribute("ogImage", baseUrl + "/og-default.png");
        model.addAttribute("ogUrl", baseUrl + "/c/" + id);
        return "view";
    }

    private String stringOrDefault(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? defaultValue : s;
    }
}
