package org.example.weddingcard.controller;

import org.example.weddingcard.entity.User;
import org.example.weddingcard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-email}")
    private String adminEmail;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String passwordConfirm,
                            Model model) {
        email = email == null ? "" : email.trim();
        if (email.isBlank() || password == null || password.length() < 8) {
            model.addAttribute("error", "이메일과 8자 이상의 비밀번호를 입력해주세요.");
            model.addAttribute("email", email);
            return "register";
        }
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("email", email);
            return "register";
        }
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "이미 가입된 이메일입니다.");
            model.addAttribute("email", email);
            return "register";
        }

        User user = new User(email, passwordEncoder.encode(password));
        if (email.equalsIgnoreCase(adminEmail)) {
            user.setRole(User.Role.ADMIN);
        }
        userRepository.save(user);
        return "redirect:/login?registered";
    }
}
