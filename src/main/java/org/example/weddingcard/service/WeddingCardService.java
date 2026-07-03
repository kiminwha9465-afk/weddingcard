package org.example.weddingcard.service;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.example.weddingcard.dto.AdminCardResponse;
import org.example.weddingcard.dto.AttendanceRequest;
import org.example.weddingcard.dto.AttendanceResponse;
import org.example.weddingcard.dto.CardIdResponse;
import org.example.weddingcard.dto.CardResponse;
import org.example.weddingcard.dto.CardSummaryResponse;
import org.example.weddingcard.dto.CommentResponse;
import org.example.weddingcard.dto.ManageResponse;
import org.example.weddingcard.entity.Attendance;
import org.example.weddingcard.entity.GuestbookComment;
import org.example.weddingcard.entity.WeddingCard;
import org.example.weddingcard.exception.CardNotFoundException;
import org.example.weddingcard.exception.ForbiddenException;
import org.example.weddingcard.repository.AttendanceRepository;
import org.example.weddingcard.repository.GuestbookCommentRepository;
import org.example.weddingcard.repository.WeddingCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class WeddingCardService {

    private final WeddingCardRepository cardRepository;
    private final GuestbookCommentRepository commentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ObjectMapper objectMapper;

    public WeddingCardService(WeddingCardRepository cardRepository,
                               GuestbookCommentRepository commentRepository,
                               AttendanceRepository attendanceRepository,
                               ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.commentRepository = commentRepository;
        this.attendanceRepository = attendanceRepository;
        this.objectMapper = objectMapper;
    }

    public CardIdResponse create(Map<String, Object> data, String ownerEmail) {
        String id = generateId();
        WeddingCard card = new WeddingCard(id, writeJson(data));
        card.setOwnerEmail(ownerEmail);
        cardRepository.save(card);
        return new CardIdResponse(card.getId(), card.getAdminKey());
    }

    public CardIdResponse update(String id, Map<String, Object> data, String requesterEmail) {
        WeddingCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        verifyOwner(card, requesterEmail);
        card.setDataJson(writeJson(data));
        cardRepository.save(card);
        return new CardIdResponse(card.getId(), card.getAdminKey());
    }

    public String getAdminKey(String id) {
        WeddingCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return ensureAdminKey(card);
    }

    @Transactional(readOnly = true)
    public String getOwnerEmail(String id) {
        WeddingCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return card.getOwnerEmail();
    }

    @Transactional(readOnly = true)
    public List<CardSummaryResponse> getMyCards(String ownerEmail) {
        return cardRepository.findByOwnerEmailOrderByUpdatedAtDesc(ownerEmail).stream()
                .map(card -> {
                    Map<String, Object> data = readJson(card.getDataJson());
                    String groom = String.valueOf(data.getOrDefault("in-groom", "")).trim();
                    String bride = String.valueOf(data.getOrDefault("in-bride", "")).trim();
                    String names;
                    if (!groom.isBlank() || !bride.isBlank()) {
                        names = (!groom.isBlank() ? groom : "?") + " ♥ " + (!bride.isBlank() ? bride : "?");
                    } else {
                        names = String.valueOf(data.getOrDefault("in-cover-names", "(이름 미입력)"));
                    }
                    String dateText = String.valueOf(data.getOrDefault("in-cover-date-text", ""));
                    return new CardSummaryResponse(card.getId(), names, dateText, card.getAdminKey(), card.getUpdatedAt());
                })
                .toList();
    }

    public ManageResponse getManageData(String id, String key) {
        WeddingCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        if (key == null || !ensureAdminKey(card).equals(key)) {
            throw new ForbiddenException();
        }
        List<CommentResponse> comments = commentRepository.findByCardIdOrderByCreatedAtDesc(id).stream()
                .map(CommentResponse::from)
                .toList();
        List<AttendanceResponse> attendances = attendanceRepository.findByCardIdOrderByCreatedAtDesc(id).stream()
                .map(AttendanceResponse::from)
                .toList();
        return new ManageResponse(card.getId(), readJson(card.getDataJson()), comments, attendances);
    }

    @Transactional(readOnly = true)
    public CardResponse get(String id) {
        WeddingCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        List<CommentResponse> comments = commentRepository.findByCardIdOrderByCreatedAtDesc(id).stream()
                .map(CommentResponse::from)
                .toList();
        return new CardResponse(card.getId(), readJson(card.getDataJson()), comments);
    }

    @Transactional(readOnly = true)
    public List<AdminCardResponse> getAllCardsForAdmin() {
        return cardRepository.findAll().stream()
                .sorted(Comparator.comparing(WeddingCard::getUpdatedAt).reversed())
                .map(card -> {
                    Map<String, Object> data = readJson(card.getDataJson());
                    String names = String.valueOf(data.getOrDefault("in-cover-names", "(이름 미입력)"));
                    String dateText = String.valueOf(data.getOrDefault("in-cover-date-text", ""));
                    return new AdminCardResponse(card.getId(), card.getOwnerEmail(), names, dateText,
                            card.getCreatedAt(), card.getUpdatedAt());
                })
                .toList();
    }

    public void deleteCard(String id, String requesterEmail) {
        WeddingCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        verifyOwner(card, requesterEmail);
        commentRepository.deleteByCardId(id);
        attendanceRepository.deleteByCardId(id);
        cardRepository.deleteById(id);
    }

    public void deleteCardAsAdmin(String id) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        commentRepository.deleteByCardId(id);
        attendanceRepository.deleteByCardId(id);
        cardRepository.deleteById(id);
    }

    public CommentResponse addComment(String id, String name, String content) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        GuestbookComment saved = commentRepository.save(new GuestbookComment(id, name, content));
        return CommentResponse.from(saved);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public CommentResponse toggleCommentHidden(Long commentId) {
        GuestbookComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        comment.setHidden(!comment.isHidden());
        return CommentResponse.from(commentRepository.save(comment));
    }

    public AttendanceResponse addAttendance(String id, AttendanceRequest request) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        Attendance saved = attendanceRepository.save(new Attendance(id, request.name(), request.side(),
                request.attending(), request.guestCount(), request.meal()));
        return AttendanceResponse.from(saved);
    }

    private void verifyOwner(WeddingCard card, String requesterEmail) {
        if (card.getOwnerEmail() == null) {
            card.setOwnerEmail(requesterEmail);
        } else if (!card.getOwnerEmail().equals(requesterEmail)) {
            throw new ForbiddenException();
        }
    }

    private String ensureAdminKey(WeddingCard card) {
        if (card.getAdminKey() == null) {
            card.setAdminKey(UUID.randomUUID().toString().replace("-", ""));
            cardRepository.save(card);
        }
        return card.getAdminKey();
    }

    private String generateId() {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        } while (cardRepository.existsById(id));
        return id;
    }

    private String writeJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("저장할 데이터를 직렬화할 수 없습니다.", e);
        }
    }

    private Map<String, Object> readJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new IllegalStateException("저장된 데이터를 읽을 수 없습니다.", e);
        }
    }
}
