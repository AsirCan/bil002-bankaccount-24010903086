package tr.edu.sakarya.bil002.exception;

/**
 * Yetersiz bakiye (R-3.4, R-4.5).
 * API katmanında HTTP 422 Unprocessable Entity'e çevrilir (Gereksinimler §7).
 */
public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
