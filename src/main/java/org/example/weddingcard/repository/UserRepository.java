package org.example.weddingcard.repository;

import org.example.weddingcard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByKakaoId(String kakaoId);

    boolean existsByUsername(String username);

    List<User> findAllByOrderByCreatedAtDesc();
}
