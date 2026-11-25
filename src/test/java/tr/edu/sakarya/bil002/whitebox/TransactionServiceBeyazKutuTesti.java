package tr.edu.sakarya.bil002.whitebox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tr.edu.sakarya.bil002.exception.InvalidInputException;
import tr.edu.sakarya.bil002.exception.LimitExceededException;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.repository.AccountRepository;
import tr.edu.sakarya.bil002.repository.TransactionRepository;
import tr.edu.sakarya.bil002.service.AccountService;
import tr.edu.sakarya.bil002.service.TransactionService;

/**
 * Beyaz kutu (Ödev 5.2): TransactionService'in dalları. Spring YOK: nesneler elle kurulur,
 * saat elle verilir. Bu yüzden milisaniyeler içinde koşar (F.I.R.S.T. - Fast).
 * Dal listesi ve hangi testin hangi dalı kapsadığı: notlar/DAL_LISTESI.md, Rapor Bölüm 3.
 */
@DisplayName("Beyaz kutu · TransactionService")
class TransactionServiceBeyazKutuTesti {

    private AyarlanabilirSaat saat;
    private TransactionRepository islemDeposu;
    private TransactionService islemServisi;
    private AccountService hesapServisi;

    @BeforeEach
    void kur() {
        saat = new AyarlanabilirSaat(LocalDateTime.of(2026, 11, 25, 14, 0));
        AccountRepository hesapDeposu = new AccountRepository();
        islemDeposu = new TransactionRepository();
        islemServisi = new TransactionService(hesapDeposu, islemDeposu, saat);
        hesapServisi = new AccountService(hesapDeposu, islemServisi, saat);
    }

    private BigDecimal tl(String deger) {
        return new BigDecimal(deger);
    }

    // ---------- G-6: zamana bağlı kural ----------

    @Test
    @DisplayName("WB-01: Dün çekilen 5.000 TL bugünkü limite sayılmaz: gün değişince limit sıfırlanır (G-6)")
    void wb01_gunDegisinceLimitSifirlanir() {
        String no = hesapServisi.createAccount("A", "STANDARD", tl("20000")).getAccountNumber();
        hesapServisi.withdraw(no, tl("5000"));                                    // bugünün limiti doldu
        assertThrows(LimitExceededException.class, () -> hesapServisi.withdraw(no, tl("0.01")));

        saat.ilerlet(Duration.ofDays(1));                                         // ertesi gün

        Account sonuc = hesapServisi.withdraw(no, tl("5000"));
        assertEquals(0, tl("10000").compareTo(sonuc.getBalance()));
    }

    @Test
    @DisplayName("WB-02: Gece yarısı sınırı: 23:59:59 ile 00:00:00 farklı günlerdir (G-6, BVA)")
    void wb02_geceYarisiSiniri() {
        String no = hesapServisi.createAccount("A", "STANDARD", tl("20000")).getAccountNumber();
        saat.ayarla(LocalDateTime.of(2026, 11, 25, 23, 59, 58));
        hesapServisi.withdraw(no, tl("5000"));

        saat.ayarla(LocalDateTime.of(2026, 11, 25, 23, 59, 59));                 // hâlâ aynı gün
        assertThrows(LimitExceededException.class, () -> hesapServisi.withdraw(no, tl("0.01")));

        saat.ayarla(LocalDateTime.of(2026, 11, 26, 0, 0, 0));                    // yeni gün
        assertEquals(0, tl("14999.99").compareTo(hesapServisi.withdraw(no, tl("0.01")).getBalance()));
    }

    // ---------- MC/DC: (A) WITHDRAWAL && (B) SUCCESS && (C) bugün ----------

    private void kayitEkle(String no, TransactionType tip, TransactionStatus durum, LocalDateTime zaman, String tutar) {
        Transaction t = new Transaction();
        t.setAccountNumber(no);
        t.setType(tip);
        t.setStatus(durum);
        t.setAmount(tl(tutar));
        t.setBalanceAfter(BigDecimal.ZERO);
        t.setTimestamp(zaman);
        islemDeposu.save(t);
    }

