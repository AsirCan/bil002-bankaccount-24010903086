package tr.edu.sakarya.bil002.dto;

import java.time.LocalDateTime;

/** Hata yanıtı biçimi (Gereksinimler §7). */
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
}
