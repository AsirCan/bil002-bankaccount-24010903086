package tr.edu.sakarya.bil002.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import tr.edu.sakarya.bil002.exception.AccountNotFoundException;
import tr.edu.sakarya.bil002.exception.AccountStateException;
import tr.edu.sakarya.bil002.exception.BusinessException;
import tr.edu.sakarya.bil002.exception.InsufficientBalanceException;
import tr.edu.sakarya.bil002.exception.InvalidInputException;
import tr.edu.sakarya.bil002.exception.LimitExceededException;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.AccountStatus;
import tr.edu.sakarya.bil002.model.AccountType;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.repository.AccountRepository;

/**
 * Hesap işlemleri: açma, sorgulama, yatırma, çekme, durum değişiklikleri, faiz.
 * Her kuralın yanında gereksinim kodu (R-x.y) yazılıdır.
 * Kilit: para ve durum değiştiren her işlem {@code accountRepository} üzerinde sıraya girer.
 */
@Service
public class AccountService {

    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final Clock clock;

    public AccountService(AccountRepository accountRepository,
                          TransactionService transactionService,
                          Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    /** Hesap açma (R-1.1 – R-1.7). */
    public Account createAccount(String ownerName, String accountType, BigDecimal initialBalance) {
        if (ownerName == null || ownerName.isBlank()) {                           // R-1.1: null, "" ya da boşluk
            throw new InvalidInputException("Hesap sahibi adı boş olamaz");
        }
        AccountType type = parseAccountType(accountType);                         // R-1.2
        BigDecimal balance = initialBalance == null ? BigDecimal.ZERO : initialBalance; // R-1.3
        if (balance.compareTo(BigDecimal.ZERO) < 0) {                             // R-1.4: 0 kabul
            throw new InvalidInputException("Başlangıç bakiyesi negatif olamaz: " + balance);
        }
        synchronized (accountRepository) {
            Account account = new Account();
            account.setAccountNumber(generateAccountNumber());                    // R-1.5
            account.setOwnerName(ownerName);
            account.setAccountType(type);
            account.setBalance(balance);
            account.setStatus(AccountStatus.ACTIVE);                              // R-1.6
            LocalDateTime now = LocalDateTime.now(clock);
            account.setCreatedAt(now);
            account.setUpdatedAt(now);
            return accountRepository.save(account);                               // R-1.7: işlem kaydı YOK
        }
    }

    public List<Account> getAllAccounts() {
        synchronized (accountRepository) {
            return accountRepository.findAll();
        }
    }

    /** Hesap sorgulama (R-10.1, R-10.2). Durum fark etmez (§4.3). */
    public Account getAccount(String accountNumber) {
        synchronized (accountRepository) {
            return findAccount(accountNumber);
        }
    }

    /** Para yatırma (R-2.1 – R-2.7). */
    public Account deposit(String accountNumber, BigDecimal amount) {
        synchronized (accountRepository) {
            Account account = findAccount(accountNumber);                         // R-2.1: 404, kayıt yok (§6)
            requireAmountPresent(amount);                                         // §7 eksik alan: 400, kayıt yok
            try {
                requirePositive(amount);                                          // R-2.2
                requireActive(account);                                           // R-2.3
                if (amount.compareTo(account.getAccountType().getDepositLimit()) > 0) { // R-2.4: limit dahil
                    throw new LimitExceededException("Yatırma limiti aşıldı: "
                            + account.getAccountType().getDepositLimit());
                }
            } catch (BusinessException e) {
                transactionService.record(account, TransactionType.DEPOSIT, amount,
                        TransactionStatus.FAILED, "Para yatırma reddedildi: " + e.getMessage()); // R-2.7
                throw e;
            }
            account.setBalance(account.getBalance().add(amount));                 // R-2.5
            account.setUpdatedAt(LocalDateTime.now(clock));
            transactionService.record(account, TransactionType.DEPOSIT, amount,
                    TransactionStatus.SUCCESS, "Para yatırma");                   // R-2.6
            return account;
        }
    }

    /** Para çekme (R-3.1 – R-3.8). */
    public Account withdraw(String accountNumber, BigDecimal amount) {
        synchronized (accountRepository) {
            Account account = findAccount(accountNumber);                         // R-3.1
            requireAmountPresent(amount);
            try {
                requirePositive(amount);                                          // R-3.2
                requireActive(account);                                           // R-3.3
                if (amount.compareTo(account.getBalance()) > 0) {                 // R-3.4: tamamı çekilebilir
                    throw new InsufficientBalanceException("Yetersiz bakiye: " + account.getBalance());
                }
                BigDecimal limit = account.getAccountType().getDailyWithdrawLimit();
                BigDecimal todayTotal = transactionService.todaysSuccessfulWithdrawals(accountNumber);
                if (todayTotal.add(amount).compareTo(limit) > 0) {                // R-3.5, Bölüm 3: (bugün + yeni) <= limit
                    throw new LimitExceededException("Günlük çekim limiti aşıldı: " + limit);
                }
            } catch (BusinessException e) {
                transactionService.record(account, TransactionType.WITHDRAWAL, amount,
                        TransactionStatus.FAILED, "Para çekme reddedildi: " + e.getMessage()); // R-3.8
                throw e;
            }
            account.setBalance(account.getBalance().subtract(amount));            // R-3.6
            account.setUpdatedAt(LocalDateTime.now(clock));
            transactionService.record(account, TransactionType.WITHDRAWAL, amount,
                    TransactionStatus.SUCCESS, "Para çekme");                     // R-3.7
            return account;
        }
    }

    /** Dondurma (R-5.1, R-5.2). Kayıt oluşmaz (§6). */
    public Account freeze(String accountNumber) {
        synchronized (accountRepository) {
            Account account = findAccount(accountNumber);
            if (account.getStatus() != AccountStatus.ACTIVE) {                    // R-5.1
                throw new AccountStateException("Sadece ACTIVE hesap dondurulabilir: "
                        + accountNumber + " (" + account.getStatus() + ")");
            }
            return changeStatus(account, AccountStatus.FROZEN);                   // R-5.2: bakiye değişmez
        }
    }

    /** Dondurmayı kaldırma (R-6.1, R-6.2). */
    public Account unfreeze(String accountNumber) {
        synchronized (accountRepository) {
            Account account = findAccount(accountNumber);
            if (account.getStatus() != AccountStatus.FROZEN) {                    // R-6.1
                throw new AccountStateException("Sadece FROZEN hesabın dondurması kaldırılabilir: "
                        + accountNumber + " (" + account.getStatus() + ")");
            }
            return changeStatus(account, AccountStatus.ACTIVE);                   // R-6.2
        }
    }

    /** Kapatma (R-7.1 – R-7.4). */
    public Account close(String accountNumber) {
        synchronized (accountRepository) {
            Account account = findAccount(accountNumber);
            if (account.getStatus() == AccountStatus.CLOSED) {                    // R-7.1, R-7.4
                throw new AccountStateException("Hesap zaten kapalı: " + accountNumber);
            }
            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {           // R-7.2: tam 0 olmalı
                throw new AccountStateException("Bakiyesi olan hesap kapatılamaz: " + account.getBalance());
            }
            return changeStatus(account, AccountStatus.CLOSED);                   // R-7.3
        }
    }

    /** Aylık faiz (R-8.1 – R-8.5). */
    public Account applyInterest(String accountNumber) {
        synchronized (accountRepository) {
            Account account = findAccount(accountNumber);
            requireActive(account);                                               // R-8.1
            BigDecimal rate = account.getAccountType().getAnnualInterestRate();   // R-8.2
            BigDecimal interest = account.getBalance().multiply(rate)
                    .divide(MONTHS_IN_YEAR, 2, RoundingMode.HALF_UP);             // R-8.3: kuruşa, yarım yukarı
            account.setBalance(account.getBalance().add(interest));               // R-8.4
            account.setUpdatedAt(LocalDateTime.now(clock));
            transactionService.record(account, TransactionType.INTEREST, interest,
                    TransactionStatus.SUCCESS, "Aylık faiz");                     // R-8.5
            return account;
        }
    }

    // ---------- yardımcı metotlar ----------

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountNumber));
    }

    private AccountType parseAccountType(String accountType) {
        if ("STANDARD".equals(accountType)) {                                     // R-1.2: büyük harf, birebir
            return AccountType.STANDARD;
        }
        if ("PREMIUM".equals(accountType)) {
            return AccountType.PREMIUM;
        }
        throw new InvalidInputException("Geçersiz hesap türü: " + accountType);
    }

    /** 10 haneli, sadece rakam, ilk hane 0 değil, benzersiz (R-1.5). Kilit içinde çağrılır. */
    private String generateAccountNumber() {
        String number;
        do {
            number = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private void requireAmountPresent(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidInputException("Eksik alan: amount zorunludur");
        }
    }

    private void requirePositive(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Tutar sıfırdan büyük olmalıdır: " + amount);
        }
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStateException("Hesap aktif değil: "
                    + account.getAccountNumber() + " (" + account.getStatus() + ")");
        }
    }

    private Account changeStatus(Account account, AccountStatus newStatus) {
        account.setStatus(newStatus);
        account.setUpdatedAt(LocalDateTime.now(clock));
        return account;
    }
}
