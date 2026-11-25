package tr.edu.sakarya.bil002.exception;

/**
 * Bir iş kuralı ihlal edildiğinde fırlatılan bütün hataların ortak atası.
 * Servis, reddedilen yatırma/çekme denemesinde FAILED kaydı oluşturmak için
 * sadece bu türü yakalar (§6). Kodda gerçek bir bug (ör. NullPointerException)
 * olursa yanlışlıkla "iş kuralı reddi" gibi kaydedilmesin diye ayrı bir tür.
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }
}
