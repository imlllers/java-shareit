package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ErrorHandlerTest {
    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void shouldHandleNotFound() {
        ErrorResponse response = errorHandler.handleNotFound(new NotFoundException("not found"));

        assertThat(response.getError()).isEqualTo("Не найдено");
        assertThat(response.getDescription()).isEqualTo("not found");
    }

    @Test
    void shouldHandleConflict() {
        ErrorResponse response = errorHandler.handleConflict(new ConflictException("conflict"));

        assertThat(response.getError()).isEqualTo("Конфликт");
        assertThat(response.getDescription()).isEqualTo("conflict");
    }

    @Test
    void shouldHandleValidation() {
        ErrorResponse response = errorHandler.handleValidation(new IllegalArgumentException("bad input"));

        assertThat(response.getError()).isEqualTo("Ошибка валидации");
        assertThat(response.getDescription()).isEqualTo("bad input");
    }

    @Test
    void shouldHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        ErrorResponse response = errorHandler.handleValidation(exception);

        assertThat(response.getError()).isEqualTo("Ошибка валидации");
    }

    @Test
    void shouldHandleForbidden() {
        ErrorResponse response = errorHandler.handleForbidden(new ForbiddenException("forbidden"));

        assertThat(response.getError()).isEqualTo("Нет прав доступа");
        assertThat(response.getDescription()).isEqualTo("forbidden");
    }
}
