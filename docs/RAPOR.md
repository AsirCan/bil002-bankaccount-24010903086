# BIL 002 Dönem Projesi Test Raporu

| | |
|---|---|
| **Öğrenci Adı** | |
| **Öğrenci No** | |
| **Tarih** | |
| **Kısım 1 Repo URL (BankAccount)** | |
| **Kısım 2 Repo URL (Selenium + Cucumber)** | |

> Bu rapor BYS üzerinden teslim edilir (son tarih: 18 Aralık 2026 Cuma). Bölüm 1–10 Kısım 1'i, en sondaki
> Kısım 2 başlığı Selenium/Cucumber çalışmasını kapsar.

## Proje yapısı hakkında not

Bu projede hocanın şablon reposu verilmedi. Hatalı uygulama yalnızca canlı serviste (`lab.mykucukkara.com`)
çalışıyor. Bu yüzden:

- `src/main` altında **Gereksinimler belgesinden yazdığımız referans API** var. Beyaz kutu, kapsama, TDD ve
  MockMvc testleri bu kod üzerinde yapıldı.
- Kara kutu / BDD / API testleri gereksinimlerden tasarlandı. Aynı testler hem yerelde (referans API) hem
  canlı serviste koşuldu. Yerelde geçip canlıda kalan test, canlı servisin gereksinimden saptığının kanıtıdır
  ve `@Tag("hata")` ile işaretlendi.
