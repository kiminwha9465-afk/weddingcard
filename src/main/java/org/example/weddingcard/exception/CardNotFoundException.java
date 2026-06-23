package org.example.weddingcard.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String id) {
        super("청첩장을 찾을 수 없습니다: " + id);
    }
}
