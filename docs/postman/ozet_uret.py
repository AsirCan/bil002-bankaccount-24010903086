"""newman JSON çıktısından API-xx özet tablosu (Markdown) üretir. Token ve URL YAZILMAZ.
Kullanım: python ozet_uret.py <newman.json> <cikti.md> <başlık>"""
import json
import sys

kaynak, hedef, baslik = sys.argv[1], sys.argv[2], sys.argv[3]
veri = json.load(open(kaynak, encoding="utf-8"))
satirlar = [f"# {baslik}", "", "| Test ID | İstek | Metot | Beklenen | Gerçekleşen kod | Doğrulama | Sonuç |",
            "|---|---|---|---|---|---|---|"]
toplam = basarisiz = 0
for ex in veri["run"]["executions"]:
    ad = ex["item"]["name"]
    test_id, _, aciklama = ad.partition(": ")
    metot = ex["request"]["method"]
    gercek = ex.get("response", {}).get("code", "-")
    assertler = ex.get("assertions", [])
    kalan = [a for a in assertler if a.get("error")]
    beklenen = next((a["assertion"].replace("HTTP ", "") for a in assertler if a["assertion"].startswith("HTTP ")), "-")
    toplam += 1
    basarisiz += 1 if kalan else 0
    dogrulama = f"{len(assertler) - len(kalan)}/{len(assertler)}"
    sonuc = "✘ " + "; ".join(a["assertion"] for a in kalan) if kalan else "✔"
    satirlar.append(f"| {test_id} | {aciklama} | {metot} | {beklenen} | {gercek} | {dogrulama} | {sonuc} |")
satirlar += ["", f"**Toplam:** {toplam} istek · {toplam - basarisiz} geçti · {basarisiz} kaldı"]
open(hedef, "w", encoding="utf-8").write("\n".join(satirlar) + "\n")
print(f"{toplam} istek, {basarisiz} kalan -> {hedef}")
