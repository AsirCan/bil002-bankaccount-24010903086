package tr.edu.sakarya.bil002.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Cucumber'ı Spring'e bağlar: senaryolar koşmadan önce referans API gerçek bir sunucu olarak
 * rastgele bir portta açılır (kara kutu testlerindeki gibi). Hedef canlı ise adımlar canlıya gider.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringAyari {
}
