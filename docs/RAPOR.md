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

_(SonarQube / Checkstyle bulguları)_

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
| | | | |

_Her hata için: ilgili gereksinim kodu (örn. R-3.4), hatayı yakalayan test metodu ve beklenen / gerçekleşen davranış._

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
