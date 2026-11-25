# Postman koleksiyonu · canlı servis

| Test ID | İstek | Metot | Beklenen | Gerçekleşen kod | Doğrulama | Sonuç |
|---|---|---|---|---|---|---|
| API-01 | Hesap aç (geçerli STANDARD) | POST | 201 | 201 | 2/2 | ✔ |
| API-02 | Hedef hesap aç (geçerli PREMIUM, bakiye yok) | POST | 201 | 201 | 2/2 | ✔ |
| API-03 | Hesap aç (geçersiz tür 'standard') → 400 | POST | 400 | 400 | 2/2 | ✔ |
| API-04 | Hesap aç (negatif başlangıç bakiyesi) → 400 | POST | 400 | 201 | 0/1 | ✘ HTTP 400 |
| API-05 | Tüm hesaplar → 200, dizi | GET | 200 | 200 | 2/2 | ✔ |
| API-06 | Hesap sorgula (var) → 200 | GET | 200 | 200 | 2/2 | ✔ |
| API-07 | Hesap sorgula (yok) → 404 | GET | 404 | 200 | 0/2 | ✘ HTTP 404; Hata gövdesi §7 biçiminde |
| API-08 | Para yatır 500 → 200, bakiye 1500 | POST | 200 | 200 | 2/2 | ✔ |
| API-09 | Para yatır (negatif) → 400 | POST | 400 | 400 | 2/2 | ✔ |
| API-10 | Para yatır (bozuk JSON) → 400 | POST | 400 | 400 | 1/1 | ✔ |
| API-11 | Para çek 200 → 200, bakiye 1300 | POST | 200 | 200 | 2/2 | ✔ |
| API-12 | Para çek (yetersiz bakiye) → 422 | POST | 422 | 422 | 2/2 | ✔ |
| API-13 | Faiz uygula → 200, STANDARD %3: 1300 → 1303.25 | POST | 200 | 200 | 1/2 | ✘ Faiz kuruşlu (R-8.3) |
| API-14 | Faiz uygula (olmayan hesap) → 404 | POST | 404 | 404 | 1/1 | ✔ |
| API-15 | Transfer 100 → 200 | POST | 200 | 200 | 2/2 | ✔ |
| API-16 | Transfer (aynı hesap) → 400 | POST | 400 | 200 | 0/1 | ✘ HTTP 400 |
| API-17 | Transfer (eksik alan) → 400 | POST | 400 | 400 | 1/1 | ✔ |
| API-18 | İşlem geçmişi → 200, en yeni önce | GET | 200 | 200 | 2/2 | ✔ |
| API-19 | İşlem geçmişi (olmayan hesap) → 404 | GET | 404 | 404 | 1/1 | ✔ |
| API-20 | İşlem geçmişi alternatif yol → 200 | GET | 200 | 200 | 2/2 | ✔ |
| API-21 | İşlem geçmişi alternatif yol (olmayan) → 404 | GET | 404 | 404 | 1/1 | ✔ |
| API-22 | Dondur → 200, FROZEN | POST | 200 | 200 | 2/2 | ✔ |
| API-23 | Tekrar dondur (zaten FROZEN) → 409 | POST | 409 | 200 | 0/1 | ✘ HTTP 409 |
| API-24 | Dondurmayı kaldır → 200, ACTIVE | POST | 200 | 200 | 2/2 | ✔ |
| API-25 | Dondurmayı kaldır (zaten ACTIVE) → 409 | POST | 409 | 409 | 1/1 | ✔ |
| API-26 | Kapat (bakiyesi var) → 409 | POST | 409 | 200 | 0/1 | ✘ HTTP 409 |
| API-27 | Kapat (bakiye 0) → 200, CLOSED | POST | 200 | 200 | 2/2 | ✔ |
| API-27 | Kapat (bakiye 0) → 200, CLOSED | POST | 200 | 200 | 2/2 | ✔ |
| API-28 | Kapat (olmayan hesap) → 404 | POST | 404 | 404 | 1/1 | ✔ |

**Toplam:** 29 istek · 23 geçti · 6 kaldı
