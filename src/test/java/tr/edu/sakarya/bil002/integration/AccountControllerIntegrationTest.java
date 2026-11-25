package tr.edu.sakarya.bil002.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * API / entegrasyon testi (Ödev 5.5): AccountController + servis + repository birlikte.
 * MockMvc gerçek bir HTTP sunucusu AÇMAZ; isteği Spring MVC katmanına doğrudan verir (hızlı).
 * Her endpoint için en az bir başarılı ve bir hatalı istek: durum kodu + JSON gövdesi doğrulanır.
 * Bu testler sadece referans API'yi test eder (canlıya gidemez); canlı karşılığı Postman koleksiyonu.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Entegrasyon · AccountController (MockMvc)")
class AccountControllerIntegrationTest {

    private static final String OLMAYAN = "9999999999";

    @Autowired
    private MockMvc mockMvc;

    private ResultActions postJson(String yol, String govde) throws Exception {
        return mockMvc.perform(post(yol).contentType(MediaType.APPLICATION_JSON).content(govde));
    }

    private String hesapAc(String tur, String bakiye) throws Exception {
        String yanit = postJson("/api/accounts",
                "{\"ownerName\":\"MockMvc\",\"accountType\":\"" + tur + "\",\"initialBalance\":" + bakiye + "}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(yanit, "$.accountNumber");
    }

    @Test
    @DisplayName("API-01: POST /api/accounts geçerli → 201 ve hesap JSON'u")
    void api01_hesapAc_201() throws Exception {
        postJson("/api/accounts", "{\"ownerName\":\"Ayşe\",\"accountType\":\"PREMIUM\",\"initialBalance\":250.5}")
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))   // G-3
                .andExpect(jsonPath("$.accountNumber").value(org.hamcrest.Matchers.matchesPattern("[1-9][0-9]{9}")))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.accountType").value("PREMIUM"))
                .andExpect(jsonPath("$.balance").value(250.5));
    }

    @Test
    @DisplayName("API-03: POST /api/accounts geçersiz tür → 400 ve §7 hata gövdesi")
    void api03_hesapAc_gecersizTur_400() throws Exception {
        postJson("/api/accounts", "{\"ownerName\":\"Ali\",\"accountType\":\"GOLD\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))                                   // §7 biçimi
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/accounts"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("API-03b: POST /api/accounts bozuk JSON → 400")
    void api03b_hesapAc_bozukJson_400() throws Exception {
        postJson("/api/accounts", "{\"ownerName\": ").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API-05: GET /api/accounts → 200 ve dizi")
    void api05_tumHesaplar_200() throws Exception {
        hesapAc("STANDARD", "1");

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("API-06/07: GET /api/accounts/{no} var → 200, yok → 404")
    void api06_07_hesapSorgula() throws Exception {
        String no = hesapAc("STANDARD", "10");

        mockMvc.perform(get("/api/accounts/" + no))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(no));
        mockMvc.perform(get("/api/accounts/" + OLMAYAN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("API-08/09: POST deposit geçerli → 200, limit üstü → 422")
    void api08_09_yatirma() throws Exception {
        String no = hesapAc("STANDARD", "1000");

        postJson("/api/accounts/" + no + "/deposit", "{\"amount\":500}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
        postJson("/api/accounts/" + no + "/deposit", "{\"amount\":10000.01}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("API-09b: POST deposit eksik amount / gövdesiz → 400")
    void api09b_yatirma_eksikAlan_400() throws Exception {
        String no = hesapAc("STANDARD", "1000");

        postJson("/api/accounts/" + no + "/deposit", "{}").andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/accounts/" + no + "/deposit")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API-11/12: POST withdraw geçerli → 200, olmayan hesap → 404")
    void api11_12_cekme() throws Exception {
        String no = hesapAc("STANDARD", "1000");

        postJson("/api/accounts/" + no + "/withdraw", "{\"amount\":200}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800));
        postJson("/api/accounts/" + OLMAYAN + "/withdraw", "{\"amount\":200}")
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("API-22/23: POST freeze → 200 FROZEN, tekrar → 409")
    void api22_23_dondurma() throws Exception {
        String no = hesapAc("STANDARD", "100");

        mockMvc.perform(post("/api/accounts/" + no + "/freeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
        mockMvc.perform(post("/api/accounts/" + no + "/freeze"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("API-24/25: POST unfreeze FROZEN → 200 ACTIVE, ACTIVE → 409")
    void api24_25_dondurmaKaldirma() throws Exception {
        String no = hesapAc("STANDARD", "100");
        mockMvc.perform(post("/api/accounts/" + no + "/freeze"));

        mockMvc.perform(post("/api/accounts/" + no + "/unfreeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(post("/api/accounts/" + no + "/unfreeze"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("API-26/27: POST close bakiyeli → 409, bakiye 0 → 200 CLOSED")
    void api26_27_kapatma() throws Exception {
        String bakiyeli = hesapAc("STANDARD", "0.01");
        String sifir = hesapAc("STANDARD", "0");

        mockMvc.perform(post("/api/accounts/" + bakiyeli + "/close"))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/accounts/" + sifir + "/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("API-13/14: POST interest → 200 kuruşlu faiz, FROZEN → 409")
    void api13_14_faiz() throws Exception {
        String no = hesapAc("PREMIUM", "2500.50");
        String donuk = hesapAc("STANDARD", "100");
        mockMvc.perform(post("/api/accounts/" + donuk + "/freeze"));

        mockMvc.perform(post("/api/accounts/" + no + "/interest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(2510.92));                             // R-8.3 örneği
        mockMvc.perform(post("/api/accounts/" + donuk + "/interest"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("API-18/19: GET transactions → 200 en yeni önce, olmayan → 404")
    void api18_19_islemGecmisi() throws Exception {
        String no = hesapAc("STANDARD", "100");
        postJson("/api/accounts/" + no + "/deposit", "{\"amount\":10}");
        postJson("/api/accounts/" + no + "/withdraw", "{\"amount\":5}");

        mockMvc.perform(get("/api/accounts/" + no + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("WITHDRAWAL"))                         // R-9.3
                .andExpect(jsonPath("$[0].balanceAfter").value(105));
        mockMvc.perform(get("/api/accounts/" + OLMAYAN + "/transactions"))
                .andExpect(status().isNotFound());
    }
}
