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

/**
 * TDD (Ödev 5.3): Hesap özeti özelliği. Bu testler KODDAN ÖNCE yazıldı (kırmızı adım).
 * Mini gereksinim: docs/tdd/OZELLIK.md (S-1 … S-8).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TDD · Hesap özeti (GET /api/accounts/{no}/summary)")
class HesapOzetiTesti {

    @Autowired
    private MockMvc mockMvc;

    private String hesapAc(String bakiye) throws Exception {
        String yanit = mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerName\":\"TDD\",\"accountType\":\"STANDARD\",\"initialBalance\":" + bakiye + "}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(yanit, "$.accountNumber");
    }

    private void islem(String no, String tur, String tutar) throws Exception {
        mockMvc.perform(post("/api/accounts/" + no + "/" + tur).contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + tutar + "}"));
    }

    @Test
    @DisplayName("TDD-01: Sadece başarılı yatırma ve çekimler toplanır, sayı bütün kayıtları içerir (S-1..S-5)")
    void tdd01_ozet_basariliIslemlerToplanir() throws Exception {
        String no = hesapAc("1000");
        islem(no, "deposit", "500");
        islem(no, "deposit", "250.50");
        islem(no, "withdraw", "300");
        islem(no, "deposit", "-5");                     // FAILED: toplama girmez, sayıya girer
        islem(no, "withdraw", "999999");                // FAILED: toplama girmez, sayıya girer

        mockMvc.perform(get("/api/accounts/" + no + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(no))
                .andExpect(jsonPath("$.totalDeposited").value(750.50))
                .andExpect(jsonPath("$.totalWithdrawn").value(300))
                .andExpect(jsonPath("$.transactionCount").value(5));
    }

    @Test
    @DisplayName("TDD-02: Transfer ve faiz kayıtları yatırma/çekme toplamına girmez (S-4)")
    void tdd02_ozet_transferVeFaizToplamaGirmez() throws Exception {
        String no = hesapAc("1000");
        String diger = hesapAc("0");
        mockMvc.perform(post("/api/transactions/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccount\":\"" + no + "\",\"toAccount\":\"" + diger + "\",\"amount\":100}"));
        mockMvc.perform(post("/api/accounts/" + no + "/interest"));

        mockMvc.perform(get("/api/accounts/" + no + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeposited").value(0))
                .andExpect(jsonPath("$.totalWithdrawn").value(0))
                .andExpect(jsonPath("$.transactionCount").value(2));
    }

    @Test
    @DisplayName("TDD-03: İşlemsiz hesapta toplamlar ve sayı 0 (S-6)")
    void tdd03_ozet_islemsizHesap() throws Exception {
        String no = hesapAc("10");

        mockMvc.perform(get("/api/accounts/" + no + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeposited").value(0))
                .andExpect(jsonPath("$.totalWithdrawn").value(0))
                .andExpect(jsonPath("$.transactionCount").value(0));
    }

    @Test
    @DisplayName("TDD-04: Olmayan hesap için 404 ve §7 hata gövdesi (S-7)")
    void tdd04_ozet_olmayanHesap404() throws Exception {
        mockMvc.perform(get("/api/accounts/9999999999/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Hesap bulunamadı: 9999999999"));
    }

    @Test
    @DisplayName("TDD-05: Kapalı (CLOSED) hesabın özeti de alınabilir (S-8)")
    void tdd05_ozet_kapaliHesap() throws Exception {
        String no = hesapAc("0");
        mockMvc.perform(post("/api/accounts/" + no + "/close"));

        mockMvc.perform(get("/api/accounts/" + no + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(0));
    }
}
