# language: tr
Özellik: Hesap açma
  Bir müşteri olarak, adım ve hesap türümle yeni bir banka hesabı açabilmek istiyorum.
  Gereksinimler: R-1.1 – R-1.7

  Senaryo: Geçerli bilgilerle hesap açılır
    Eğer ki "Ayşe" adına STANDARD türünde ve 1000 TL başlangıç bakiyeli hesap açılmak istenir
    O zaman işlem kabul edilir
    Ve "Ayşe" hesabının durumu ACTIVE olur
    Ve "Ayşe" hesabının bakiyesi 1000 TL olur

  Senaryo taslağı: Geçersiz hesap türüyle hesap açılamaz
    Eğer ki "Ali" adına <tür> türünde ve 100 TL başlangıç bakiyeli hesap açılmak istenir
    O zaman işlem "geçersiz girdi" gerekçesiyle reddedilir

    Örnekler:
      | tür      |
      | standard |
      | GOLD     |
      | Premium  |

  # R-1.4: initialBalance negatif ise reddedilir. Rapor: H-02
  @hata
  Senaryo: Negatif başlangıç bakiyesiyle hesap açılamaz
    Eğer ki "Ali" adına STANDARD türünde ve -0,01 TL başlangıç bakiyeli hesap açılmak istenir
    O zaman işlem "geçersiz girdi" gerekçesiyle reddedilir
