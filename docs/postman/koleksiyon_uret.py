"""Postman koleksiyonunu (v2.1) üretir: docs/postman/bankaccount.postman_collection.json

Neden elle değil de betikle? 26 istek ve test bloğu tutarlı kalsın, Test ID'leri (API-xx) raporla eşleşsin.
Koleksiyon Postman'e "Import" ile alınabilir ve Collection Runner ile koşulabilir.
Adres {{baseUrl}} ortam değişkeninden gelir (yerel: http://localhost:8080, canlı: https://lab.mykucukkara.com/s/<TOKEN>).
Ortam dosyası (token içerdiği için) repoya KONMAZ.
Çalıştırma: python docs/postman/koleksiyon_uret.py
"""
import json
import os

ISTEKLER = []


def istek(test_id, ad, metot, yol, govde=None, testler=(), on_betik=None):
    """Bir Postman isteği ekler. testler: pm.test(...) satırları (JavaScript)."""
    olay = [{"listen": "test", "script": {"type": "text/javascript", "exec": list(testler)}}]
    if on_betik:
        olay.append({"listen": "prerequest", "script": {"type": "text/javascript", "exec": list(on_betik)}})
    req = {
        "method": metot,
        "header": [{"key": "Content-Type", "value": "application/json"},
                   {"key": "User-Agent", "value": "Mozilla/5.0 (BIL002 Postman)"}],
        "url": {"raw": "{{baseUrl}}" + yol, "host": ["{{baseUrl}}"],
                "path": [p for p in yol.split("/") if p]},
    }
    if govde is not None:
        req["body"] = {"mode": "raw", "raw": govde, "options": {"raw": {"language": "json"}}}
    ISTEKLER.append({"name": f"{test_id}: {ad}", "event": olay, "request": req})


def kod(beklenen):
    return f'pm.test("HTTP {beklenen}", () => pm.response.to.have.status({beklenen}));'


JSON_HATA = ('pm.test("Hata gövdesi §7 biçiminde", () => { const j = pm.response.json(); '
             'pm.expect(j).to.have.property("status"); pm.expect(j).to.have.property("message"); });')

# ---------- Hesap açma ----------
istek("API-01", "Hesap aç (geçerli STANDARD)", "POST", "/api/accounts",
      '{"ownerName":"Postman Kullanıcı","accountType":"STANDARD","initialBalance":1000}',
      [kod(201),
       'const j = pm.response.json();',
       'pm.test("ACTIVE ve 10 haneli numara", () => { pm.expect(j.status).to.eql("ACTIVE"); '
       'pm.expect(j.accountNumber).to.match(/^[1-9][0-9]{9}$/); });',
       'pm.environment.set("hesapNo", j.accountNumber);'])
istek("API-02", "Hedef hesap aç (geçerli PREMIUM, bakiye yok)", "POST", "/api/accounts",
      '{"ownerName":"Postman Hedef","accountType":"PREMIUM"}',
      [kod(201), 'const j = pm.response.json();',
       'pm.test("initialBalance yoksa bakiye 0 (R-1.3)", () => pm.expect(j.balance).to.eql(0));',
       'pm.environment.set("hedefNo", j.accountNumber);'])
istek("API-03", "Hesap aç (geçersiz tür 'standard') → 400", "POST", "/api/accounts",
      '{"ownerName":"Ali","accountType":"standard","initialBalance":10}', [kod(400), JSON_HATA])
istek("API-04", "Hesap aç (negatif başlangıç bakiyesi) → 400", "POST", "/api/accounts",
      '{"ownerName":"Ali","accountType":"STANDARD","initialBalance":-100}', [kod(400)])

# ---------- Listeleme / sorgulama ----------
istek("API-05", "Tüm hesaplar → 200, dizi", "GET", "/api/accounts", None,
      [kod(200), 'pm.test("Yanıt dizi", () => pm.expect(pm.response.json()).to.be.an("array"));'])
istek("API-06", "Hesap sorgula (var) → 200", "GET", "/api/accounts/{{hesapNo}}", None,
      [kod(200), 'pm.test("Aynı numara", () => pm.expect(pm.response.json().accountNumber).to.eql(pm.environment.get("hesapNo")));'])
istek("API-07", "Hesap sorgula (yok) → 404", "GET", "/api/accounts/9999999999", None, [kod(404), JSON_HATA])

# ---------- Yatırma / çekme ----------
istek("API-08", "Para yatır 500 → 200, bakiye 1500", "POST", "/api/accounts/{{hesapNo}}/deposit", '{"amount":500}',
      [kod(200), 'pm.test("Bakiye 1500 (R-2.5)", () => pm.expect(pm.response.json().balance).to.be.closeTo(1500, 0.001));'])
istek("API-09", "Para yatır (negatif) → 400", "POST", "/api/accounts/{{hesapNo}}/deposit", '{"amount":-5}',
      [kod(400), JSON_HATA])
istek("API-10", "Para yatır (bozuk JSON) → 400", "POST", "/api/accounts/{{hesapNo}}/deposit", '{"amount": ',
      [kod(400)])
istek("API-11", "Para çek 200 → 200, bakiye 1300", "POST", "/api/accounts/{{hesapNo}}/withdraw", '{"amount":200}',
      [kod(200), 'pm.test("Bakiye 1300 (R-3.6)", () => pm.expect(pm.response.json().balance).to.be.closeTo(1300, 0.001));'])
