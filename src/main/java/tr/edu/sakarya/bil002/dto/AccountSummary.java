package tr.edu.sakarya.bil002.dto;

import java.math.BigDecimal;

/** Hesap özeti yanıtı (TDD özelliği, docs/tdd/OZELLIK.md). */
public record AccountSummary(String accountNumber, BigDecimal totalDeposited, BigDecimal totalWithdrawn,
                             int transactionCount) {
}
