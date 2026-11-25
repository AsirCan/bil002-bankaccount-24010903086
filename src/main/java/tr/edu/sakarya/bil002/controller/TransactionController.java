package tr.edu.sakarya.bil002.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.edu.sakarya.bil002.dto.TransferRequest;
import tr.edu.sakarya.bil002.dto.TransferResponse;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.service.TransactionService;

/** Transfer ve işlem geçmişi (alternatif yol) uç noktaları. */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        transactionService.transfer(request.fromAccount(), request.toAccount(), request.amount());
        return new TransferResponse("Transfer başarılı", request.fromAccount(), request.toAccount(), request.amount());
    }

    @GetMapping("/{accountNumber}")
    public List<Transaction> history(@PathVariable String accountNumber) {
        return transactionService.getHistory(accountNumber);
    }
}
