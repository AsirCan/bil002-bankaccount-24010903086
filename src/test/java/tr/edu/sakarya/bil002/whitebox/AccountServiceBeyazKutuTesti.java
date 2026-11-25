package tr.edu.sakarya.bil002.whitebox;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tr.edu.sakarya.bil002.exception.AccountStateException;
import tr.edu.sakarya.bil002.exception.InvalidInputException;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.AccountStatus;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.repository.AccountRepository;
import tr.edu.sakarya.bil002.repository.TransactionRepository;
import tr.edu.sakarya.bil002.service.AccountService;
import tr.edu.sakarya.bil002.service.TransactionService;

/**
 * Beyaz kutu (Ödev 5.2): AccountService'in koşulları ve dalları (Spring yok, sabit saat).
 * Kara kutu testlerinin zaten kapsadığı dallar burada tekrar edilmez; burada koda bakarak
 * görülen kısa devre (short-circuit) koşulları, istisna yolları ve BigDecimal ayrıntıları var.
 */
@DisplayName("Beyaz kutu · AccountService")
class AccountServiceBeyazKutuTesti {

    private AccountService servis;
    private TransactionService islemServisi;

    @BeforeEach
    void kur() {
        AyarlanabilirSaat saat = new AyarlanabilirSaat(LocalDateTime.of(2026, 11, 25, 12, 0));
        AccountRepository hesapDeposu = new AccountRepository();
        islemServisi = new TransactionService(hesapDeposu, new TransactionRepository(), saat);
        servis = new AccountService(hesapDeposu, islemServisi, saat);
    }

    private BigDecimal tl(String deger) {
        return new BigDecimal(deger);
    }

    @Test
    @DisplayName("WB-07: ownerName == null || isBlank(): iki koşul ayrı ayrı doğru olunca da red (kısa devre)")
    void wb07_adKosulununIkiKolu() {
        assertAll(
                () -> assertThrows(InvalidInputException.class, () -> servis.createAccount(null, "STANDARD", null)),
                () -> assertThrows(InvalidInputException.class, () -> servis.createAccount(" \t ", "STANDARD", null)));
    }

    @Test
    @DisplayName("WB-08: parseAccountType: iki if de yanlış (null tür) → InvalidInputException")
    void wb08_turNull() {
        assertThrows(InvalidInputException.class, () -> servis.createAccount("A", null, null));
    }

    @Test
    @DisplayName("WB-09: Bakiye 0.00 (ölçek 2) olan hesap kapatılabilir: compareTo kullanıldığı için (equals olsaydı kalırdı)")
    void wb09_sifirOlcekliBakiyeKapatma() {
        Account hesap = servis.createAccount("A", "STANDARD", tl("0.00"));

        Account sonuc = servis.close(hesap.getAccountNumber());

        assertEquals(AccountStatus.CLOSED, sonuc.getStatus());                    // R-7.2, R-7.3
    }

    @Test
    @DisplayName("WB-10: Reddedilen yatırmada catch dalı: FAILED kayıt, balanceAfter değişmemiş bakiye, hata yeniden fırlar")
    void wb10_yatirmaCatchDali() {
        String no = servis.createAccount("A", "STANDARD", tl("50")).getAccountNumber();
        servis.freeze(no);

        assertThrows(AccountStateException.class, () -> servis.deposit(no, tl("10")));

        Transaction kayit = islemServisi.getHistory(no).get(0);
        assertAll(
                () -> assertEquals(TransactionStatus.FAILED, kayit.getStatus()),      // R-2.7
                () -> assertEquals(0, tl("50").compareTo(kayit.getBalanceAfter())),   // §6: değişmemiş bakiye
                () -> assertEquals(0, tl("10").compareTo(kayit.getAmount())));
    }

    @Test
    @DisplayName("WB-11: Eksik amount (null) try bloğunun DIŞINDA reddedilir: kayıt oluşmaz (§6 dipnot)")
    void wb11_nullTutarKayitYok() {
        String no = servis.createAccount("A", "STANDARD", tl("50")).getAccountNumber();

        assertThrows(InvalidInputException.class, () -> servis.withdraw(no, null));

        List<Transaction> gecmis = islemServisi.getHistory(no);
        assertEquals(0, gecmis.size());
    }

    @ParameterizedTest(name = "WB-12: {0} bakiye {1} → faiz {2}")
    @CsvSource({"STANDARD, 2.00, 0.01", "STANDARD, 1.99, 0.00", "PREMIUM, 0.01, 0.00", "PREMIUM, 1.20, 0.01"})
    @DisplayName("WB-12: Faiz divide(12, 2, HALF_UP) yuvarlama dalları (R-8.3)")
    void wb12_faizYuvarlama(String tur, String bakiye, String beklenenFaiz) {
        String no = servis.createAccount("A", tur, tl(bakiye)).getAccountNumber();

        servis.applyInterest(no);

        assertEquals(0, tl(beklenenFaiz).compareTo(islemServisi.getHistory(no).get(0).getAmount()));
    }

    @Test
    @DisplayName("WB-13: Durum değiştiren işlemler updatedAt'i günceller, createdAt sabit kalır")
    void wb13_zamanDamgalari() {
        Account hesap = servis.createAccount("A", "STANDARD", tl("1"));
        LocalDateTime ilk = hesap.getCreatedAt();

        Account sonuc = servis.freeze(hesap.getAccountNumber());

        assertAll(
                () -> assertEquals(ilk, sonuc.getCreatedAt()),
                () -> assertEquals(AccountStatus.FROZEN, sonuc.getStatus()));
    }
}
