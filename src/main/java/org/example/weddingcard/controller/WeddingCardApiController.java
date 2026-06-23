package org.example.weddingcard.controller;

import org.example.weddingcard.dto.AttendanceRequest;
import org.example.weddingcard.dto.AttendanceResponse;
import org.example.weddingcard.dto.CardIdResponse;
import org.example.weddingcard.dto.CardResponse;
import org.example.weddingcard.dto.CommentRequest;
import org.example.weddingcard.dto.CommentResponse;
import org.example.weddingcard.dto.ManageResponse;
import org.example.weddingcard.service.WeddingCardService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class WeddingCardApiController {

    private final WeddingCardService cardService;

    public WeddingCardApiController(WeddingCardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@RequestBody Map<String, Object> data, Authentication authentication) {
        CardIdResponse result = cardService.create(data, authentication.getName());
        return Map.of("id", result.id(), "adminKey", result.adminKey());
    }

    @PutMapping("/{id}")
    public Map<String, String> update(@PathVariable String id, @RequestBody Map<String, Object> data, Authentication authentication) {
        CardIdResponse result = cardService.update(id, data, authentication.getName());
        return Map.of("id", result.id(), "adminKey", result.adminKey());
    }

    @GetMapping("/{id}")
    public CardResponse get(@PathVariable String id) {
        return cardService.get(id);
    }

    @GetMapping("/{id}/manage")
    public ManageResponse manage(@PathVariable String id, @RequestParam String key) {
        return cardService.getManageData(id, key);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable String id, @RequestBody CommentRequest request) {
        if (request.name() == null || request.name().isBlank() || request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("이름과 메시지를 입력해주세요.");
        }
        return cardService.addComment(id, request.name(), request.content());
    }

    @PostMapping("/{id}/attendances")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse addAttendance(@PathVariable String id, @RequestBody AttendanceRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (!"groom".equals(request.side()) && !"bride".equals(request.side())) {
            throw new IllegalArgumentException("구분 값이 올바르지 않습니다.");
        }
        return cardService.addAttendance(id, request);
    }
}
