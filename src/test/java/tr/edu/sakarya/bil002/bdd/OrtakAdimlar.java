package tr.edu.sakarya.bil002.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.tr.Diyelimki;
import io.cucumber.java.tr.Ozaman;
import io.cucumber.java.tr.Ve;
import java.util.Locale;
import java.util.Map;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi;

/**
 * Birden fazla feature dosyasında kullanılan ortak adımlar: hesap hazırlama ve sonuç doğrulama.
 * Kural: aynı adım cümlesi iki sınıfta tanımlanmaz (Cucumber "ambiguous step" hatası verir),
 * o yüzden ortak cümleler sadece burada.
 */
public class OrtakAdimlar {

    /** İş dilindeki red gerekçesi → Gereksinimler §7'deki HTTP kodu. */
    private static final Map<String, Integer> GEREKCE_KODU = Map.of(
            "geçersiz girdi", 400,
            "hesap bulunamadı", 404,
            "hesap durumu uygun değil", 409,
            "bakiye veya limit yetersiz", 422);

    private final SenaryoBaglami baglam;

    public OrtakAdimlar(SenaryoBaglami baglam) {
        this.baglam = baglam;
    }

    @Diyelimki("{string} adına {double} TL bakiyeli bir {word} hesabı var")
    public void hesapVar(String ad, double bakiye, String tur) {
        ApiIstemcisi.Yanit yanit = baglam.api().post("/api/accounts", String.format(Locale.ROOT,
                "{\"ownerName\":\"%s\",\"accountType\":\"%s\",\"initialBalance\":%.2f}", ad, tur, bakiye));
        assertEquals(201, yanit.durum(), "hazırlık: hesap açılamadı " + yanit.ham());
        baglam.hesapKaydet(ad, yanit.govde().get("accountNumber").asText());
    }

    @Ve("{string} hesabı dondurulmuş")
    public void hesapDondurulmus(String ad) {
        baglam.api().post("/api/accounts/" + baglam.hesapNo(ad) + "/freeze");
        assertEquals("FROZEN", baglam.hesapBilgisi(ad).path("status").asText(), "hazırlık: hesap dondurulamadı");
    }

    @Ozaman("işlem kabul edilir")
    public void islemKabulEdilir() {
        int kod = baglam.sonYanit().durum();
        assertTrue(kod >= 200 && kod < 300, "işlem kabul edilmeliydi: " + kod + " " + baglam.sonYanit().ham());
    }

    @Ozaman("işlem {string} gerekçesiyle reddedilir")
    public void islemReddedilir(String gerekce) {
        Integer beklenen = GEREKCE_KODU.get(gerekce);
        assertEquals(beklenen, baglam.sonYanit().durum(), gerekce + ": " + baglam.sonYanit().ham());
    }

    @Ozaman("işlem reddedilir")
    public void islemReddedilirGerekcesiz() {
        int kod = baglam.sonYanit().durum();
        assertTrue(kod >= 400 && kod < 500, "işlem reddedilmeliydi: " + kod + " " + baglam.sonYanit().ham());
    }

    @Ve("{string} hesabının bakiyesi {double} TL olur")
    public void bakiyeOlur(String ad, double beklenen) {
        assertEquals(beklenen, baglam.hesapBilgisi(ad).path("balance").asDouble(), 0.001);
    }

    @Ve("{string} hesabının durumu {word} olur")
    public void durumOlur(String ad, String beklenenDurum) {
        assertEquals(beklenenDurum, baglam.hesapBilgisi(ad).path("status").asText());
    }

    @Ve("{string} hesabının geçmişinde {int} adet başarısız {word} kaydı bulunur")
    public void basarisizKaydiBulunur(String ad, int adet, String tip) {
        int sayi = 0;
        for (JsonNode islem : baglam.gecmis(ad)) {
            if (tip.equals(islem.path("type").asText()) && "FAILED".equals(islem.path("status").asText())) {
                sayi++;
            }
        }
        assertEquals(adet, sayi, "FAILED " + tip + " kaydı sayısı");
    }

    @Ve("{string} hesabının son işlemi başarılı bir {word} kaydıdır")
    public void sonIslemBasarili(String ad, String tip) {
        JsonNode son = baglam.gecmis(ad).get(0);
        assertEquals(tip, son.path("type").asText());
        assertEquals("SUCCESS", son.path("status").asText());
    }
}