    @Test
    @DisplayName("WB-03: MC/DC günlük toplam: A&&B&&C için n+1 = 4 durum, her koşul sonucu tek başına değiştirir")
    void wb03_gunlukToplam_mcdc() {
        LocalDateTime bugun = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime dun = bugun.minusDays(1);
        kayitEkle("1111111111", TransactionType.WITHDRAWAL, TransactionStatus.SUCCESS, bugun, "100"); // A=D B=D C=D → sayılır
        kayitEkle("2222222222", TransactionType.TRANSFER, TransactionStatus.SUCCESS, bugun, "100");   // A=Y        → sayılmaz
        kayitEkle("3333333333", TransactionType.WITHDRAWAL, TransactionStatus.FAILED, bugun, "100");  // B=Y        → sayılmaz
        kayitEkle("4444444444", TransactionType.WITHDRAWAL, TransactionStatus.SUCCESS, dun, "100");   // C=Y        → sayılmaz

        assertAll(
                () -> assertEquals(0, tl("100").compareTo(islemServisi.todaysSuccessfulWithdrawals("1111111111"))),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(islemServisi.todaysSuccessfulWithdrawals("2222222222"))),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(islemServisi.todaysSuccessfulWithdrawals("3333333333"))),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(islemServisi.todaysSuccessfulWithdrawals("4444444444"))));
    }

    // ---------- Transfer: (fromAccount == null || toAccount == null || amount == null) ----------

    @Test
    @DisplayName("WB-04: Transfer eksik alan koşulu: her alanın tek başına null olması 400 (MC/DC, 3 koşul)")
    void wb04_transferEksikAlan() {
        String a = hesapServisi.createAccount("A", "STANDARD", tl("100")).getAccountNumber();
        String b = hesapServisi.createAccount("B", "STANDARD", tl("0")).getAccountNumber();

        assertAll(
                () -> assertThrows(InvalidInputException.class, () -> islemServisi.transfer(null, b, tl("1"))),
                () -> assertThrows(InvalidInputException.class, () -> islemServisi.transfer(a, null, tl("1"))),
                () -> assertThrows(InvalidInputException.class, () -> islemServisi.transfer(a, b, null)));
        islemServisi.transfer(a, b, tl("1"));                                     // hepsi dolu → geçer
        assertEquals(0, tl("99").compareTo(hesapServisi.getAccount(a).getBalance()));
    }

    // ---------- R-9.3: aynı zaman damgalı işlemlerde sıra ----------

    @Test
    @DisplayName("WB-05: Aynı zaman damgalı işlemlerde de en yeni (son eklenen) önce gelir (thenComparing id)")
    void wb05_ayniZamanDamgasindaSiralama() {
        String no = hesapServisi.createAccount("A", "STANDARD", tl("100")).getAccountNumber();
        hesapServisi.deposit(no, tl("1"));                                        // saat durduğu için
        hesapServisi.withdraw(no, tl("2"));                                       // ikisinin zamanı aynı

        List<Transaction> gecmis = islemServisi.getHistory(no);

        assertAll(
                () -> assertEquals(gecmis.get(0).getTimestamp(), gecmis.get(1).getTimestamp()),
                () -> assertEquals(TransactionType.WITHDRAWAL, gecmis.get(0).getType()),
                () -> assertEquals(TransactionType.DEPOSIT, gecmis.get(1).getType()));
    }

    @Test
    @DisplayName("WB-06: Başarısız çekim kaydı (FAILED) sonraki çekimlerin günlük toplamına girmez")
    void wb06_failedKayitLimiteGirmez() {
        String no = hesapServisi.createAccount("A", "STANDARD", tl("10000")).getAccountNumber();
        assertThrows(LimitExceededException.class, () -> hesapServisi.withdraw(no, tl("5000.01")));   // FAILED kayıt

        Account sonuc = hesapServisi.withdraw(no, tl("5000"));                    // FAILED sayılsaydı red olurdu

        assertEquals(0, tl("5000").compareTo(sonuc.getBalance()));
    }
}
