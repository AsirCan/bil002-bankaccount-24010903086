package tr.edu.sakarya.bil002.destek;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Testlerin API ile konuştuğu küçük HTTP istemcisi (Java'nın kendi HttpClient'ı).
 * Aynı testler iki farklı adrese gidebilir; adresi {@link Hedef} belirler:
 * yerel referans API ya da canlı servis. Test kodu hangisine gittiğini bilmez.
 */
public class ApiIstemcisi {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String temelAdres;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public ApiIstemcisi(String temelAdres) {
        this.temelAdres = temelAdres;
    }

    public Yanit get(String yol) {
        return gonder(istek(yol).GET().build());
    }

    public Yanit post(String yol) {
        return gonder(istek(yol).POST(HttpRequest.BodyPublishers.noBody()).build());
    }

    /** Gövde ham JSON metni olarak gönderilir; bozuk JSON testi de böyle yapılabilir. */
    public Yanit post(String yol, String jsonGovde) {
        return gonder(istek(yol)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonGovde, StandardCharsets.UTF_8))
                .build());
    }

    private HttpRequest.Builder istek(String yol) {
        return HttpRequest.newBuilder(URI.create(temelAdres + yol))
                .timeout(Duration.ofSeconds(30))
                // Canlı servis tanımadığı istemcileri 403 ile reddedebiliyor (Canlı Servis Rehberi)
                .header("User-Agent", "Mozilla/5.0 (BIL002 test istemcisi)")
                .header("Accept", "application/json");
    }

    private Yanit gonder(HttpRequest istek) {
        try {
            HttpResponse<String> cevap = http.send(istek, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Yanit(cevap.statusCode(), jsonaCevir(cevap.body()), cevap.body());
        } catch (IOException e) {
            throw new IllegalStateException("İstek gönderilemedi: " + istek.uri().getPath(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("İstek kesildi", e);
        }
    }

    /** Gövde JSON değilse (ör. vekil sunucunun 502 HTML sayfası) boş düğüm döner, ham metin yine saklanır. */
    private static JsonNode jsonaCevir(String metin) {
        if (metin == null || metin.isBlank()) {
            return JSON.nullNode();
        }
        try {
            return JSON.readTree(metin);
        } catch (IOException e) {
            return JSON.nullNode();
        }
    }

    /** HTTP yanıtı: durum kodu + JSON gövde (+ hata ayıklama için ham metin). */
    public record Yanit(int durum, JsonNode govde, String ham) {
    }
}
