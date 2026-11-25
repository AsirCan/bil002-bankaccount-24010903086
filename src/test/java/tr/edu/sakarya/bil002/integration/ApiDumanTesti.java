package tr.edu.sakarya.bil002.integration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi.Yanit;
import tr.edu.sakarya.bil002.destek.ApiTestTabani;

/**
 * Duman testi (smoke test): API'nin temel akışı ayakta mı, yanıt biçimleri beklendiği gibi mi?
 * Ayrıntılı kural testi değildir. Hedef değiştirilerek canlı servise de koşulur:
 * {@code mvnw test -Dtest=ApiDumanTesti -Dhedef=canli} (token: BIL002_TOKEN ortam değişkeni).
 */
@DisplayName("Duman testi: temel API akışı")
class ApiDumanTesti extends ApiTestTabani {

    @Test
    @DisplayName("DUMAN-01: Hesap açılır (201), numara 10 hane, durum ACTIVE")
    void duman01_hesapAcilir() {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":\"Ayşe Yılmaz\",\"accountType\":\"STANDARD\",\"initialBalance\":1000}");

        JsonNode hesap = yanit.govde();
        assertAll(
                () -> assertEquals(201, yanit.durum(), yanit.ham()),
                () -> assertTrue(hesap.get("accountNumber").asText().matches("[1-9][0-9]{9}")),
                () -> assertEquals("ACTIVE", hesap.get("status").asText()),
                () -> assertEquals("STANDARD", hesap.get("accountType").asText()),
                () -> assertEquals(1000.0, hesap.get("balance").asDouble(), 0.001));
    }

    @Test
    @DisplayName("DUMAN-02: Açılan hesap numarasıyla sorgulanır (200)")
    void duman02_hesapSorgulanir() {
        String hesapNo = hesapAc("PREMIUM", "250.50");

        Yanit yanit = hesap(hesapNo);

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(hesapNo, yanit.govde().get("accountNumber").asText()),
                () -> assertEquals(250.50, yanit.govde().get("balance").asDouble(), 0.001));
    }

    @Test
    @DisplayName("DUMAN-03: Para yatırılır, bakiye artar, geçmişte DEPOSIT/SUCCESS kaydı görünür")
    void duman03_paraYatirilir() {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = paraYatir(hesapNo, "500");

        JsonNode sonIslem = gecmis(hesapNo).get(0);
        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(1500.0, yanit.govde().get("balance").asDouble(), 0.001),
                () -> assertEquals("DEPOSIT", sonIslem.get("type").asText()),
                () -> assertEquals("SUCCESS", sonIslem.get("status").asText()));
    }

    // R-10.2: Hesap mevcut değilse 404 Not Found döner. Rapor: H-01
    @Test
    @Tag("hata")
    @DisplayName("DUMAN-04: Olmayan hesap 404 döner, hata gövdesi JSON (§7)")
    void duman04_olmayanHesap404() {
        Yanit yanit = hesap("1000000000");

        assertAll(
                () -> assertEquals(404, yanit.durum(), yanit.ham()),
                () -> assertEquals(404, yanit.govde().path("status").asInt()),
                () -> assertTrue(yanit.govde().hasNonNull("message"), "hata mesajı olmalı"));
    }
}
