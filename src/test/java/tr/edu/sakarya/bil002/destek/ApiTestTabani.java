package tr.edu.sakarya.bil002.destek;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * HTTP üzerinden çalışan testlerin (kara kutu, canlı kabul) ortak tabanı.
 * {@code @SpringBootTest(RANDOM_PORT)}: referans API gerçek bir sunucu olarak boş bir portta açılır.
 * Hedef canlı ise yine açılır ama testler canlı adrese gider.
 * Kural (F.I.R.S.T. - Independent): her test kendi YENİ hesabını açar, sabit hesap numarası yok.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiTestTabani {

    @LocalServerPort
    private int yerelPort;

    protected ApiIstemcisi api;

    @BeforeEach
    void istemciyiHazirla() {
        api = new ApiIstemcisi(Hedef.temelAdres(yerelPort));
    }

    /** Yeni hesap açar, hesap numarasını döndürür. Bakiye null ise alan gönderilmez (R-1.3). */
    protected String hesapAc(String tur, String bakiye) {
        String govde = bakiye == null
                ? "{\"ownerName\":\"Test Kullanıcı\",\"accountType\":\"" + tur + "\"}"
                : "{\"ownerName\":\"Test Kullanıcı\",\"accountType\":\"" + tur + "\",\"initialBalance\":" + bakiye + "}";
        ApiIstemcisi.Yanit yanit = api.post("/api/accounts", govde);
        if (yanit.durum() != 201) {
            throw new IllegalStateException("Hazırlık başarısız, hesap açılamadı: " + yanit.durum() + " " + yanit.ham());
        }
        return yanit.govde().get("accountNumber").asText();
    }

    protected ApiIstemcisi.Yanit paraYatir(String hesapNo, String tutar) {
        return api.post("/api/accounts/" + hesapNo + "/deposit", "{\"amount\":" + tutar + "}");
    }

    protected ApiIstemcisi.Yanit paraCek(String hesapNo, String tutar) {
        return api.post("/api/accounts/" + hesapNo + "/withdraw", "{\"amount\":" + tutar + "}");
    }

    protected ApiIstemcisi.Yanit transfer(String kaynak, String hedef, String tutar) {
        return api.post("/api/transactions/transfer",
                "{\"fromAccount\":\"" + kaynak + "\",\"toAccount\":\"" + hedef + "\",\"amount\":" + tutar + "}");
    }

    protected ApiIstemcisi.Yanit hesap(String hesapNo) {
        return api.get("/api/accounts/" + hesapNo);
    }

    protected double bakiye(String hesapNo) {
        return hesap(hesapNo).govde().get("balance").asDouble();
    }

    protected String durum(String hesapNo) {
        return hesap(hesapNo).govde().get("status").asText();
    }

    /** İşlem geçmişi (en yeni önce). */
    protected JsonNode gecmis(String hesapNo) {
        return api.get("/api/accounts/" + hesapNo + "/transactions").govde();
    }

    /** Geçmişte verilen tip ve durumdaki kayıt sayısı (ör. DEPOSIT + FAILED). */
    protected int kayitSayisi(String hesapNo, String tip, String durum) {
        int sayi = 0;
        for (JsonNode islem : gecmis(hesapNo)) {
            if (tip.equals(islem.path("type").asText()) && durum.equals(islem.path("status").asText())) {
                sayi++;
            }
        }
        return sayi;
    }

    protected ApiIstemcisi.Yanit hesapIslemi(String hesapNo, String islem) {
        return api.post("/api/accounts/" + hesapNo + "/" + islem);
    }
}
