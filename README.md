# BIL 002 · BankAccount API Test Projesi (Kısım 1)

Sakarya Uygulamalı Bilimler Üniversitesi · BIL 002 Yazılım Doğrulama ve Sınama dönem projesi.

Amaç: içine bilerek hata konmuş BankAccount API'sindeki hataları test teknikleriyle **bulmak, testle
kanıtlamak ve raporlamak**. Rapor: [docs/RAPOR.md](docs/RAPOR.md) · Gereksinimler: [docs/GEREKSINIMLER.pdf](docs/GEREKSINIMLER.pdf)

## Yapı

Şablon repo verilmediği için gereksinimlerden bir **referans API** yazdık (`src/main`). Test takımı aynı
testleri hem bu referans API'ye (yerel) hem canlı servise karşı koşar. Yerelde geçip canlıda kalan
test bir hatanın kanıtıdır ve `@Tag("hata")` ile işaretlenir.

```
src/test/java/tr/edu/sakarya/bil002/
├── blackbox/     EP, BVA, karar tablosu, durum geçiş testleri
├── whitebox/     dal kapsamı testleri (JaCoCo)
├── integration/  MockMvc ile API testleri
└── bdd/          Cucumber adım tanımları
src/test/resources/features/   Gherkin senaryoları
docs/             RAPOR.md, kapsama, Postman koleksiyonu, ekran görüntüleri
performans/       JMeter test planı ve sonuçları
```

## Komutlar

Windows'ta `mvnw.cmd`, Linux/macOS/Git Bash'te `./mvnw` kullanın.

```
./mvnw spring-boot:run                 # uygulamayı başlat → http://localhost:8080
./mvnw test                            # tüm testler + JaCoCo raporu (target/site/jacoco/index.html)
./mvnw test -DexcludedGroups=hata      # CI'daki zorunlu adım → yeşil olmalı
./mvnw test -Dgroups=hata              # sadece hata yakalayan testler → kırmızı olması beklenir
./mvnw checkstyle:check                # stil kontrolü
```
