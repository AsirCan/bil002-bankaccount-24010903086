# language: tr
Özellik: Aylık faiz
  Bir müşteri olarak, aktif hesabıma hesap türüme göre aylık faiz uygulanmasını istiyorum.
  Gereksinimler: R-8.1 – R-8.5 (formül: bakiye × yıllık oran / 12, kuruşa yarım yukarı)

  Senaryo taslağı: Aylık faiz belgedeki örnek tabloya göre hesaplanır
    Diyelim ki "Can" adına <bakiye> TL bakiyeli bir <tür> hesabı var
    Eğer ki "Can" hesabına aylık faiz uygulanır
    O zaman işlem kabul edilir
    Ve "Can" hesabının bakiyesi <yeni bakiye> TL olur
    Ve "Can" hesabının son işlemi başarılı bir INTEREST kaydıdır

    Örnekler: Tam liralık sonuçlar
      | tür      | bakiye | yeni bakiye |
      | STANDARD | 12000  | 12030       |

    # R-8.3: Faiz kuruşa yuvarlanır (2 ondalık, yarım yukarı). Rapor: H-04
    @hata
    Örnekler: Kuruşlu sonuçlar
      | tür      | bakiye  | yeni bakiye |
      | STANDARD | 1000    | 1002,50     |
      | PREMIUM  | 1000    | 1004,17     |
      | PREMIUM  | 2500,50 | 2510,92     |

  Senaryo: Dondurulmuş hesaba faiz uygulanmaz
    Diyelim ki "Can" adına 1000 TL bakiyeli bir STANDARD hesabı var
    Ve "Can" hesabı dondurulmuş
    Eğer ki "Can" hesabına aylık faiz uygulanır
    O zaman işlem "hesap durumu uygun değil" gerekçesiyle reddedilir
    Ve "Can" hesabının bakiyesi 1000 TL olur
