package io.code.tdd.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long userId) {
        super("유저를 찾을 수 없습니다. " + userId);
    }
}
