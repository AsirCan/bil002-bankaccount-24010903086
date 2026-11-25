package tr.edu.sakarya.bil002;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Uygulamanın giriş noktası. {@code mvn spring-boot:run} bu sınıfı çalıştırır.
 * {@code @SpringBootApplication}: bu paket ve alt paketlerindeki bileşenleri
 * (controller, service, repository) bulur ve otomatik ayarları açar.
 */
@SpringBootApplication
public class BankAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankAccountApplication.class, args);
    }
}
