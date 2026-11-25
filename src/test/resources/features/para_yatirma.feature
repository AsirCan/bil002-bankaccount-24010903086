# language: tr
Özellik: Para yatırma
  Bir müşteri olarak, hesabıma limitler dahilinde para yatırabilmek istiyorum.
  Gereksinimler: R-2.1 – R-2.7, Bölüm 3

  Geçmiş:
    Diyelim ki "Ayşe" adına 1000 TL bakiyeli bir STANDARD hesabı var

  Senaryo: Hesaba para yatırılır
    Eğer ki "Ayşe" hesabına 500 TL yatırılır
    O zaman işlem kabul edilir
    Ve "Ayşe" hesabının bakiyesi 1500 TL olur
    Ve "Ayşe" hesabının son işlemi başarılı bir DEPOSIT kaydıdır

  Senaryo taslağı: STANDARD hesapta tek seferlik yatırma limiti 10.000 TL'dir
    Eğer ki "Ayşe" hesabına <tutar> TL yatırılır
    O zaman işlem <sonuç>
    Ve "Ayşe" hesabının bakiyesi <yeni bakiye> TL olur

    Örnekler:
      | tutar    | sonuç                                                | yeni bakiye |
      | 9999,99  | kabul edilir                                         | 10999,99    |
      | 10000    | kabul edilir                                         | 11000       |
      | 10000,01 | "bakiye veya limit yetersiz" gerekçesiyle reddedilir | 1000        |

  # R-2.2: amount <= 0 (0 dahil) reddedilir. Rapor: H-05
  @hata
  Senaryo: Sıfır TL yatırılamaz
    Eğer ki "Ayşe" hesabına 0 TL yatırılır
    O zaman işlem "geçersiz girdi" gerekçesiyle reddedilir
    Ve "Ayşe" hesabının bakiyesi 1000 TL olur

  # R-2.7: Reddedilen yatırmada DEPOSIT/FAILED kaydı oluşturulur. Rapor: H-03
  @hata
  Senaryo: Reddedilen yatırma denemesi kayıt altına alınır
    Eğer ki "Ayşe" hesabına -50 TL yatırılır
    O zaman işlem "geçersiz girdi" gerekçesiyle reddedilir
    Ve "Ayşe" hesabının geçmişinde 1 adet başarısız DEPOSIT kaydı bulunur

  Senaryo: Dondurulmuş hesaba para yatırılamaz
    Ve "Ayşe" hesabı dondurulmuş
    Eğer ki "Ayşe" hesabına 100 TL yatırılır
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir
    Ve "Ayşe" hesabının bakiyesi 1000 TL olur
