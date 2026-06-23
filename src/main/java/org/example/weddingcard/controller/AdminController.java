package org.example.weddingcard.controller;

import org.example.weddingcard.repository.UserRepository;
import org.example.weddingcard.service.WeddingCardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final WeddingCardService cardService;

    public AdminController(UserRepository userRepository, WeddingCardService cardService) {
        this.userRepository = userRepository;
        this.cardService = cardService;
    }

    @GetMapping
    public String admin(Model model) {
        model.addAttribute("users", userRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("cards", cardService.getAllCardsForAdmin());
        return "admin";
    }

    @PostMapping("/cards/{id}/delete")
    public String deleteCard(@PathVariable String id) {
        cardService.deleteCardAsAdmin(id);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin";
    }
}
