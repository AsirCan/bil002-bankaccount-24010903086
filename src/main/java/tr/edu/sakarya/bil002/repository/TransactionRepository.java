package tr.edu.sakarya.bil002.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import tr.edu.sakarya.bil002.model.Transaction;

/**
 * İşlem kayıtlarını bellekte tutan basit depo. Kayıtlar eklenme sırasıyla durur,
 * sıralama (R-9.3) servis katmanında yapılır.
 */
@Repository
public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong();

    public Transaction save(Transaction transaction) {
        transaction.setId(idSequence.incrementAndGet());
        transactions.add(transaction);
        return transaction;
    }

    public List<Transaction> findByAccountNumber(String accountNumber) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getAccountNumber().equals(accountNumber)) {
                result.add(transaction);
            }
        }
        return result;
    }
}
