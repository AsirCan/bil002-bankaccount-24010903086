package tr.edu.sakarya.bil002.blackbox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi.Yanit;
import tr.edu.sakarya.bil002.destek.ApiTestTabani;

/**
 * Durum Geçiş Testi (State Transition Testing) · Rapor 2.4.
 * ST-01..11: Gereksinimler §4.2 tablosunun BÜTÜN satırları (geçerli + geçersiz geçişler).
 * ST-12..21: §4.3 izin matrisi, FROZEN/CLOSED hesapta yasak olaylar (tablodaki ✘ hücreleri).
 * ST-22: sorgulama ve geçmiş her durumda serbest. ST-23: döngülü uzun yol.
 * Her geçişte bakiyenin değişmediği de doğrulanır (R-5.2, R-6.2, G-4).
 */
@DisplayName("Kara kutu · Durum geçiş")
class DurumGecisTesti extends ApiTestTabani {

    /** Hesabı istenen başlangıç durumuna getirir. CLOSED için bakiye 0 olmalı (R-7.2). */
    private String hesapHazirla(String durum, String bakiye) {
        String hesapNo = hesapAc("STANDARD", bakiye);
        if ("FROZEN".equals(durum)) {
            hesapIslemi(hesapNo, "freeze");
        } else if ("CLOSED".equals(durum)) {
            hesapIslemi(hesapNo, "close");
        }
        if (!durum.equals(durum(hesapNo))) {
            throw new IllegalStateException("Hazırlık başarısız: " + durum + " durumuna getirilemedi");
        }
        return hesapNo;
    }

    // Not: Tablonun canlıda hata yakalayan satırları (ST-04, ST-05, ST-08) aşağıda AYRI metotlarda.
    // Sebep: @Tag("hata") metoda konur; geçen satırlar CI'daki zorunlu (yeşil) adımda kalmaya devam etsin.
    @ParameterizedTest(name = "{0}: {1} + {2} (bakiye {3}) → {4} / {5}")
    @CsvSource({
        "ST-01, ACTIVE, freeze,   100,  200, FROZEN",
        "ST-02, ACTIVE, unfreeze, 100,  409, ACTIVE",
        "ST-03, ACTIVE, close,    0,    200, CLOSED",
        "ST-06, FROZEN, unfreeze, 100,  200, ACTIVE",
        "ST-07, FROZEN, close,    0,    200, CLOSED",
        "ST-09, CLOSED, freeze,   0,    409, CLOSED",
        "ST-10, CLOSED, unfreeze, 0,    409, CLOSED",
        "ST-11, CLOSED, close,    0,    409, CLOSED"
    })
    @DisplayName("ST-01..11: §4.2 durum geçiş tablosu (geçen satırlar)")
    void st01_11_durumGecisTablosu(String id, String baslangic, String olay, String bakiye,
                                   int beklenenKod, String beklenenDurum) {
        gecisDogrula(id, baslangic, olay, bakiye, beklenenKod, beklenenDurum);
    }

    // R-7.2: Bakiyesi olan hesap kapatılamaz. Rapor: H-08
    @Test
    @Tag("hata")
    @DisplayName("ST-04: ACTIVE + close (bakiye 0,01) → 409, ACTIVE kalmalı")
    void st04_bakiyeliAktifHesapKapatma_reddedilmeli() {
        gecisDogrula("ST-04", "ACTIVE", "close", "0.01", 409, "ACTIVE");
    }

    // R-5.1: Zaten FROZEN olan hesap için dondurma reddedilir. Rapor: H-09
    @Test
    @Tag("hata")
    @DisplayName("ST-05: FROZEN + freeze → 409 (zaten dondurulmuş)")
    void st05_dondurulmusHesabiTekrarDondurma_reddedilmeli() {
        gecisDogrula("ST-05", "FROZEN", "freeze", "100", 409, "FROZEN");
    }

    // R-7.2: Bakiyesi olan hesap kapatılamaz. Rapor: H-08
    @Test
    @Tag("hata")
    @DisplayName("ST-08: FROZEN + close (bakiye 0,01) → 409, FROZEN kalmalı")
    void st08_bakiyeliDondurulmusHesapKapatma_reddedilmeli() {
        gecisDogrula("ST-08", "FROZEN", "close", "0.01", 409, "FROZEN");
    }

