package tr.edu.sakarya.bil002.blackbox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi.Yanit;
import tr.edu.sakarya.bil002.destek.ApiTestTabani;

/**
 * Denklik Sınıfı Bölümleme (Equivalence Partitioning, EP) · Rapor 2.1.
 * Her geçerli ve her geçersiz sınıftan bir temsilci. Geçersiz sınıflar AYRI test edilir
 * (birleştirilse bir hata diğerini maskeleyebilir). Testler yalnızca Gereksinimlerden tasarlandı.
 */
@DisplayName("Kara kutu · EP (denklik sınıfları)")
class DenklikSinifiTesti extends ApiTestTabani {

    // ---------- Hesap açma (R-1.x) ----------

    @Test
    @DisplayName("EP-01: Geçerli STANDARD hesap açılır: 201, ACTIVE, 10 haneli numara, işlem kaydı yok")
    void ep01_gecerliStandartHesap_acilmali() {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":\"Ali Veli\",\"accountType\":\"STANDARD\",\"initialBalance\":500}");

        String hesapNo = yanit.govde().path("accountNumber").asText();
        assertAll(
                () -> assertEquals(201, yanit.durum(), yanit.ham()),                          // API Ref.
                () -> assertEquals("ACTIVE", yanit.govde().path("status").asText()),          // R-1.6
                () -> assertTrue(hesapNo.matches("[1-9][0-9]{9}"), hesapNo),                   // R-1.5
                () -> assertEquals(500.0, yanit.govde().path("balance").asDouble(), 0.001),
                () -> assertEquals(0, gecmis(hesapNo).size(), "açılışta kayıt olmamalı"));     // R-1.7
    }

    @ParameterizedTest(name = "EP-02: ownerName = {0} → 400")
    @ValueSource(strings = {"null", "\"\"", "\"   \""})
    @DisplayName("EP-02: ownerName null / boş / sadece boşluk ise hesap açılmamalı (400)")
    void ep02_gecersizAd_reddedilmeli(String adJson) {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":" + adJson + ",\"accountType\":\"STANDARD\",\"initialBalance\":100}");

        assertEquals(400, yanit.durum(), yanit.ham());                                        // R-1.1
    }

    @ParameterizedTest(name = "EP-03: accountType = {0} → 400")
    @ValueSource(strings = {"\"standard\"", "\"GOLD\"", "\"\"", "null"})
    @DisplayName("EP-03: accountType STANDARD/PREMIUM dışında ise hesap açılmamalı (400)")
    void ep03_gecersizTur_reddedilmeli(String turJson) {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":\"Ali\",\"accountType\":" + turJson + ",\"initialBalance\":100}");

        assertEquals(400, yanit.durum(), yanit.ham());                                        // R-1.2
    }

    @Test
    @DisplayName("EP-04: Geçerli PREMIUM hesap açılır (201)")
    void ep04_gecerliPremiumHesap_acilmali() {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":\"Ayşe\",\"accountType\":\"PREMIUM\",\"initialBalance\":100}");

        assertAll(
                () -> assertEquals(201, yanit.durum(), yanit.ham()),
                () -> assertEquals("PREMIUM", yanit.govde().path("accountType").asText()));   // R-1.2
    }

    @Test
    @DisplayName("EP-05: initialBalance gönderilmezse bakiye 0 olmalı")
    void ep05_baslangicBakiyesiYok_sifirOlmali() {
        String hesapNo = hesapAc("STANDARD", null);

        assertEquals(0.0, bakiye(hesapNo), 0.001);                                            // R-1.3
    }

    // R-1.4: initialBalance negatif ise reddedilir. Rapor: H-02
    @Test
    @Tag("hata")
    @DisplayName("EP-06: Negatif initialBalance ile hesap açılmamalı (400)")
    void ep06_negatifBaslangicBakiyesi_reddedilmeli() {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":\"Ali\",\"accountType\":\"STANDARD\",\"initialBalance\":-100}");

        assertEquals(400, yanit.durum(), yanit.ham());                                        // R-1.4
    }

    // ---------- Para yatırma (R-2.x) ----------

    @Test
    @DisplayName("EP-07: Geçerli tutar yatırılır: bakiye artar, DEPOSIT/SUCCESS kaydı oluşur")
    void ep07_gecerliYatirma_kabulEdilmeli() {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = paraYatir(hesapNo, "500");

        JsonNode kayit = gecmis(hesapNo).get(0);
        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(1500.0, bakiye(hesapNo), 0.001),                           // R-2.5
                () -> assertEquals("DEPOSIT", kayit.path("type").asText()),                   // R-2.6
                () -> assertEquals("SUCCESS", kayit.path("status").asText()),
                () -> assertEquals(1500.0, kayit.path("balanceAfter").asDouble(), 0.001));    // §6
    }

    // R-2.7: Reddedilen yatırmada DEPOSIT/FAILED kaydı oluşturulur. Rapor: H-03
    @Test
    @Tag("hata")
    @DisplayName("EP-08: Negatif tutar yatırma reddedilmeli (400), bakiye aynı, DEPOSIT/FAILED kaydı oluşmalı")
    void ep08_negatifYatirma_reddedilmeli() {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = paraYatir(hesapNo, "-100");

        assertAll(
                () -> assertEquals(400, yanit.durum(), yanit.ham()),                          // R-2.2
                () -> assertEquals(1000.0, bakiye(hesapNo), 0.001),                           // G-4
                () -> assertEquals(1, kayitSayisi(hesapNo, "DEPOSIT", "FAILED")));            // R-2.7, §6
    }

    @Test
    @DisplayName("EP-09: Olmayan hesaba yatırma 404 döndürmeli")
    void ep09_olmayanHesabaYatirma_404() {
        Yanit yanit = paraYatir("1000000001", "100");

        assertEquals(404, yanit.durum(), yanit.ham());                                        // R-2.1
    }

    // ---------- Para çekme (R-3.x) ----------

    @Test
    @DisplayName("EP-10: Geçerli tutar çekilir: bakiye azalır, WITHDRAWAL/SUCCESS kaydı oluşur")
    void ep10_gecerliCekme_kabulEdilmeli() {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = paraCek(hesapNo, "300");

        JsonNode kayit = gecmis(hesapNo).get(0);
        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(700.0, bakiye(hesapNo), 0.001),                            // R-3.6
                () -> assertEquals("WITHDRAWAL", kayit.path("type").asText()),                // R-3.7
                () -> assertEquals("SUCCESS", kayit.path("status").asText()),
                () -> assertEquals(700.0, kayit.path("balanceAfter").asDouble(), 0.001));
    }

    // R-3.8: Reddedilen çekimde WITHDRAWAL/FAILED kaydı oluşturulur. Rapor: H-03
    @Test
    @Tag("hata")
    @DisplayName("EP-11: Negatif tutar çekme reddedilmeli (400), bakiye aynı, WITHDRAWAL/FAILED kaydı oluşmalı")
    void ep11_negatifCekme_reddedilmeli() {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = paraCek(hesapNo, "-100");

        assertAll(
                () -> assertEquals(400, yanit.durum(), yanit.ham()),                          // R-3.2
                () -> assertEquals(1000.0, bakiye(hesapNo), 0.001),                           // G-4
                () -> assertEquals(1, kayitSayisi(hesapNo, "WITHDRAWAL", "FAILED")));         // R-3.8
    }

    @Test
    @DisplayName("EP-12: Olmayan hesaptan çekme 404 döndürmeli")
    void ep12_olmayanHesaptanCekme_404() {
        Yanit yanit = paraCek("1000000002", "100");

        assertEquals(404, yanit.durum(), yanit.ham());                                        // R-3.1
    }

    // ---------- Sorgulama (R-10.x) ----------

    @Test
    @DisplayName("EP-13: Var olan hesap sorgulanır (200)")
    void ep13_varOlanHesapSorgu_200() {
        String hesapNo = hesapAc("STANDARD", "10");

        Yanit yanit = hesap(hesapNo);

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),                          // R-10.1
                () -> assertEquals(hesapNo, yanit.govde().path("accountNumber").asText()));
    }

    // R-10.2: Hesap mevcut değilse 404 döner. Rapor: H-01
    @Test
    @Tag("hata")
    @DisplayName("EP-14: Olmayan hesap sorgusu 404 döndürmeli")
    void ep14_olmayanHesapSorgu_404() {
        Yanit yanit = hesap("1000000003");

        assertEquals(404, yanit.durum(), yanit.ham());                                        // R-10.2
    }

    // ---------- Faiz (R-8.x): belgedeki örnek tablo ----------

    // R-8.3: Aylık faiz kuruşa yuvarlanır (2 ondalık, yarım yukarı). Rapor: H-04
    @ParameterizedTest(name = "EP-15: {0} {1} TL → yeni bakiye {2}")
    @Tag("hata")
    @CsvSource({
        "STANDARD, 1000.00, 1002.50",
        "PREMIUM, 1000.00, 1004.17",
        "STANDARD, 12000.00, 12030.00",
        "PREMIUM, 2500.50, 2510.92",
        "STANDARD, 0.00, 0.00"
    })
    @DisplayName("EP-15: Aylık faiz belgedeki örnek tabloya uymalı (oran, formül, 2 ondalık yarım yukarı)")
    void ep15_faizOrnekTablosu(String tur, String bakiye, double beklenenBakiye) {
        String hesapNo = hesapAc(tur, bakiye);

        Yanit yanit = hesapIslemi(hesapNo, "interest");

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));                  // R-8.2, R-8.3, R-8.4
    }

    // R-8.3, R-8.5: INTEREST kaydının tutarı kuruşlu faiz olmalı. Rapor: H-04
    @Test
    @Tag("hata")
    @DisplayName("EP-16: Faiz uygulanınca INTEREST/SUCCESS kaydı oluşur, amount = faiz tutarı")
    void ep16_faizKaydi_olusmali() {
        String hesapNo = hesapAc("PREMIUM", "1000");

        hesapIslemi(hesapNo, "interest");

        JsonNode kayit = gecmis(hesapNo).get(0);
        assertAll(
                () -> assertEquals("INTEREST", kayit.path("type").asText()),                  // R-8.5
                () -> assertEquals("SUCCESS", kayit.path("status").asText()),
                () -> assertEquals(4.17, kayit.path("amount").asDouble(), 0.001));
    }

    // ---------- İşlem geçmişi (R-9.x) ----------

    @Test
    @DisplayName("EP-17: Olmayan hesabın işlem geçmişi 404 döndürmeli")
    void ep17_olmayanHesapGecmis_404() {
        Yanit yanit = api.get("/api/accounts/1000000004/transactions");

        assertEquals(404, yanit.durum(), yanit.ham());                                        // R-9.1
    }

    @Test
    @DisplayName("EP-18: İşlemi olmayan hesabın geçmişi boş liste (200)")
    void ep18_islemsizHesapGecmis_bosListe() {
        String hesapNo = hesapAc("STANDARD", "100");

        Yanit yanit = api.get("/api/accounts/" + hesapNo + "/transactions");

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertTrue(yanit.govde().isArray()),
                () -> assertEquals(0, yanit.govde().size()));                                 // R-9.4
    }

    @Test
    @DisplayName("EP-19: İşlem geçmişi en yeni işlem önce olacak şekilde sıralı")
    void ep19_gecmisSiralama_azalan() throws InterruptedException {
        String hesapNo = hesapAc("STANDARD", "1000");
        paraYatir(hesapNo, "100");
        Thread.sleep(50);                       // aynı zaman damgası belirsizliği (Rapor 1.1 #9) olmasın
        paraCek(hesapNo, "50");

        JsonNode liste = gecmis(hesapNo);

        assertAll(
                () -> assertEquals(2, liste.size()),
                () -> assertEquals("WITHDRAWAL", liste.get(0).path("type").asText()),         // R-9.3
                () -> assertEquals("DEPOSIT", liste.get(1).path("type").asText()));
    }

    // ---------- Biçim hataları (§7) ----------

    @Test
    @DisplayName("EP-20: amount alanı eksik ise 400, kayıt oluşmamalı (§6 dipnot)")
    void ep20_eksikAmount_400() {
        String hesapNo = hesapAc("STANDARD", "100");

        Yanit yanit = api.post("/api/accounts/" + hesapNo + "/deposit", "{}");

        assertAll(
                () -> assertEquals(400, yanit.durum(), yanit.ham()),                          // §7 eksik alan
                () -> assertEquals(100.0, bakiye(hesapNo), 0.001),
                () -> assertEquals(0, gecmis(hesapNo).size()));                               // §6 dipnot
    }

    @Test
    @DisplayName("EP-21: Bozuk JSON gövde 400 döndürmeli")
    void ep21_bozukJson_400() {
        String hesapNo = hesapAc("STANDARD", "100");

        Yanit yanit = api.post("/api/accounts/" + hesapNo + "/deposit", "{\"amount\": ");

        assertAll(
                () -> assertEquals(400, yanit.durum(), yanit.ham()),                          // §7 bozuk JSON
                () -> assertEquals(100.0, bakiye(hesapNo), 0.001));
    }

    @Test
    @DisplayName("EP-22: Art arda açılan 20 hesabın numaraları 10 haneli, ilk hanesi 0 değil ve benzersiz")
    void ep22_hesapNumarasi_bicimVeBenzersizlik() {
        Set<String> numaralar = new HashSet<>();

        for (int i = 0; i < 20; i++) {
            numaralar.add(hesapAc("STANDARD", "1"));
        }

        assertAll(
                () -> assertEquals(20, numaralar.size(), "tekrar eden numara var"),           // R-1.5
                () -> assertTrue(numaralar.stream().allMatch(n -> n.matches("[1-9][0-9]{9}")), numaralar.toString()));
    }
}
