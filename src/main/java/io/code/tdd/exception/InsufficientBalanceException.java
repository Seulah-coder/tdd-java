package io.code.tdd.exception;

public class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(long amount) {
        super("현재 유저가 가지고 있는 포인트 보다 사용하려는 포인트가 많습니다. 잔액:"
              + amount + "포인트");
    }
    
}
