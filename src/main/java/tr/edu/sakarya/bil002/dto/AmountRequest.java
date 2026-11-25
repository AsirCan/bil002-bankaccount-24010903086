package tr.edu.sakarya.bil002.dto;

import java.math.BigDecimal;

/** deposit / withdraw gövdesi: { "amount": 500.0 }. */
public record AmountRequest(BigDecimal amount) {
}
