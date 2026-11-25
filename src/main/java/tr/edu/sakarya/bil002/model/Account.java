package tr.edu.sakarya.bil002.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Hesap (Veri modeli 2.1). Para tutarları {@link BigDecimal}: double'daki
 * yuvarlama hatalarından (0.1 + 0.2 = 0.30000000000000004) kaçınmak için.
 */
public class Account {

    private Long id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance = BigDecimal.ZERO;
    private AccountStatus status = AccountStatus.ACTIVE;
    private AccountType accountType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
