package tr.edu.sakarya.bil002.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tr.edu.sakarya.bil002.dto.AmountRequest;
import tr.edu.sakarya.bil002.dto.CreateAccountRequest;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.service.AccountService;
import tr.edu.sakarya.bil002.service.TransactionService;

/**
 * Hesap uç noktaları (API Referansı). Controller'da iş kuralı YOK:
 * sadece HTTP isteğini servise çevirir, sonucu JSON olarak döndürür.
 * Hatalar GlobalExceptionHandler'da HTTP koduna çevrilir.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)                                   // 201 Created
    public Account create(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request.ownerName(), request.accountType(), request.initialBalance());
    }

    @GetMapping
    public List<Account> list() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountNumber}")
    public Account get(@PathVariable String accountNumber) {
        return accountService.getAccount(accountNumber);
    }

    @PostMapping("/{accountNumber}/deposit")
    public Account deposit(@PathVariable String accountNumber, @RequestBody AmountRequest request) {
        return accountService.deposit(accountNumber, request.amount());
    }

    @PostMapping("/{accountNumber}/withdraw")
    public Account withdraw(@PathVariable String accountNumber, @RequestBody AmountRequest request) {
        return accountService.withdraw(accountNumber, request.amount());
    }

    @PostMapping("/{accountNumber}/freeze")
    public Account freeze(@PathVariable String accountNumber) {
        return accountService.freeze(accountNumber);
    }

    @PostMapping("/{accountNumber}/unfreeze")
    public Account unfreeze(@PathVariable String accountNumber) {
        return accountService.unfreeze(accountNumber);
    }

    @PostMapping("/{accountNumber}/close")
    public Account close(@PathVariable String accountNumber) {
        return accountService.close(accountNumber);
    }

    @PostMapping("/{accountNumber}/interest")
    public Account interest(@PathVariable String accountNumber) {
        return accountService.applyInterest(accountNumber);
    }

    @GetMapping("/{accountNumber}/transactions")
    public List<Transaction> transactions(@PathVariable String accountNumber) {
        return transactionService.getHistory(accountNumber);
    }
}
