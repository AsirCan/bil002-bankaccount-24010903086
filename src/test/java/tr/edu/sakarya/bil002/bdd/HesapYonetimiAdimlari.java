package tr.edu.sakarya.bil002.bdd;

import io.cucumber.java.en.When;
import java.util.Locale;

/** Hesap açma, durum değişiklikleri ve faiz adımları (hesap_acma, hesap_durumu, faiz feature'ları). */
public class HesapYonetimiAdimlari {

    private final SenaryoBaglami baglam;

    public HesapYonetimiAdimlari(SenaryoBaglami baglam) {
        this.baglam = baglam;
    }

    @When("{string} adına {word} türünde ve {double} TL başlangıç bakiyeli hesap açılmak istenir")
    public void hesapAcilmakIstenir(String ad, String tur, double bakiye) {
        baglam.sonYanit(baglam.api().post("/api/accounts", String.format(Locale.ROOT,
                "{\"ownerName\":\"%s\",\"accountType\":\"%s\",\"initialBalance\":%.2f}", ad, tur, bakiye)));
        if (baglam.sonYanit().durum() == 201) {
            baglam.hesapKaydet(ad, baglam.sonYanit().govde().path("accountNumber").asText());
        }
    }

    @When("{string} hesabı dondurulur")
    public void dondurulur(String ad) {
        durumIslemi(ad, "freeze");
    }

    @When("{string} hesabının dondurulması kaldırılır")
    public void dondurmaKaldirilir(String ad) {
        durumIslemi(ad, "unfreeze");
    }

    @When("{string} hesabı kapatılır")
    public void kapatilir(String ad) {
        durumIslemi(ad, "close");
    }

    @When("{string} hesabına aylık faiz uygulanır")
    public void faizUygulanir(String ad) {
        durumIslemi(ad, "interest");
    }

    private void durumIslemi(String ad, String islem) {
        baglam.sonYanit(baglam.api().post("/api/accounts/" + baglam.hesapNo(ad) + "/" + islem));
    }
}
