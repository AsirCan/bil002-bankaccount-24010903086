package tr.edu.sakarya.bil002.blackbox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi.Yanit;
import tr.edu.sakarya.bil002.destek.ApiTestTabani;

/**
 * Karar Tablosu Testi (Decision Table Testing) · Rapor 2.3.
 * Tablo 1 (transfer): K1 kaynak var · K2 hedef var · K3 kaynak ACTIVE · K4 hedef ACTIVE ·
 * K5 farklı hesap · K6 amount &gt; 0 · K7 bakiye yeterli. 2^7 = 128 kombinasyon yerine indirgenmiş tablo:
 * bir koşul "hayır" olunca sonuç diğerlerinden bağımsız ("-"), o yüzden her sütunda tek bir koşul bozulur.
 * Tablo 2 (limitler): hesap türü × tutar × işlem.
 * Belge iki kural aynı anda bozulunca hangi kodun öncelikli olduğunu söylemez (Rapor 1.1 #1):
 * öyle sütunlarda sadece "reddedildi + bakiyeler aynı" doğrulanır.
 */
@DisplayName("Kara kutu · Karar tablosu (transfer ve limitler)")
class KararTablosuTesti extends ApiTestTabani {

    // ---------- Tablo 1: Transfer ----------

    @Test
    @DisplayName("KT-01: Bütün koşullar sağlanıyor → transfer yapılır, iki hesapta TRANSFER/SUCCESS kaydı")
    void kt01_tumKosullarSaglaniyor_transferYapilir() {
        String kaynak = hesapAc("STANDARD", "1000");
        String hedef = hesapAc("PREMIUM", "200");

        Yanit yanit = transfer(kaynak, hedef, "300");

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(700.0, bakiye(kaynak), 0.001),                             // R-4.6
                () -> assertEquals(500.0, bakiye(hedef), 0.001),                              // R-4.6
                () -> assertEquals(1, kayitSayisi(kaynak, "TRANSFER", "SUCCESS")),            // R-4.7
                () -> assertEquals(1, kayitSayisi(hedef, "TRANSFER", "SUCCESS")));            // R-4.7
    }

    @Test
    @DisplayName("KT-02: Kaynak FROZEN → 409, bakiyeler değişmez")
    void kt02_kaynakDondurulmus_409() {
        String kaynak = hesapAc("STANDARD", "1000");
        String hedef = hesapAc("STANDARD", "0");
        hesapIslemi(kaynak, "freeze");

        Yanit yanit = transfer(kaynak, hedef, "100");

        assertAll(
                () -> assertEquals(409, yanit.durum(), yanit.ham()),                          // R-4.3
                () -> assertEquals(1000.0, bakiye(kaynak), 0.001),                            // G-4, G-5
                () -> assertEquals(0.0, bakiye(hedef), 0.001));
    }

    // R-4.3: Hedef hesap FROZEN/CLOSED ise transfer reddedilir. Rapor: H-11
    @Test
    @Tag("hata")
    @DisplayName("KT-03: Hedef FROZEN → 409, bakiyeler değişmez")
    void kt03_hedefDondurulmus_409() {
        String kaynak = hesapAc("STANDARD", "1000");
        String hedef = hesapAc("STANDARD", "0");
        hesapIslemi(hedef, "freeze");

        Yanit yanit = transfer(kaynak, hedef, "100");

        assertAll(
                () -> assertEquals(409, yanit.durum(), yanit.ham()),                          // R-4.3
                () -> assertEquals(1000.0, bakiye(kaynak), 0.001),                            // G-5 atomiklik
                () -> assertEquals(0.0, bakiye(hedef), 0.001));
    }

    // R-4.3: Hedef hesap FROZEN/CLOSED ise transfer reddedilir. Rapor: H-11
    @Test
    @Tag("hata")
    @DisplayName("KT-04: Hedef CLOSED → 409, bakiyeler değişmez")
    void kt04_hedefKapali_409() {
        String kaynak = hesapAc("STANDARD", "1000");
        String hedef = hesapAc("STANDARD", "0");
        hesapIslemi(hedef, "close");

        Yanit yanit = transfer(kaynak, hedef, "100");

        assertAll(
                () -> assertEquals(409, yanit.durum(), yanit.ham()),                          // R-4.3
                () -> assertEquals(1000.0, bakiye(kaynak), 0.001),
                () -> assertEquals(0.0, bakiye(hedef), 0.001));
    }

    @Test
    @DisplayName("KT-05: Kaynak CLOSED (bakiyesi zorunlu olarak 0) → reddedilir, hedef değişmez")
    void kt05_kaynakKapali_reddedilir() {
        String kaynak = hesapAc("STANDARD", "0");
        String hedef = hesapAc("STANDARD", "50");
        hesapIslemi(kaynak, "close");

        Yanit yanit = transfer(kaynak, hedef, "10");

        // CLOSED (409) ve yetersiz bakiye (422) birlikte bozuluyor, belge öncelik vermiyor: sadece red doğrulanır
        assertAll(
                () -> assertTrue(yanit.durum() >= 400 && yanit.durum() < 500, yanit.ham()),   // R-4.3
                () -> assertEquals(50.0, bakiye(hedef), 0.001));
    }

    // R-4.4: Bir hesaptan aynı hesaba transfer reddedilir. Rapor: H-12
    @Test
    @Tag("hata")
    @DisplayName("KT-06: Kaynak = hedef (aynı hesap) → 400, bakiye değişmez")
    void kt06_ayniHesap_400() {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = transfer(hesapNo, hesapNo, "100");

        assertAll(
                () -> assertEquals(400, yanit.durum(), yanit.ham()),                          // R-4.4
                () -> assertEquals(1000.0, bakiye(hesapNo), 0.001));
    }

    @Test
    @DisplayName("KT-07: Bakiye yetersiz → 422, bakiyeler değişmez")
    void kt07_bakiyeYetersiz_422() {
        String kaynak = hesapAc("STANDARD", "100");
        String hedef = hesapAc("STANDARD", "0");

        Yanit yanit = transfer(kaynak, hedef, "150");

        assertAll(
                () -> assertEquals(422, yanit.durum(), yanit.ham()),                          // R-4.5
                () -> assertEquals(100.0, bakiye(kaynak), 0.001),
                () -> assertEquals(0.0, bakiye(hedef), 0.001));
    }

    @Test
    @DisplayName("KT-08: Hedef hesap yok → 404, kaynak bakiye değişmez")
    void kt08_hedefYok_404() {
        String kaynak = hesapAc("STANDARD", "1000");

        Yanit yanit = transfer(kaynak, "1000000005", "100");

        assertAll(
                () -> assertEquals(404, yanit.durum(), yanit.ham()),                          // R-4.2
                () -> assertEquals(1000.0, bakiye(kaynak), 0.001));                           // G-5
    }

    @Test
    @DisplayName("KT-09: Kaynak hesap yok → 404, hedef bakiye değişmez")
    void kt09_kaynakYok_404() {
        String hedef = hesapAc("STANDARD", "100");

        Yanit yanit = transfer("1000000006", hedef, "50");

        assertAll(
                () -> assertEquals(404, yanit.durum(), yanit.ham()),                          // R-4.2
                () -> assertEquals(100.0, bakiye(hedef), 0.001));
    }

    @ParameterizedTest(name = "KT-10: transfer tutarı {0} → 400")
    @ValueSource(strings = {"0", "-50"})
    @DisplayName("KT-10: Transfer tutarı sıfır ya da negatif → 400, bakiyeler değişmez")
    void kt10_gecersizTutar_400(String tutar) {
        String kaynak = hesapAc("STANDARD", "1000");
        String hedef = hesapAc("STANDARD", "0");

        Yanit yanit = transfer(kaynak, hedef, tutar);

        assertAll(
                () -> assertEquals(400, yanit.durum(), yanit.ham()),                          // R-4.1
                () -> assertEquals(1000.0, bakiye(kaynak), 0.001),
                () -> assertEquals(0.0, bakiye(hedef), 0.001));
    }

    @Test
    @DisplayName("KT-11: Transfer günlük çekim limitine sayılmaz: 5.000 transfer sonrası 5.000 çekilebilir")
    void kt11_transferGunlukLimiteSayilmaz() {
        // Bakiye 12.000: çekim sonrası bakiye 2.000 kalır, böylece "tam bakiye" sınırına (H-06) takılmaz.
        // İlk sürümde bakiye 10.000'di ve test H-06 yüzünden kırmızıydı: hata maskeleme (Rapor 10).
        String kaynak = hesapAc("STANDARD", "12000");
        String hedef = hesapAc("STANDARD", "0");
        transfer(kaynak, hedef, "5000");

        Yanit cekim = paraCek(kaynak, "5000");

        assertAll(
                () -> assertEquals(200, cekim.durum(), cekim.ham()),                          // R-4.8, Bölüm 3
                () -> assertEquals(2000.0, bakiye(kaynak), 0.001));
    }

    @Test
    @DisplayName("KT-12: Transferin limiti yok: STANDARD hesaba tek seferde 20.000 TL transfer edilebilir")
    void kt12_transferLimitiYok() {
        String kaynak = hesapAc("PREMIUM", "25000");
        String hedef = hesapAc("STANDARD", "0");

        Yanit yanit = transfer(kaynak, hedef, "20000");

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),                          // R-4.8 (Rapor 1.1 #6)
                () -> assertEquals(20000.0, bakiye(hedef), 0.001));
    }

    // ---------- Tablo 2: Hesap türü × tutar × işlem ----------

    @ParameterizedTest(name = "{0}: {1} hesapta {2} {3} TL (bakiye {4}) → {5}")
    @CsvSource({
        "KT-13, STANDARD, deposit, 10000.00, 0, 200",
        "KT-14, STANDARD, deposit, 10000.01, 0, 422",
        "KT-15, PREMIUM, deposit, 10000.01, 0, 200",
        "KT-16, PREMIUM, deposit, 50000.01, 0, 422",
        "KT-17, STANDARD, withdraw, 5000.01, 10000, 422",
        "KT-18, PREMIUM, withdraw, 5000.01, 10000, 200"
    })
    @DisplayName("KT-13..18: Limit karar tablosu, aynı tutar hesap türüne göre farklı sonuç verir")
    void kt13_18_limitKararTablosu(String id, String tur, String islem, String tutar, String bakiye, int beklenenKod) {
        String hesapNo = hesapAc(tur, bakiye);

        Yanit yanit = api.post("/api/accounts/" + hesapNo + "/" + islem, "{\"amount\":" + tutar + "}");

        assertEquals(beklenenKod, yanit.durum(), id + " " + yanit.ham());                    // R-2.4, R-3.5, Bölüm 3
    }
}
