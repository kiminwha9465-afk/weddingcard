package org.example.weddingcard;

import org.example.weddingcard.entity.User;
import org.example.weddingcard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeddingcardApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeddingcardApplication.class, args);
    }

    @Bean
    public CommandLineRunner promoteAdmin(UserRepository userRepository, @Value("${app.admin-username}") String adminUsername) {
        return args -> userRepository.findByUsername(adminUsername).ifPresent(user -> {
            if (user.getRole() != User.Role.ADMIN) {
                user.setRole(User.Role.ADMIN);
                userRepository.save(user);
            }
        });
    }
}
