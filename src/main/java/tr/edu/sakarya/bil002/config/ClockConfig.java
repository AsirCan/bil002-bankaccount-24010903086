package tr.edu.sakarya.bil002.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Uygulamanın saati. Servisler "şimdi"yi doğrudan LocalDateTime.now() ile değil
 * bu Clock üzerinden alır. Neden? G-6 (gün = sunucunun yerel saati) ve günlük
 * limitin her gün sıfırlanması gibi zamana bağlı kurallar, testte sabit ya da
 * ileri alınmış bir saat verilerek bir gün beklemeden test edilebilsin (beyaz kutu).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
