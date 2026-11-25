package tr.edu.sakarya.bil002.bdd;

import io.cucumber.java.en.When;
import io.cucumber.java.tr.Ve;
import java.util.Locale;

/**
 * Para yatırma ve çekme adımları (para_yatirma.feature, para_cekme.feature).
 * "Eğer ki" adımlarında İngilizce @When kullanıldı: Türkçe karşılığı olan anotasyonun adı
 * (Eğerki) Türkçe karakter içeriyor. Cucumber adımı anahtar kelimeye göre değil
 * cümleye göre eşlediği için sonuç aynıdır.
 */
public class ParaIslemleriAdimlari {

    private final SenaryoBaglami baglam;

    public ParaIslemleriAdimlari(SenaryoBaglami baglam) {
        this.baglam = baglam;
    }

    @When("{string} hesabına {double} TL yatırılır")
    public void paraYatirilir(String ad, double tutar) {
        baglam.sonYanit(baglam.api().post("/api/accounts/" + baglam.hesapNo(ad) + "/deposit",
                String.format(Locale.ROOT, "{\"amount\":%.2f}", tutar)));
    }

    @When("{string} hesabından {double} TL çekilir")
    public void paraCekilir(String ad, double tutar) {
        baglam.sonYanit(baglam.api().post("/api/accounts/" + baglam.hesapNo(ad) + "/withdraw",
                String.format(Locale.ROOT, "{\"amount\":%.2f}", tutar)));
    }

    @Ve("{string} hesabından bugün daha önce {double} TL çekilmiş")
    public void dahaOnceCekilmis(String ad, double tutar) {
        paraCekilir(ad, tutar);
        new OrtakAdimlar(baglam).islemKabulEdilir();
    }
}