    private void gecisDogrula(String id, String baslangic, String olay, String bakiye,
                              int beklenenKod, String beklenenDurum) {
        String hesapNo = hesapHazirla(baslangic, bakiye);

        Yanit yanit = hesapIslemi(hesapNo, olay);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), id + " " + yanit.ham()),       // R-5.x, R-6.x, R-7.x
                () -> assertEquals(beklenenDurum, durum(hesapNo), id),
                () -> assertEquals(Double.parseDouble(bakiye), bakiye(hesapNo), 0.001, id));  // bakiye değişmez
    }

    // Matrisin canlıda hata yakalayan hücreleri (ST-13, ST-15, ST-20) aşağıda ayrı metotlarda.
    @ParameterizedTest(name = "{0}: {1} hesapta {2} → {3}")
    @CsvSource({
        "ST-12, FROZEN, deposit,          409",
        "ST-14, FROZEN, transfer-gonder,  409",
        "ST-16, FROZEN, interest,         409",
        "ST-17, CLOSED, deposit,          409",
        "ST-18, CLOSED, withdraw,         RED",
        "ST-19, CLOSED, transfer-gonder,  RED",
        "ST-21, CLOSED, interest,         409"
    })
    @DisplayName("ST-12..21: §4.3 izin matrisi, FROZEN/CLOSED hesapta para işlemleri reddedilir (geçen hücreler)")
    void st12_21_izinMatrisi(String id, String durum, String islem, String beklenen) {
        izinDogrula(id, durum, islem, beklenen);
    }

    // R-3.3, §4.3: FROZEN hesaptan çekim reddedilir. Rapor: H-10
    @Test
    @Tag("hata")
    @DisplayName("ST-13: FROZEN hesaptan para çekme → 409, bakiye değişmemeli")
    void st13_dondurulmusHesaptanCekim_reddedilmeli() {
        izinDogrula("ST-13", "FROZEN", "withdraw", "409");
    }

    // R-4.3, §4.3: Hedef FROZEN ise transfer reddedilir. Rapor: H-11
    @Test
    @Tag("hata")
    @DisplayName("ST-15: FROZEN hesaba transfer alma → 409, iki bakiye de değişmemeli")
    void st15_dondurulmusHesabaTransfer_reddedilmeli() {
        izinDogrula("ST-15", "FROZEN", "transfer-al", "409");
    }

    // R-4.3, R-7.4, §4.3: Hedef CLOSED ise transfer reddedilir. Rapor: H-11
    @Test
    @Tag("hata")
    @DisplayName("ST-20: CLOSED hesaba transfer alma → 409, iki bakiye de değişmemeli")
    void st20_kapaliHesabaTransfer_reddedilmeli() {
        izinDogrula("ST-20", "CLOSED", "transfer-al", "409");
    }

    private void izinDogrula(String id, String durum, String islem, String beklenen) {
        String bakiye = "CLOSED".equals(durum) ? "0" : "100";
        String hesapNo = hesapHazirla(durum, bakiye);
        String diger = hesapAc("STANDARD", "100");

        Yanit yanit = olayUygula(islem, hesapNo, diger);

        // RED: CLOSED hesabın bakiyesi 0 olduğundan 409 ve 422 birlikte geçerli, belge öncelik vermiyor
        assertAll(
                () -> {
                    if ("RED".equals(beklenen)) {
                        assertTrue(yanit.durum() >= 400 && yanit.durum() < 500, id + " " + yanit.ham());
                    } else {
                        assertEquals(Integer.parseInt(beklenen), yanit.durum(), id + " " + yanit.ham()); // §4.3, R-2.3 ...
                    }
                },
                () -> assertEquals(Double.parseDouble(bakiye), bakiye(hesapNo), 0.001, id),   // G-4
                () -> assertEquals(100.0, bakiye(diger), 0.001, id),                          // G-5
                () -> assertEquals(durum, durum(hesapNo), id));
    }

    private Yanit olayUygula(String islem, String hesapNo, String diger) {
        switch (islem) {
            case "deposit":
                return paraYatir(hesapNo, "10");
            case "withdraw":
                return paraCek(hesapNo, "10");
            case "transfer-gonder":
                return transfer(hesapNo, diger, "10");
            case "transfer-al":
                return transfer(diger, hesapNo, "10");
            case "interest":
                return hesapIslemi(hesapNo, "interest");
            default:
                throw new IllegalArgumentException(islem);
        }
    }

    @ParameterizedTest(name = "ST-22: {0} hesapta sorgulama ve geçmiş → 200")
    @CsvSource({"FROZEN", "CLOSED"})
    @DisplayName("ST-22: Sorgulama ve işlem geçmişi her durumda serbest (§4.3, R-9.4)")
    void st22_sorgulamaHerDurumdaSerbest(String durum) {
        String hesapNo = hesapHazirla(durum, "0");

        Yanit sorgu = hesap(hesapNo);
        Yanit gecmisYanit = api.get("/api/accounts/" + hesapNo + "/transactions");

        assertAll(
                () -> assertEquals(200, sorgu.durum(), sorgu.ham()),
                () -> assertEquals(200, gecmisYanit.durum(), gecmisYanit.ham()));
    }

    @Test
    @DisplayName("ST-23: Döngülü yol ACTIVE→FROZEN→ACTIVE→FROZEN→CLOSED başarılı, bakiye hep aynı")
    void st23_donguluYol() {
        String hesapNo = hesapAc("STANDARD", "0");

        int k1 = hesapIslemi(hesapNo, "freeze").durum();
        int k2 = hesapIslemi(hesapNo, "unfreeze").durum();
        int k3 = hesapIslemi(hesapNo, "freeze").durum();
        int k4 = hesapIslemi(hesapNo, "close").durum();

        assertAll(
                () -> assertEquals(200, k1),
                () -> assertEquals(200, k2),
                () -> assertEquals(200, k3),
                () -> assertEquals(200, k4),
                () -> assertEquals("CLOSED", durum(hesapNo)),                                 // R-7.4 son durum
                () -> assertEquals(0, gecmis(hesapNo).size(), "durum değişiklikleri kayıt oluşturmaz (§6)"));
    }
}
