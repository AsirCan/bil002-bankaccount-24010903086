package tr.edu.sakarya.bil002.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.spring.ScenarioScope;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.stereotype.Component;
import tr.edu.sakarya.bil002.destek.ApiIstemcisi;
import tr.edu.sakarya.bil002.destek.Hedef;

/**
 * Bir senaryonun ortak hafızası. {@code @ScenarioScope}: her senaryo için YENİ bir nesne oluşur,
 * senaryolar birbirini etkilemez. Adım sınıfları bu nesneyi constructor ile alır ve veriyi
 * böylece paylaşır ("Ayşe" adındaki hesabın numarası, son işlemin yanıtı...).
 */
@Component
@ScenarioScope
public class SenaryoBaglami {

    private final ApiIstemcisi api;
    private final Map<String, String> hesaplar = new HashMap<>();
    private ApiIstemcisi.Yanit sonYanit;

    public SenaryoBaglami(@LocalServerPort int yerelPort) {
        this.api = new ApiIstemcisi(Hedef.temelAdres(yerelPort));
    }

    public ApiIstemcisi api() {
        return api;
    }

    public void hesapKaydet(String ad, String hesapNo) {
        hesaplar.put(ad, hesapNo);
    }

    /** Senaryodaki kişi adına karşılık gelen hesap numarası. Tanımsız ad = olmayan hesap. */
    public String hesapNo(String ad) {
        return hesaplar.getOrDefault(ad, "1000000009");
    }

    public ApiIstemcisi.Yanit sonYanit() {
        return sonYanit;
    }

    public void sonYanit(ApiIstemcisi.Yanit yanit) {
        this.sonYanit = yanit;
    }

    public JsonNode hesapBilgisi(String ad) {
        return api.get("/api/accounts/" + hesapNo(ad)).govde();
    }

    public JsonNode gecmis(String ad) {
        return api.get("/api/accounts/" + hesapNo(ad) + "/transactions").govde();
    }
}
