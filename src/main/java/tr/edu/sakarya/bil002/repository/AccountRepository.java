package tr.edu.sakarya.bil002.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import tr.edu.sakarya.bil002.model.Account;

/**
 * Hesapları bellekte tutan basit depo (veritabanı yok).
 * Anahtar: hesap numarası. Uygulama kapanınca veriler silinir (canlı servisteki gibi).
 * Eşzamanlılık: servisler bütün para işlemlerini bu nesne üzerinde kilitler,
 * o yüzden buradaki metotlar ayrıca kilit tutmaz.
 */
@Repository
public class AccountRepository {

    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public Account save(Account account) {
        if (account.getId() == null) {
            account.setId(idSequence.incrementAndGet());
        }
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    public boolean existsByAccountNumber(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }
}
