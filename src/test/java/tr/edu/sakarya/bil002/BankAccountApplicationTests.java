package tr.edu.sakarya.bil002;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Duman testi (smoke test): uygulama ayağa kalkabiliyor mu?
 * {@code @SpringBootTest} bütün Spring bağlamını (context) başlatır.
 */
@SpringBootTest
class BankAccountApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Uygulama bağlamı hatasız başlamalı")
    void uygulamaBaglamiBaslamali() {
        assertNotNull(context);
    }
}
