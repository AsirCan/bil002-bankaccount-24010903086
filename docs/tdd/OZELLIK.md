# TDD özelliği: Hesap özeti (`GET /api/accounts/{accountNumber}/summary`)

Ödev 5.3'te önerilen ilk özellik. Mevcut gereksinimlerde yok, bu yüzden önce kendi mini gereksinimimizi yazdık
(testler bu belgeden türetildi, kod testlerden sonra yazıldı).

| Kod | Kural |
|---|---|
| S-1 | Hesap mevcutsa 200 OK ve şu alanlar döner: `accountNumber`, `totalDeposited`, `totalWithdrawn`, `transactionCount` |
| S-2 | `totalDeposited` = hesabın **SUCCESS** durumundaki DEPOSIT kayıtlarının tutar toplamı |
| S-3 | `totalWithdrawn` = hesabın **SUCCESS** durumundaki WITHDRAWAL kayıtlarının tutar toplamı |
| S-4 | FAILED kayıtlar toplamlara girmez; TRANSFER ve INTEREST kayıtları da bu iki toplama girmez |
| S-5 | `transactionCount` = hesabın bütün işlem kayıtlarının sayısı (SUCCESS + FAILED, her tip) |
| S-6 | Hiç işlemi olmayan hesapta toplamlar 0, sayı 0 |
| S-7 | Hesap mevcut değilse 404 Not Found (Gereksinimler §7 biçiminde) |
| S-8 | Hesap durumu (ACTIVE / FROZEN / CLOSED) özeti engellemez (R-9.4 ile tutarlı) |

## Döngü (commit geçmişinde)

| Adım | Commit mesajı | Ne yapıldı |
|---|---|---|
| 🔴 Kırmızı | `test(red): hesap özeti için başarısız test` | `HesapOzetiTesti` yazıldı, kod yok. Testler 404 aldı (adres yok) → kırmızı. Çıktı: `kirmizi-cikti.txt` |
| 🟢 Yeşil | `feat(green): hesap özeti endpoint'i` | Testi geçiren **en küçük** kod: hesaplama doğrudan controller metodunun içinde |
| 🔵 Yeniden düzenleme | `refactor: özet hesaplamasını TransactionService'e taşı` | Davranış aynı, testler yeşil: hesaplama servise taşındı, yanıt için `AccountSummary` kaydı (record) eklendi |
