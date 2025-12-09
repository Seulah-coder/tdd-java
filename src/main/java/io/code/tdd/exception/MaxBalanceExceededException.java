package io.code.tdd.exception;

public class MaxBalanceExceededException extends RuntimeException {
    public MaxBalanceExceededException(long amount) {
        super("현재 잔액은 " + amount + "포인트로, 최대 충전 가능 포인트를 초과하였습니다.");
    }
}
