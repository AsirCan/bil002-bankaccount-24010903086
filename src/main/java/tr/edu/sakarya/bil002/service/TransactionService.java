package tr.edu.sakarya.bil002.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import tr.edu.sakarya.bil002.dto.AccountSummary;
import tr.edu.sakarya.bil002.exception.AccountNotFoundException;
import tr.edu.sakarya.bil002.exception.AccountStateException;
import tr.edu.sakarya.bil002.exception.InsufficientBalanceException;
import tr.edu.sakarya.bil002.exception.InvalidInputException;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.AccountStatus;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.repository.AccountRepository;
import tr.edu.sakarya.bil002.repository.TransactionRepository;

/**
 * İşlem kayıtları, işlem geçmişi, günlük çekim toplamı ve transfer.
 * Kilit: bütün işlemler {@code accountRepository} nesnesi üzerinde sıraya girer
 * (AccountService ile aynı kilit). Böylece aynı anda iki işlem aynı bakiyeyi bozamaz.
 */
@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    /** Bir işlem kaydı oluşturur. balanceAfter = hesabın o anki bakiyesi (§6). */
    public Transaction record(Account account, TransactionType type, BigDecimal amount,
                              TransactionStatus status, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(account.getAccountNumber());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setStatus(status);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now(clock));
        return transactionRepository.save(transaction);
    }

    /**
     * Bugün yapılmış BAŞARILI çekimlerin toplamı (Bölüm 3, G-6).
     * Transferler, FAILED denemeler ve önceki günler dahil edilmez.
     */
    public BigDecimal todaysSuccessfulWithdrawals(String accountNumber) {
        LocalDate today = LocalDate.now(clock);
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactionRepository.findByAccountNumber(accountNumber)) {
            if (t.getType() == TransactionType.WITHDRAWAL
                    && t.getStatus() == TransactionStatus.SUCCESS
                    && t.getTimestamp().toLocalDate().equals(today)) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    /** İşlem geçmişi (R-9.1 – R-9.4). */
    public List<Transaction> getHistory(String accountNumber) {
        synchronized (accountRepository) {
            findAccount(accountNumber);                                           // R-9.1: yoksa 404
            List<Transaction> history = transactionRepository.findByAccountNumber(accountNumber); // R-9.2
            history.sort(Comparator.comparing(Transaction::getTimestamp)
                    .thenComparing(Transaction::getId)
                    .reversed());                                                 // R-9.3: en yeni önce
            return history;                                                       // R-9.4: yoksa boş liste
        }
    }

    /** Hesap özeti (TDD özelliği, docs/tdd/OZELLIK.md S-1 … S-8). */
    public AccountSummary getSummary(String accountNumber) {
        List<Transaction> history = getHistory(accountNumber);                    // S-7: yoksa 404
        return new AccountSummary(accountNumber,
                successfulTotal(history, TransactionType.DEPOSIT),                // S-2, S-4
                successfulTotal(history, TransactionType.WITHDRAWAL),             // S-3, S-4
                history.size());                                                  // S-5
    }

    private BigDecimal successfulTotal(List<Transaction> history, TransactionType type) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : history) {
            if (t.getType() == type && t.getStatus() == TransactionStatus.SUCCESS) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    /** Hesaplar arası transfer (R-4.1 – R-4.8). Kayıt: iki hesapta da TRANSFER/SUCCESS. */
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        if (fromAccount == null || toAccount == null || amount == null) {
            throw new InvalidInputException("Eksik alan: fromAccount, toAccount ve amount zorunludur");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {                             // R-4.1
            throw new InvalidInputException("Tutar sıfırdan büyük olmalıdır: " + amount);
        }
        if (fromAccount.equals(toAccount)) {                                      // R-4.4
            throw new InvalidInputException("Aynı hesaba transfer yapılamaz: " + fromAccount);
        }
        synchronized (accountRepository) {
            Account source = findAccount(fromAccount);                            // R-4.2
            Account target = findAccount(toAccount);                              // R-4.2
            requireActive(source);                                                // R-4.3
            requireActive(target);                                                // R-4.3
            if (amount.compareTo(source.getBalance()) > 0) {                      // R-4.5: tamamı gönderilebilir
                throw new InsufficientBalanceException("Yetersiz bakiye: " + source.getBalance());
            }
            // G-5 atomiklik: bütün kontroller yukarıda bitti, değişiklik sadece burada ve birlikte yapılır
            LocalDateTime now = LocalDateTime.now(clock);
            source.setBalance(source.getBalance().subtract(amount));              // R-4.6
            target.setBalance(target.getBalance().add(amount));                   // R-4.6
            source.setUpdatedAt(now);
            target.setUpdatedAt(now);
            record(source, TransactionType.TRANSFER, amount, TransactionStatus.SUCCESS,
                    "Transfer gönderildi: " + toAccount);                         // R-4.7
            record(target, TransactionType.TRANSFER, amount, TransactionStatus.SUCCESS,
                    "Transfer alındı: " + fromAccount);                           // R-4.7
            // R-4.8: transfer WITHDRAWAL değil, günlük çekim limitine girmez
        }
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountNumber));
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStateException("Hesap aktif değil: "
                    + account.getAccountNumber() + " (" + account.getStatus() + ")");
        }
    }
}
