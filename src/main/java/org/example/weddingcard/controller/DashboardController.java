package org.example.weddingcard.controller;

import org.example.weddingcard.service.WeddingCardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final WeddingCardService cardService;

    public DashboardController(WeddingCardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("userEmail", authentication.getName());
        model.addAttribute("cards", cardService.getMyCards(authentication.getName()));
        return "dashboard";
    }
}
