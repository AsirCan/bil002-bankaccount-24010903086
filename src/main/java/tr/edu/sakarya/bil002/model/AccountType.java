package tr.edu.sakarya.bil002.model;

import java.math.BigDecimal;

/**
 * Hesap türü ve türe bağlı limitler (Gereksinimler Bölüm 3).
 * Limitler kapsayıcıdır (dahil): tam limit kabul, limit + 0,01 red.
 */
public enum AccountType {

    //        tek seferlik yatırma   günlük çekim           yıllık faiz
    STANDARD(new BigDecimal("10000"), new BigDecimal("5000"), new BigDecimal("0.03")),
    PREMIUM(new BigDecimal("50000"), new BigDecimal("20000"), new BigDecimal("0.05"));

    private final BigDecimal depositLimit;
    private final BigDecimal dailyWithdrawLimit;
    private final BigDecimal annualInterestRate;

    AccountType(BigDecimal depositLimit, BigDecimal dailyWithdrawLimit, BigDecimal annualInterestRate) {
        this.depositLimit = depositLimit;
        this.dailyWithdrawLimit = dailyWithdrawLimit;
        this.annualInterestRate = annualInterestRate;
    }

    public BigDecimal getDepositLimit() {
        return depositLimit;
    }

    public BigDecimal getDailyWithdrawLimit() {
        return dailyWithdrawLimit;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }
}
