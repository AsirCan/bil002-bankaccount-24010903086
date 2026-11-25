package tr.edu.sakarya.bil002.exception;

/**
 * Hesap bulunamadı (R-2.1, R-3.1, R-4.2, R-9.1, R-10.2).
 * API katmanında HTTP 404 Not Found'e çevrilir (Gereksinimler §7).
 */
public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
