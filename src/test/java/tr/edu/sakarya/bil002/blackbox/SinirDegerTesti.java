package tr.edu.sakarya.bil002.blackbox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi.Yanit;
import tr.edu.sakarya.bil002.destek.ApiTestTabani;

/**
 * Sınır Değer Analizi (Boundary Value Analysis, BVA) · Rapor 2.2.
 * 3-değerli BVA: her sınır için sınırın hemen altı, kendisi ve hemen üstü.
 * Adım 0,01 TL çünkü en küçük birim 1 kuruş (G-1). Limitler kapsayıcıdır (Bölüm 3).
 * Her satır: girdi değeri → beklenen HTTP kodu → beklenen bakiye (reddedilince bakiye değişmez, G-4).
 */
@DisplayName("Kara kutu · BVA (sınır değerler)")
class SinirDegerTesti extends ApiTestTabani {

    // R-2.2: amount <= 0 (0 dahil) reddedilir. Rapor: H-05
    @ParameterizedTest(name = "BVA-01: {0} TL yatırma → {1}, bakiye {2}")
    @Tag("hata")
    @CsvSource({"0.00, 400, 100.00", "0.01, 200, 100.01", "0.02, 200, 100.02"})
    @DisplayName("BVA-01: Yatırma tutarı alt sınırı (amount > 0)")
    void bva01_yatirmaAltSinir(String tutar, int beklenenKod, double beklenenBakiye) {
        String hesapNo = hesapAc("STANDARD", "100");

        Yanit yanit = paraYatir(hesapNo, tutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), yanit.ham()),                  // R-2.2
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    @ParameterizedTest(name = "BVA-02: STANDARD {0} TL yatırma → {1}")
    @CsvSource({"9999.99, 200, 9999.99", "10000.00, 200, 10000.00", "10000.01, 422, 0.00"})
    @DisplayName("BVA-02: STANDARD tek seferlik yatırma limiti 10.000 TL (dahil)")
    void bva02_standartYatirmaLimiti(String tutar, int beklenenKod, double beklenenBakiye) {
        String hesapNo = hesapAc("STANDARD", "0");

        Yanit yanit = paraYatir(hesapNo, tutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), yanit.ham()),                  // R-2.4, Bölüm 3
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    @ParameterizedTest(name = "BVA-03: PREMIUM {0} TL yatırma → {1}")
    @CsvSource({"49999.99, 200, 49999.99", "50000.00, 200, 50000.00", "50000.01, 422, 0.00"})
    @DisplayName("BVA-03: PREMIUM tek seferlik yatırma limiti 50.000 TL (dahil)")
    void bva03_premiumYatirmaLimiti(String tutar, int beklenenKod, double beklenenBakiye) {
        String hesapNo = hesapAc("PREMIUM", "0");

        Yanit yanit = paraYatir(hesapNo, tutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), yanit.ham()),                  // R-2.4
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    // R-3.4: amount <= bakiye; bakiyenin tamamı çekilebilir. Rapor: H-06
    @ParameterizedTest(name = "BVA-04: bakiye 1000, {0} TL çekme → {1}")
    @Tag("hata")
    @CsvSource({"999.99, 200, 0.01", "1000.00, 200, 0.00", "1000.01, 422, 1000.00"})
    @DisplayName("BVA-04: Çekim tutarı bakiye sınırı (tamamı çekilebilir)")
    void bva04_cekimBakiyeSiniri(String tutar, int beklenenKod, double beklenenBakiye) {
        String hesapNo = hesapAc("STANDARD", "1000");

        Yanit yanit = paraCek(hesapNo, tutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), yanit.ham()),                  // R-3.4
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    @ParameterizedTest(name = "BVA-05: STANDARD 3000 + {0} TL çekme (toplam {3}) → {1}")
    @CsvSource({"1999.99, 200, 5000.01, 4999.99", "2000.00, 200, 5000.00, 5000.00", "2000.01, 422, 7000.00, 5000.01"})
    @DisplayName("BVA-05: STANDARD günlük çekim limiti 5.000 TL, birden fazla çekimin TOPLAMI (dahil)")
    void bva05_standartGunlukLimit(String ikinciTutar, int beklenenKod, double beklenenBakiye, String toplam) {
        String hesapNo = hesapAc("STANDARD", "10000");
        paraCek(hesapNo, "3000");

        Yanit yanit = paraCek(hesapNo, ikinciTutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), "toplam " + toplam + ": " + yanit.ham()), // R-3.5
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    // R-3.5, Bölüm 3: PREMIUM günde toplam tam 20.000 TL çekilebilir. Rapor: H-07
    @ParameterizedTest(name = "BVA-06: PREMIUM 15000 + {0} TL çekme (toplam {3}) → {1}")
    @Tag("hata")
    @CsvSource({"4999.99, 200, 10000.01, 19999.99", "5000.00, 200, 10000.00, 20000.00",
        "5000.01, 422, 15000.00, 20000.01"})
    @DisplayName("BVA-06: PREMIUM günlük çekim limiti 20.000 TL, birden fazla çekimin TOPLAMI (dahil)")
    void bva06_premiumGunlukLimit(String ikinciTutar, int beklenenKod, double beklenenBakiye, String toplam) {
        String hesapNo = hesapAc("PREMIUM", "30000");
        paraCek(hesapNo, "15000");

        Yanit yanit = paraCek(hesapNo, ikinciTutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), "toplam " + toplam + ": " + yanit.ham()), // R-3.5
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    // R-1.4: initialBalance negatif ise reddedilir. Rapor: H-02
    @ParameterizedTest(name = "BVA-07: initialBalance {0} → {1}")
    @Tag("hata")
    @CsvSource({"-0.01, 400", "0.00, 201", "0.01, 201"})
    @DisplayName("BVA-07: Başlangıç bakiyesi alt sınırı (0 dahil kabul, negatif red)")
    void bva07_baslangicBakiyesiSiniri(String bakiye, int beklenenKod) {
        Yanit yanit = api.post("/api/accounts",
                "{\"ownerName\":\"Sınır\",\"accountType\":\"STANDARD\",\"initialBalance\":" + bakiye + "}");

        assertEquals(beklenenKod, yanit.durum(), yanit.ham());                                // R-1.4
    }

