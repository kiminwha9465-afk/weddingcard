package org.example.weddingcard.controller;

import tools.jackson.databind.ObjectMapper;
import org.example.weddingcard.dto.CardResponse;
import org.example.weddingcard.exception.CardNotFoundException;
import org.example.weddingcard.service.WeddingCardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditorController {

    private final WeddingCardService cardService;
    private final ObjectMapper objectMapper;

    @Value("${kakao.js-key:}")
    private String kakaoJsKey;

    public EditorController(WeddingCardService cardService, ObjectMapper objectMapper) {
        this.cardService = cardService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/editor/new")
    public String index(Model model) {
        model.addAttribute("cardId", "");
        model.addAttribute("cardDataJson", "null");
        model.addAttribute("cardAdminKey", "");
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        return "editor";
    }

    @GetMapping("/editor/{id}")
    public String edit(@PathVariable String id, Model model, Authentication authentication) {
        try {
            CardResponse card = cardService.get(id);
            String ownerEmail = cardService.getOwnerEmail(id);
            if (ownerEmail != null && !ownerEmail.equals(authentication.getName())) {
                return "redirect:/dashboard";
            }
            model.addAttribute("cardId", card.id());
            model.addAttribute("cardDataJson", toJson(card.data()));
            model.addAttribute("cardAdminKey", cardService.getAdminKey(id));
            model.addAttribute("kakaoJsKey", kakaoJsKey);
            return "editor";
        } catch (CardNotFoundException e) {
            return "redirect:/";
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "null";
        }
    }
}
