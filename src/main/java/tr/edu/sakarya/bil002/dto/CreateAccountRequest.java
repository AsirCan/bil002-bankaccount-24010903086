package tr.edu.sakarya.bil002.dto;

import java.math.BigDecimal;

/**
 * POST /api/accounts gövdesi. accountType bilerek String: "standard" gibi geçersiz
 * değerleri JSON çözümleyici değil servis reddetsin (R-1.2), hata mesajı net olsun.
 */
public record CreateAccountRequest(String ownerName, String accountType, BigDecimal initialBalance) {
}
