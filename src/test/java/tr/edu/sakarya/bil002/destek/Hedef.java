package tr.edu.sakarya.bil002.destek;

/**
 * Testler hangi sisteme karşı koşacak?
 * <ul>
 *   <li>Varsayılan: <b>yerel</b>. Test sırasında başlatılan referans API (rastgele port).</li>
 *   <li><b>canli</b>: hocanın canlı servisi. Komut satırından seçilir:
 *       {@code mvnw test -Dhedef=canli} ve token ortam değişkeninden: {@code BIL002_TOKEN}.</li>
 * </ul>
 * GİZLİLİK: token hiçbir dosyaya yazılmaz, sadece çalışma anında okunur (repo public).
 */
public final class Hedef {

    private static final String CANLI_ADRES = "https://lab.mykucukkara.com/s/";

    private Hedef() {
    }

    public static boolean canliMi() {
        String hedef = System.getProperty("hedef", System.getenv("BIL002_HEDEF"));
        return "canli".equalsIgnoreCase(hedef);
    }

    /** Testlerin kullanacağı temel adres. Yerelde Spring'in açtığı port verilir. */
    public static String temelAdres(int yerelPort) {
        if (!canliMi()) {
            return "http://localhost:" + yerelPort;
        }
        String token = System.getProperty("token", System.getenv("BIL002_TOKEN"));
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Canlı hedef için BIL002_TOKEN ortam değişkeni gerekli");
        }
        return CANLI_ADRES + token.trim();
    }
}