    @ParameterizedTest(name = "BVA-08: kaynak bakiye 500, {0} TL transfer → {1}")
    @CsvSource({"499.99, 200, 0.01, 499.99", "500.00, 200, 0.00, 500.00", "500.01, 422, 500.00, 0.00"})
    @DisplayName("BVA-08: Transfer tutarı kaynak bakiye sınırı (tamamı gönderilebilir)")
    void bva08_transferBakiyeSiniri(String tutar, int beklenenKod, double kaynakBakiye, double hedefBakiye) {
        String kaynak = hesapAc("STANDARD", "500");
        String hedef = hesapAc("STANDARD", "0");

        Yanit yanit = transfer(kaynak, hedef, tutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), yanit.ham()),                  // R-4.5
                () -> assertEquals(kaynakBakiye, bakiye(kaynak), 0.001),                      // R-4.6 / G-5
                () -> assertEquals(hedefBakiye, bakiye(hedef), 0.001));
    }

    @ParameterizedTest(name = "BVA-09: {0} TL çekme → {1}")
    @CsvSource({"0.00, 400, 100.00", "0.01, 200, 99.99", "0.02, 200, 99.98"})
    @DisplayName("BVA-09: Çekim tutarı alt sınırı (amount > 0)")
    void bva09_cekimAltSinir(String tutar, int beklenenKod, double beklenenBakiye) {
        String hesapNo = hesapAc("STANDARD", "100");

        Yanit yanit = paraCek(hesapNo, tutar);

        assertAll(
                () -> assertEquals(beklenenKod, yanit.durum(), yanit.ham()),                  // R-3.2
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001));
    }

    // R-8.3: Faiz kuruşa yuvarlanır (yarım yukarı). Rapor: H-04
    @ParameterizedTest(name = "BVA-10: STANDARD bakiye {0}, faiz {2} → yeni bakiye {1}")
    @Tag("hata")
    @CsvSource({"1.96, 1.96, 0.0049", "2.00, 2.01, 0.0050", "2.04, 2.05, 0.0051"})
    @DisplayName("BVA-10: Faiz yuvarlama sınırı 0,005 (yarım yukarı: 0,005 → 0,01)")
    void bva10_faizYuvarlamaSiniri(String bakiye, double beklenenBakiye, String hamFaiz) {
        String hesapNo = hesapAc("STANDARD", bakiye);

        Yanit yanit = hesapIslemi(hesapNo, "interest");

        assertAll(
                () -> assertEquals(200, yanit.durum(), yanit.ham()),
                () -> assertEquals(beklenenBakiye, bakiye(hesapNo), 0.001, "ham faiz " + hamFaiz)); // R-8.3
    }
}
