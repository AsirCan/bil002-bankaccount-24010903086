package tr.edu.sakarya.bil002.whitebox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Testte elle ileri alınabilen saat (bir tür "fake" test double'ı, Hafta 7).
 * Zamana bağlı kurallar (G-6: gün değişince günlük limit sıfırlanır) bir gün beklemeden test edilir.
 * Servisler saati Clock üzerinden aldığı için (bağımlılık enjeksiyonu) gerçek saat yerine bu verilir.
 */
public class AyarlanabilirSaat extends Clock {

    private final ZoneId bolge = ZoneId.of("Europe/Istanbul");
    private Instant an;

    public AyarlanabilirSaat(LocalDateTime baslangic) {
        this.an = baslangic.atZone(bolge).toInstant();
    }

    public void ilerlet(Duration sure) {
        an = an.plus(sure);
    }

    public void ayarla(LocalDateTime zaman) {
        an = zaman.atZone(bolge).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return bolge;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return an;
    }
}