- Gereksinim belgesi: [docs/GEREKSINIMLER.pdf](GEREKSINIMLER.pdf) (ödev PDF'inin 11–22. sayfaları, değiştirilmeden).

---

# KISIM 1 — BankAccount API Testleri

## 1. Statik Test Sonuçları

### 1.1 Gereksinim belgesinin gözden geçirilmesi (review)

Test tasarımından önce Gereksinimler belgesi, kod çalıştırılmadan **gözden geçirildi**. Bu bir statik testtir: belgedeki
belirsizlikler en ucuz aşamada (gereksinim, ×1) yakalanır. İnceleme türü: kontrol listesi destekli **gayri resmi inceleme**
(2 kişi, bulgular kayıt altına alındı). Kontrol listesi: *Her kural test edilebilir mi? Sınırlar dahil mi hariç mi açık mı?
İki kural aynı anda ihlal edilirse ne olur? Kayıt (Transaction) oluşur mu? Hangi HTTP kodu döner?*

**Kural:** Belge bir şeyi söylemiyorsa testimiz o konuda varsayım yapmaz. Sadece belgenin açıkça söylediğini
doğrular ("reddedilmeli, bakiye değişmemeli"). Belirsiz bir noktadaki davranış hata olarak raporlanmaz.

| # | Yer | Belirsizlik / gözlem | Önem | Testte nasıl ele alındı |
|---|---|---|---|---|
| 1 | §5, §7 | İki kural aynı anda ihlal edilirse (ör. FROZEN hesaba −5 TL yatırma: 400 mü 409 mu?) hangi kodun öncelikli olduğu belirtilmemiş | Orta | Bu kombinasyonlarda yalnızca "reddedildi (4xx), bakiye ve durum değişmedi" doğrulanır, kod doğrulanmaz |
| 2 | §6 | Reddedilen **transfer** ve reddedilen **faiz** için işlem kaydı oluşup oluşmayacağı tabloda yok. Tablo sadece yatırma/çekme redlerinde FAILED kayıt istiyor | Orta | Bu durumlarda kayıt sayısı doğrulanmaz, sadece G-4 (bakiye/durum) doğrulanır |
| 3 | G-1, G-2 | 0,01'den küçük kuruş içeren tutarın (ör. 10,005 TL) kabul mü, red mi, yuvarlama mı olacağı belirtilmemiş | Düşük | Test değerleri her zaman 2 ondalıklı seçilir |
| 4 | R-1.1 | Başında/sonunda boşluk olan adın ("  Ayşe ") kırpılıp kırpılmayacağı belirtilmemiş | Düşük | Sadece "kabul edilir" doğrulanır, saklanan ad doğrulanmaz |
| 5 | R-1.4, Bölüm 3 | initialBalance için üst sınır yok: STANDARD hesap 1.000.000 TL ile açılabilir (yatırma limiti açılışa uygulanmaz) | Bilgi | Belgeye uygun davranış: kabul edilmeli (garip görünse de hata değildir) |
| 6 | R-4.8, Bölüm 3 | Transfer için limit yok: STANDARD hesaba tek transferle 10.000 TL'den fazla para gelebilir (yatırma limiti transfere uygulanmaz) | Bilgi | Belgeye uygun davranış: kabul edilmeli, testi var (KT-10) |
| 7 | R-8.3, R-8.5 | Bakiye 0 iken faiz 0,00 olur (örnek tabloda var). 0,00 tutarlı INTEREST kaydının oluşması R-8.5'e göre beklenir | Bilgi | R-8.5'e göre kayıt beklenir |
| 8 | R-8.3 | "Yarım yukarı" yuvarlama ondalıklı sayı (Double) ile yapılırsa 0,005 gibi değerler 0,00'a düşebilir (kayan nokta temsili) | Risk | Yuvarlama sınırı için ayrı BVA testi (BVA-10) |
| 9 | R-9.3 | Aynı zaman damgalı iki işlemin sırası belirtilmemiş | Düşük | Sıralama testinde işlemler arasında zaman farkı bırakılır |
| 10 | G-6 | "Gün" sunucunun yerel saatine göre. Canlı sunucunun saat dilimi bilinmiyor, gece yarısına yakın koşulan limit testleri kararsız (flaky) olabilir | Risk | Günlük limit testleri gece yarısı civarında koşulmaz; gün değişimi beyaz kutu testiyle (zaman kontrollü) sınanır |
| 11 | §6 dipnot | FAILED kayıt yalnızca "var olan hesap + sayısal amount + iş kuralı reddi" için. Eksik/bozuk amount (400) için kayıt oluşmaz | Bilgi | EP-16'da kayıt oluşmadığı doğrulanır |

### 1.2 Statik analiz araçları (Checkstyle / SonarLint)

| # | Araç | Dosya:Satır | Kural / Bulgu | Önem | Yorum |
|---|---|---|---|---|---|
| 1 | | | | | |

## 2. Kara Kutu Test Vakaları

### 2.1 Denklik Sınıfı Tablosu

| Test ID | Giriş | Beklenen | Gerçekleşen | Sonuç |
|---|---|---|---|---|
| EP-01 | | | | ✔ / ✘ |

### 2.2 Sınır Değer Analizi Tablosu

| Test ID | Değişken | Sınır | Test Değeri | Beklenen | Gerçekleşen | Sonuç |
|---|---|---|---|---|---|---|
| BVA-01 | | | | | | ✔ / ✘ |

### 2.3 Karar Tablosu

| Koşul / Eylem | K1 | K2 | K3 | K4 | ... |
|---|---|---|---|---|---|
| Koşul: | | | | | |
| Eylem: | | | | | |
| Test ID | | | | | |
| Sonuç | | | | | |

### 2.4 Durum Geçiş Tablosu

| Test ID | Başlangıç Durumu | Olay | Beklenen Durum | Gerçekleşen Durum | Sonuç |
|---|---|---|---|---|---|
| ST-01 | | | | | ✔ / ✘ |

## 3. Beyaz Kutu Test Sonuçları

_(JaCoCo ekran görüntüsü ve açıklama)_

- **Branch Coverage:** %
- **Statement Coverage:** %

| Metot | Dal (koşul) | Test Metodu | Kapsandı mı? |
|---|---|---|---|
| | | | |

## 4. TDD Uygulaması

_(Hangi özellik için, nasıl uygulandı)_

- **Özellik:**
- **Kırmızı (Red):** Yazılan başarısız test ve hata çıktısı
- **Yeşil (Green):** Testi geçiren en küçük kod değişikliği
- **Yeniden düzenleme (Refactor):** Yapılan iyileştirme
- **İlgili commit'ler:**

## 5. BDD Senaryoları

_(Feature dosyalarının listesi ve açıklamaları)_

| Feature Dosyası | Senaryo Sayısı | Açıklama | Sonuç |
|---|---|---|---|
| | | | |

## 6. Entegrasyon ve API Testleri

_(Postman koleksiyonu özeti)_

| Test ID | Metot | Endpoint | Beklenen Kod | Gerçekleşen Kod | Sonuç |
|---|---|---|---|---|---|
| API-01 | | | | | ✔ / ✘ |

## 7. Performans Testi Sonuçları

_(JMeter raporu özeti, grafik)_

| Senaryo | Kullanıcı Sayısı | Ort. Yanıt Süresi (ms) | 90p (ms) | Throughput (istek/sn) | Hata % |
|---|---|---|---|---|---|
| | | | | | |

## 8. CI/CD Pipeline

_(GitHub Actions ekran görüntüsü)_

## 9. Bulunan Hatalar

| Hata No | Açıklama | Hangi Teknikle Bulundu | Etki Seviyesi |
|---|---|---|---|
| H-01 | Olmayan hesap sorgusu 404 yerine 200 ve boş gövde döndürüyor | API (duman testi) | Orta |

_Her hata için: ilgili gereksinim kodu (örn. R-3.4), hatayı yakalayan test metodu ve beklenen / gerçekleşen davranış._

### H-01: Olmayan hesap sorgusu 404 yerine 200 dönüyor

| Alan | İçerik |
|---|---|
| İhlal edilen gereksinim | R-10.2, §7, G-3 |
| Yeniden üretme | Canlı örnekte `GET /api/accounts/9999999999` (var olmayan hesap) |
| Beklenen | 404 Not Found + §7 biçiminde JSON hata gövdesi |
| Gerçekleşen | 200 OK, boş gövde. Aynı numarayla `deposit` ve `transactions` doğru şekilde 404 veriyor |
| Yakalayan test | `ApiDumanTesti.duman04_olmayanHesap404` (`@Tag("hata")`) |
| Teknik | API duman testi (canlı servis) |
| Etki | Orta: yanlış hata kodu |
| Sadece canlıda / yük altında mı? | Sadece canlıda (referans API'de geçiyor), yük gerektirmiyor |

## 10. Sonuç ve Değerlendirme

---

# KISIM 2 — Selenium + Cucumber (Web Otomasyonu)

### K2.1 Seçilen Uygulama

- **Site / uygulama:**
- **Neden seçildi:**
- **Test edilen akışlar (en az 2):**

### K2.2 Senaryo Tablosu

| Feature Dosyası | Senaryo Sayısı | Pozitif/Negatif | Scenario Outline? | Kısa Açıklama |
|---|---|---|---|---|
| | | | | |

### K2.3 Page Object Model Yapısı

| Page Sınıfı | Sorumluluğu (hangi sayfa/eleman) |
|---|---|
| | |

### K2.4 Bekleme (Wait) Stratejisi

_(Nerede WebDriverWait / ExpectedConditions kullanıldı?)_

### K2.5 Karşılaşılan Sorunlar ve Çözümler

_(Kırılgan locator, dinamik içerik, zamanlama vb.)_

### K2.6 Çalıştırma Sonucu

_(mvn test çıktısı veya Cucumber HTML raporu ekran görüntüsü)_
