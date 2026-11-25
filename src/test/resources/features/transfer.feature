# language: tr
Özellik: Hesaplar arası transfer
  Bir müşteri olarak, aktif hesaplar arasında para transfer edebilmek istiyorum.
  Gereksinimler: R-4.1 – R-4.8, G-5

  Geçmiş:
    Diyelim ki "Ayşe" adına 1000 TL bakiyeli bir STANDARD hesabı var
    Ve "Mehmet" adına 200 TL bakiyeli bir PREMIUM hesabı var

  Senaryo: İki aktif hesap arasında transfer yapılır
    Eğer ki "Ayşe" hesabından "Mehmet" hesabına 300 TL transfer edilir
    O zaman işlem kabul edilir
    Ve "Ayşe" hesabının bakiyesi 700 TL olur
    Ve "Mehmet" hesabının bakiyesi 500 TL olur
    Ve "Mehmet" hesabının son işlemi başarılı bir TRANSFER kaydıdır

  Senaryo: Yetersiz bakiyeyle transfer yapılamaz
    Eğer ki "Ayşe" hesabından "Mehmet" hesabına 1000,01 TL transfer edilir
    O zaman işlem "bakiye veya limit yetersiz" gerekçesiyle reddedilir
    Ve "Ayşe" hesabının bakiyesi 1000 TL olur
    Ve "Mehmet" hesabının bakiyesi 200 TL olur

  Senaryo: Olmayan bir hesaba transfer yapılamaz
    Eğer ki "Ayşe" hesabından "Hayalet" hesabına 100 TL transfer edilir
    O zaman işlem "hesap bulunamadı" gerekçesiyle reddedilir
    Ve "Ayşe" hesabının bakiyesi 1000 TL olur

  # R-4.4: Bir hesaptan aynı hesaba transfer reddedilir. Rapor: H-12
  @hata
  Senaryo: Bir hesaptan kendisine transfer yapılamaz
    Eğer ki "Ayşe" hesabından "Ayşe" hesabına 100 TL transfer edilir
    O zaman işlem "geçersiz girdi" gerekçesiyle reddedilir

  # R-4.3: Hedef hesap FROZEN ise transfer reddedilir. Rapor: H-11
  @hata
  Senaryo: Dondurulmuş hesaba transfer yapılamaz
    Ve "Mehmet" hesabı dondurulmuş
    Eğer ki "Ayşe" hesabından "Mehmet" hesabına 100 TL transfer edilir
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir
    Ve "Ayşe" hesabının bakiyesi 1000 TL olur
    Ve "Mehmet" hesabının bakiyesi 200 TL olur
