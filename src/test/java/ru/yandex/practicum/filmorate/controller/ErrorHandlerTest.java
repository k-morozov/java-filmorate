package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handleValidationException_returnsErrorMessage() {
        Map<String, String> result = handler.handleValidationException(new ValidationException("bad input"));
        assertEquals("bad input", result.get("error"));
    }

    @Test
    void handleNotFoundException_returnsErrorMessage() {
        Map<String, String> result = handler.handleNotFoundException(new NotFoundException("not found"));
        assertEquals("not found", result.get("error"));
    }

    @Test
    void handleException_returnsErrorMessageWith500() {
        Map<String, String> result = handler.handleException(new RuntimeException("unexpected"));
        assertEquals("unexpected", result.get("error"));
    }
}
