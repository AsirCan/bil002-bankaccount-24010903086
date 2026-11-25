package tr.edu.sakarya.bil002.bdd;

import io.cucumber.java.en.When;
import java.util.Locale;

/** Transfer adımları (transfer.feature). */
public class TransferAdimlari {

    private final SenaryoBaglami baglam;

    public TransferAdimlari(SenaryoBaglami baglam) {
        this.baglam = baglam;
    }

    @When("{string} hesabından {string} hesabına {double} TL transfer edilir")
    public void transferEdilir(String kaynak, String hedef, double tutar) {
        baglam.sonYanit(baglam.api().post("/api/transactions/transfer", String.format(Locale.ROOT,
                "{\"fromAccount\":\"%s\",\"toAccount\":\"%s\",\"amount\":%.2f}",
                baglam.hesapNo(kaynak), baglam.hesapNo(hedef), tutar)));
    }
}
