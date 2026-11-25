package tr.edu.sakarya.bil002.dto;

import java.math.BigDecimal;

/** Başarılı transfer yanıtı (API Referansı). */
public record TransferResponse(String message, String fromAccount, String toAccount, BigDecimal amount) {
}
