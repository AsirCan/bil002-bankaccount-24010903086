package tr.edu.sakarya.bil002.exception;

/**
 * Yatırma limiti (R-2.4) ya da günlük çekim limiti (R-3.5) aşıldı.
 * API katmanında HTTP 422 Unprocessable Entity'e çevrilir (Gereksinimler §7).
 */
public class LimitExceededException extends BusinessException {

    public LimitExceededException(String message) {
        super(message);
    }
}
