package tr.edu.sakarya.bil002.exception;

/**
 * Hesap durumu işleme uygun değil: FROZEN/CLOSED hesapta işlem, geçersiz durum geçişi, bakiyesi olan hesabı kapatma.
 * API katmanında HTTP 409 Conflict'e çevrilir (Gereksinimler §7).
 */
public class AccountStateException extends BusinessException {

    public AccountStateException(String message) {
        super(message);
    }
}
