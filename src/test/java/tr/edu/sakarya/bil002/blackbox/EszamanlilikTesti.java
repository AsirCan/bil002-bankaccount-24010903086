package tr.edu.sakarya.bil002.blackbox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tr.edu.sakarya.bil002.destek.ApiTestTabani;

/**
 * Eşzamanlılık testleri (concurrency): aynı anda gelen isteklerde bakiye tutarlı kalıyor mu?
 * Kayıp güncelleme (lost update) ve yarış durumu (race condition) ancak eşzamanlı istekle görülür.
 * Yük küçük tutuldu (20 iş parçacığı); asıl yük testi JMeter'de (performans/).
 */
@DisplayName("Kara kutu · Eşzamanlılık")
class EszamanlilikTesti extends ApiTestTabani {

    private static final int IS_PARCACIGI = 20;

    /** Görevleri aynı anda çalıştırır, her birinin HTTP kodunu döndürür. */
    private List<Integer> esZamanliCalistir(List<Callable<Integer>> gorevler) throws Exception {
        ExecutorService havuz = Executors.newFixedThreadPool(IS_PARCACIGI);
        try {
            List<Integer> kodlar = new ArrayList<>();
            for (Future<Integer> sonuc : havuz.invokeAll(gorevler)) {
                kodlar.add(sonuc.get());
            }
            return kodlar;
        } finally {
            havuz.shutdown();
        }
    }

    // R-2.5: Her başarılı yatırmada yeni bakiye = eski bakiye + amount (eşzamanlı isteklerde de). Rapor: H-13
    @Test
    @Tag("hata")
    @DisplayName("EZ-01: 100 eşzamanlı 1 TL yatırma → hepsi 200 ise bakiye tam 100 TL olmalı (kayıp güncelleme yok)")
    void ez01_esZamanliYatirma_kayipGuncellemeOlmamali() throws Exception {
        String hesapNo = hesapAc("STANDARD", "0");
        List<Callable<Integer>> gorevler = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            gorevler.add(() -> paraYatir(hesapNo, "1").durum());
        }

        List<Integer> kodlar = esZamanliCalistir(gorevler);

        long basarili = kodlar.stream().filter(k -> k == 200).count();
        assertAll(
                () -> assertEquals(100, basarili, "200 dönen yatırma sayısı, kodlar: " + kodlar),
                () -> assertEquals((double) basarili, bakiye(hesapNo), 0.001,
                        "bakiye, başarılı yatırma sayısı kadar olmalı"),                       // R-2.5
                () -> assertEquals(basarili, kayitSayisi(hesapNo, "DEPOSIT", "SUCCESS")));    // R-2.6
    }

    // G-5, R-4.6: Transfer atomik; karşılıklı transferlerde iki hesabın toplamı sabit kalır. Rapor: H-14
    @Test
    @Tag("hata")
    @DisplayName("EZ-02: 60 eşzamanlı karşılıklı 10 TL transfer → hepsi 200 ve A + B toplamı 10.000 TL kalmalı")
    void ez02_esZamanliTransfer_toplamKorunmali() throws Exception {
        String hesapA = hesapAc("PREMIUM", "5000");
        String hesapB = hesapAc("PREMIUM", "5000");
        List<Callable<Integer>> gorevler = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            boolean aDanB = i % 2 == 0;
            gorevler.add(() -> aDanB
                    ? transfer(hesapA, hesapB, "10").durum()
                    : transfer(hesapB, hesapA, "10").durum());
        }

        List<Integer> kodlar = esZamanliCalistir(gorevler);

        double toplam = bakiye(hesapA) + bakiye(hesapB);
        assertAll(
                () -> assertTrue(kodlar.stream().allMatch(k -> k == 200), "geçerli transferler 200 dönmeli: " + kodlar),
                () -> assertEquals(10000.0, toplam, 0.001, "para yoktan var olmamalı / kaybolmamalı")); // G-5
    }

    @Test
    @DisplayName("EZ-03: 50 eşzamanlı hesap açma → bütün numaralar benzersiz ve 10 haneli")
    void ez03_esZamanliHesapAcma_numaralarBenzersiz() throws Exception {
        List<Callable<Integer>> gorevler = new ArrayList<>();
        Set<String> numaralar = java.util.Collections.synchronizedSet(new HashSet<>());
        for (int i = 0; i < 50; i++) {
            gorevler.add(() -> {
                numaralar.add(hesapAc("STANDARD", "1"));
                return 201;
            });
        }

        esZamanliCalistir(gorevler);

        assertAll(
                () -> assertEquals(50, numaralar.size(), "tekrar eden numara olmamalı"),       // R-1.5
                () -> assertTrue(numaralar.stream().allMatch(n -> n.matches("[1-9][0-9]{9}"))));
    }
}
