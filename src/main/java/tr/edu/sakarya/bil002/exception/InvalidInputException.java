package tr.edu.sakarya.bil002.exception;

/**
 * Geçersiz girdi: boş ad, geçersiz tür, negatif bakiye, amount &lt;= 0, aynı hesaba transfer, eksik alan.
 * API katmanında HTTP 400 Bad Request'e çevrilir (Gereksinimler §7).
 */
public class InvalidInputException extends BusinessException {

    public InvalidInputException(String message) {
        super(message);
    }
}
