package org.example.weddingcard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ManageController {

    @GetMapping("/manage/{id}")
    public String manage(@PathVariable String id, Model model) {
        model.addAttribute("cardId", id);
        return "manage";
    }
}
