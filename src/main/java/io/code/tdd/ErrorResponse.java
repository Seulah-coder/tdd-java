package io.code.tdd;

public record ErrorResponse(
        String code,
        String message
) {
}
