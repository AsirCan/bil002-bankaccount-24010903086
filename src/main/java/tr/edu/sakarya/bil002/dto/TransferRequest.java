package tr.edu.sakarya.bil002.dto;

import java.math.BigDecimal;

/** POST /api/transactions/transfer gövdesi. */
public record TransferRequest(String fromAccount, String toAccount, BigDecimal amount) {
}
