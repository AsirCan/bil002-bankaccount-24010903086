"""Surefire XML raporlarından test koşum özeti (Markdown) üretir.
Kullanım: python docs/canli/surefire_ozet.py <surefire-reports klasörü> <çıktı.md> <başlık>
Her test: sınıf, görünen ad (Test ID), sonuç; kırmızılar için ilk hata satırı. Token/URL yazılmaz."""
import glob
import os
import re
import sys
import xml.etree.ElementTree as ET

klasor, hedef, baslik = sys.argv[1], sys.argv[2], sys.argv[3]
satirlar = [f"# {baslik}", "", "| Sınıf | Test | Sonuç | Kırmızıysa ilk mesaj |", "|---|---|---|---|"]
toplam = kalan = 0
for dosya in sorted(glob.glob(os.path.join(klasor, "TEST-*.xml"))):
    kok = ET.parse(dosya).getroot()
    sinif = kok.get("name").split(".")[-1]
    for tc in kok.iter("testcase"):
        toplam += 1
        hata = tc.find("failure") if tc.find("failure") is not None else tc.find("error")
        mesaj = ""
        if hata is not None:
            kalan += 1
            mesaj = (hata.get("message") or "").splitlines()[0] if hata.get("message") else ""
            mesaj = re.sub(r"/s/[0-9a-f]+", "/s/<TOKEN>", mesaj)          # güvenlik: token asla yazılmaz
            mesaj = re.sub(r"\{\"timestamp.*?\}", "{hata gövdesi}", mesaj)
            mesaj = mesaj.replace("|", "/")[:140]
        ad = tc.get("name").replace("|", "/")
        satirlar.append(f"| {sinif} | {ad} | {'✘' if hata is not None else '✔'} | {mesaj} |")
satirlar += ["", f"**Toplam:** {toplam} test · {toplam - kalan} geçti · {kalan} kaldı"]
open(hedef, "w", encoding="utf-8").write("\n".join(satirlar) + "\n")
print(toplam, "test,", kalan, "kalan ->", hedef)
