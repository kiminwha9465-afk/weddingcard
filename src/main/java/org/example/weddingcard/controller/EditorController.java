package org.example.weddingcard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EditorController {

    @GetMapping("/")
    public String index() {
        return "editor";
    }
}