istek("API-12", "Para çek (yetersiz bakiye) → 422", "POST", "/api/accounts/{{hesapNo}}/withdraw", '{"amount":99999}',
      [kod(422), JSON_HATA])

# ---------- Faiz ----------
istek("API-13", "Faiz uygula → 200, STANDARD %3: 1300 → 1303.25", "POST", "/api/accounts/{{hesapNo}}/interest", None,
      [kod(200), 'pm.test("Faiz kuruşlu (R-8.3)", () => pm.expect(pm.response.json().balance).to.be.closeTo(1303.25, 0.001));'])
istek("API-14", "Faiz uygula (olmayan hesap) → 404", "POST", "/api/accounts/9999999999/interest", None, [kod(404)])

# ---------- Transfer ----------
istek("API-15", "Transfer 100 → 200", "POST", "/api/transactions/transfer",
      '{"fromAccount":"{{hesapNo}}","toAccount":"{{hedefNo}}","amount":100}',
      [kod(200), 'pm.test("Başarı mesajı", () => pm.expect(pm.response.json()).to.have.property("message"));'])
istek("API-16", "Transfer (aynı hesap) → 400", "POST", "/api/transactions/transfer",
      '{"fromAccount":"{{hesapNo}}","toAccount":"{{hesapNo}}","amount":10}', [kod(400)])
istek("API-17", "Transfer (eksik alan) → 400", "POST", "/api/transactions/transfer",
      '{"fromAccount":"{{hesapNo}}","amount":10}', [kod(400)])

# ---------- İşlem geçmişi ----------
istek("API-18", "İşlem geçmişi → 200, en yeni önce", "GET", "/api/accounts/{{hesapNo}}/transactions", None,
      [kod(200), 'const t = pm.response.json();',
       'pm.test("Dizi ve en yeni işlem TRANSFER (R-9.3)", () => { pm.expect(t).to.be.an("array"); '
       'pm.expect(t[0].type).to.eql("TRANSFER"); });'])
istek("API-19", "İşlem geçmişi (olmayan hesap) → 404", "GET", "/api/accounts/9999999999/transactions", None, [kod(404)])
istek("API-20", "İşlem geçmişi alternatif yol → 200", "GET", "/api/transactions/{{hedefNo}}", None,
      [kod(200), 'pm.test("Hedefte TRANSFER kaydı", () => pm.expect(pm.response.json()[0].type).to.eql("TRANSFER"));'])
istek("API-21", "İşlem geçmişi alternatif yol (olmayan) → 404", "GET", "/api/transactions/9999999999", None, [kod(404)])

# ---------- Durum değişiklikleri ----------
istek("API-22", "Dondur → 200, FROZEN", "POST", "/api/accounts/{{hesapNo}}/freeze", None,
      [kod(200), 'pm.test("FROZEN", () => pm.expect(pm.response.json().status).to.eql("FROZEN"));'])
istek("API-23", "Tekrar dondur (zaten FROZEN) → 409", "POST", "/api/accounts/{{hesapNo}}/freeze", None, [kod(409)])
istek("API-24", "Dondurmayı kaldır → 200, ACTIVE", "POST", "/api/accounts/{{hesapNo}}/unfreeze", None,
      [kod(200), 'pm.test("ACTIVE", () => pm.expect(pm.response.json().status).to.eql("ACTIVE"));'])
istek("API-25", "Dondurmayı kaldır (zaten ACTIVE) → 409", "POST", "/api/accounts/{{hesapNo}}/unfreeze", None, [kod(409)])
istek("API-26", "Kapat (bakiyesi var) → 409", "POST", "/api/accounts/{{hesapNo}}/close", None, [kod(409)])
istek("API-27", "Kapat (bakiye 0) → 200, CLOSED", "POST", "/api/accounts/{{hedefNo}}/close", None,
      [kod(200), 'pm.test("CLOSED", () => pm.expect(pm.response.json().status).to.eql("CLOSED"));'],
      on_betik=['// Hedef hesabın bakiyesi transferden sonra 100: önce sıfırlansın diye 100 çekilir',
                'pm.sendRequest({ url: pm.environment.get("baseUrl") + "/api/accounts/" + pm.environment.get("hedefNo") + "/withdraw",',
                '  method: "POST", header: { "Content-Type": "application/json", "User-Agent": "Mozilla/5.0" },',
                '  body: { mode: "raw", raw: JSON.stringify({ amount: 100 }) } });'])
istek("API-28", "Kapat (olmayan hesap) → 404", "POST", "/api/accounts/9999999999/close", None, [kod(404)])

koleksiyon = {
    "info": {
        "name": "BIL002 BankAccount API",
        "description": "BIL 002 dönem projesi · her endpoint için en az bir başarılı ve bir hatalı istek. "
                       "Adres {{baseUrl}} ortam değişkeninden gelir. Hesap numaraları istekler arasında "
                       "hesapNo / hedefNo ortam değişkenleriyle taşınır. Sıra önemlidir: Collection Runner ile baştan sona koşun.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "item": ISTEKLER,
}
hedef = os.path.join(os.path.dirname(os.path.abspath(__file__)), "bankaccount.postman_collection.json")
with open(hedef, "w", encoding="utf-8") as f:
    json.dump(koleksiyon, f, ensure_ascii=False, indent=2)
print(len(ISTEKLER), "istek yazıldı:", hedef)
