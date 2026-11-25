package tr.edu.sakarya.bil002.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * API / entegrasyon testi: TransactionController (transfer + işlem geçmişi alternatif yolu).
 * Transfer için başarılı / 400 / 404 / 409 / 422, geçmiş için 200 / 404.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Entegrasyon · TransactionController (MockMvc)")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions transfer(String kaynak, String hedef, String tutar) throws Exception {
        return mockMvc.perform(post("/api/transactions/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccount\":\"" + kaynak + "\",\"toAccount\":\"" + hedef + "\",\"amount\":" + tutar + "}"));
    }

    private String hesapAc(String bakiye) throws Exception {
        String yanit = mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerName\":\"MockMvc\",\"accountType\":\"STANDARD\",\"initialBalance\":" + bakiye + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(yanit, "$.accountNumber");
    }

    @Test
    @DisplayName("API-15: POST transfer geçerli → 200 ve başarı mesajı, iki bakiye güncel")
    void api15_transfer_200() throws Exception {
        String a = hesapAc("1000");
        String b = hesapAc("0");

        transfer(a, b, "250")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer başarılı"))
                .andExpect(jsonPath("$.fromAccount").value(a))
                .andExpect(jsonPath("$.toAccount").value(b))
                .andExpect(jsonPath("$.amount").value(250));
        mockMvc.perform(get("/api/accounts/" + a)).andExpect(jsonPath("$.balance").value(750));
        mockMvc.perform(get("/api/accounts/" + b)).andExpect(jsonPath("$.balance").value(250));
    }

    @Test
    @DisplayName("API-16/17: POST transfer aynı hesap / eksik alan / sıfır tutar → 400")
    void api16_17_transfer_400() throws Exception {
        String a = hesapAc("1000");

        transfer(a, a, "10").andExpect(status().isBadRequest());
        transfer(a, hesapAc("0"), "0").andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/transactions/transfer").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccount\":\"" + a + "\",\"amount\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("API-15b: POST transfer olmayan hedef → 404")
    void api15b_transfer_404() throws Exception {
        transfer(hesapAc("1000"), "9999999999", "10").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("API-15c: POST transfer hedef FROZEN → 409")
    void api15c_transfer_409() throws Exception {
        String a = hesapAc("1000");
        String b = hesapAc("0");
        mockMvc.perform(post("/api/accounts/" + b + "/freeze"));

        transfer(a, b, "10").andExpect(status().isConflict());
    }

    @Test
    @DisplayName("API-15d: POST transfer yetersiz bakiye → 422")
    void api15d_transfer_422() throws Exception {
        transfer(hesapAc("10"), hesapAc("0"), "10.01").andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("API-20/21: GET /api/transactions/{no} → 200, olmayan → 404")
    void api20_21_gecmisAlternatifYol() throws Exception {
        String a = hesapAc("100");
        String b = hesapAc("0");
        transfer(a, b, "40");

        mockMvc.perform(get("/api/transactions/" + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].balanceAfter").value(40));
        mockMvc.perform(get("/api/transactions/9999999999")).andExpect(status().isNotFound());
    }
}
