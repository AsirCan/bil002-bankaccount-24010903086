# language: tr
Özellik: Hesap durumu yönetimi
  Banka görevlisi olarak, hesapları dondurabilmek, çözebilmek ve kapatabilmek istiyorum.
  Gereksinimler: Bölüm 4, R-5.x, R-6.x, R-7.x

  Senaryo: Aktif hesap dondurulur ve bakiye korunur
    Diyelim ki "Zeynep" adına 500 TL bakiyeli bir STANDARD hesabı var
    Eğer ki "Zeynep" hesabı dondurulur
    O zaman işlem kabul edilir
    Ve "Zeynep" hesabının durumu FROZEN olur
    Ve "Zeynep" hesabının bakiyesi 500 TL olur

  Senaryo: Dondurulmuş hesabın dondurması kaldırılır
    Diyelim ki "Zeynep" adına 500 TL bakiyeli bir STANDARD hesabı var
    Ve "Zeynep" hesabı dondurulmuş
    Eğer ki "Zeynep" hesabının dondurulması kaldırılır
    O zaman işlem kabul edilir
    Ve "Zeynep" hesabının durumu ACTIVE olur

  # R-5.1: Zaten FROZEN olan hesap için dondurma reddedilir. Rapor: H-09
  @hata
  Senaryo: Zaten dondurulmuş hesap tekrar dondurulamaz
    Diyelim ki "Zeynep" adına 500 TL bakiyeli bir STANDARD hesabı var
    Ve "Zeynep" hesabı dondurulmuş
    Eğer ki "Zeynep" hesabı dondurulur
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir

  Senaryo: Bakiyesi sıfır olan hesap kapatılır ve bir daha açılamaz
    Diyelim ki "Zeynep" adına 0 TL bakiyeli bir STANDARD hesabı var
    Eğer ki "Zeynep" hesabı kapatılır
    O zaman işlem kabul edilir
    Ve "Zeynep" hesabının durumu CLOSED olur
    Eğer ki "Zeynep" hesabının dondurulması kaldırılır
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir
    Ve "Zeynep" hesabının durumu CLOSED olur

  # R-7.2: Bakiyesi olan hesap kapatılamaz (müşterinin parası kaybolmamalı). Rapor: H-08
  @hata
  Senaryo: Bakiyesi olan hesap kapatılamaz
    Diyelim ki "Zeynep" adına 250 TL bakiyeli bir STANDARD hesabı var
    Eğer ki "Zeynep" hesabı kapatılır
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir
    Ve "Zeynep" hesabının durumu ACTIVE olur
