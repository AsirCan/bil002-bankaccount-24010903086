# language: tr
Özellik: Para çekme
  Bir müşteri olarak, bakiyem ve günlük limitim dahilinde para çekebilmek istiyorum.
  Gereksinimler: R-3.1 – R-3.8, Bölüm 3

  Senaryo: Hesaptan para çekilir
    Diyelim ki "Mehmet" adına 1000 TL bakiyeli bir STANDARD hesabı var
    Eğer ki "Mehmet" hesabından 300 TL çekilir
    O zaman işlem kabul edilir
    Ve "Mehmet" hesabının bakiyesi 700 TL olur
    Ve "Mehmet" hesabının son işlemi başarılı bir WITHDRAWAL kaydıdır

  # R-3.4: Bakiyenin tamamı çekilebilir. Rapor: H-06
  @hata
  Senaryo: Bakiyenin tamamı çekilebilir
    Diyelim ki "Mehmet" adına 1000 TL bakiyeli bir STANDARD hesabı var
    Eğer ki "Mehmet" hesabından 1000 TL çekilir
    O zaman işlem kabul edilir
    Ve "Mehmet" hesabının bakiyesi 0 TL olur

  Senaryo: Bakiyeden fazla para çekilemez
    Diyelim ki "Mehmet" adına 1000 TL bakiyeli bir STANDARD hesabı var
    Eğer ki "Mehmet" hesabından 1000,01 TL çekilir
    O zaman işlem "bakiye veya limit yetersiz" gerekçesiyle reddedilir
    Ve "Mehmet" hesabının bakiyesi 1000 TL olur

  Senaryo taslağı: Günlük çekim limiti gün içindeki çekimlerin toplamına uygulanır
    Diyelim ki "Mehmet" adına 30000 TL bakiyeli bir <tür> hesabı var
    Ve "Mehmet" hesabından bugün daha önce <önceki> TL çekilmiş
    Eğer ki "Mehmet" hesabından <tutar> TL çekilir
    O zaman işlem <sonuç>

    Örnekler:
      | tür      | önceki | tutar   | sonuç                                                |
      | STANDARD | 3000   | 2000    | kabul edilir                                         |
      | STANDARD | 3000   | 2000,01 | "bakiye veya limit yetersiz" gerekçesiyle reddedilir |
      | PREMIUM  | 15000  | 5000,01 | "bakiye veya limit yetersiz" gerekçesiyle reddedilir |

  # R-3.3: FROZEN hesaptan çekim reddedilir. Rapor: H-10
  @hata
  Senaryo: Dondurulmuş hesaptan para çekilemez
    Diyelim ki "Mehmet" adına 1000 TL bakiyeli bir STANDARD hesabı var
    Ve "Mehmet" hesabı dondurulmuş
    Eğer ki "Mehmet" hesabından 100 TL çekilir
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir
    Ve "Mehmet" hesabının bakiyesi 1000 TL olur
