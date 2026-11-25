package tr.edu.sakarya.bil002.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tr.edu.sakarya.bil002.dto.ErrorResponse;
import tr.edu.sakarya.bil002.exception.AccountNotFoundException;
import tr.edu.sakarya.bil002.exception.AccountStateException;
import tr.edu.sakarya.bil002.exception.InsufficientBalanceException;
import tr.edu.sakarya.bil002.exception.InvalidInputException;
import tr.edu.sakarya.bil002.exception.LimitExceededException;

/**
 * Servis katmanının fırlattığı istisnaları Gereksinimler §7'deki HTTP kodlarına çevirir
 * ve hatayı §7'deki JSON biçiminde döndürür: timestamp, status, error, message, path.
 * {@code @RestControllerAdvice}: bütün controller'lar için ortak hata yakalayıcı.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> invalidInput(InvalidInputException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request);                     // 400
    }

    /** Bozuk JSON, gövde yok ya da sayı yerine metin ("abc") → 400 (§7). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "İstek gövdesi okunamadı (bozuk ya da eksik JSON)", request); // 400
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> mediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "İçerik tipi application/json olmalıdır", request); // 400
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(AccountNotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request);                       // 404
    }

    @ExceptionHandler(AccountStateException.class)
    public ResponseEntity<ErrorResponse> conflict(AccountStateException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, e.getMessage(), request);                        // 409
    }

    @ExceptionHandler({InsufficientBalanceException.class, LimitExceededException.class})
    public ResponseEntity<ErrorResponse> unprocessable(RuntimeException e, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), request);            // 422
    }

    /** Olmayan adres (yanlış URL) → 404. Aşağıdaki genel 500 yakalayıcısına düşmesin diye ayrı. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResource(NoResourceFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Adres bulunamadı", request);                   // 404
    }

    /** Yanlış HTTP metodu (ör. deposit'e GET) → 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> method(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), request);              // 405
    }

    /** Beklenmeyen her şey → 500. Gereksinimdeki hiçbir durumda buraya düşülmemeli. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception e, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen sunucu hatası", request); // 500
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(clock), status.value(),
                status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
