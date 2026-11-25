# PMD statik analiz çıktısı (src/main)

Kural setleri: bestpractices, errorprone, design · Komut: `mvnw pmd:pmd` · Toplam **47** bulgu

## Kurala göre

| Kural | Kategori | Adet |
|---|---|---|
| LawOfDemeter | Design | 31 |
| MissingSerialVersionUID | Error Prone | 6 |
| ExceptionAsFlowControl | Design | 3 |
| DataClass | Design | 2 |
| AvoidLiteralsInIfCondition | Error Prone | 2 |
| UseUtilityClass | Design | 1 |
| TooManyMethods | Design | 1 |
| CyclomaticComplexity | Design | 1 |

## Bütün bulgular

| Dosya | Satır | Kural | Öncelik | Mesaj |
|---|---|---|---|---|
| BankAccountApplication.java | 12 | UseUtilityClass | 3 | This utility class has a non-private constructor |
| exception/AccountNotFoundException.java | 7 | MissingSerialVersionUID | 3 | Classes implementing Serializable should set a serialVersionUID |
| exception/AccountStateException.java | 7 | MissingSerialVersionUID | 3 | Classes implementing Serializable should set a serialVersionUID |
| exception/BusinessException.java | 9 | MissingSerialVersionUID | 3 | Classes implementing Serializable should set a serialVersionUID |
| exception/InsufficientBalanceException.java | 7 | MissingSerialVersionUID | 3 | Classes implementing Serializable should set a serialVersionUID |
| exception/InvalidInputException.java | 7 | MissingSerialVersionUID | 3 | Classes implementing Serializable should set a serialVersionUID |
| exception/LimitExceededException.java | 7 | MissingSerialVersionUID | 3 | Classes implementing Serializable should set a serialVersionUID |
| model/Account.java | 10 | DataClass | 3 | The class 'Account' is suspected to be a Data Class (WOC=0.000%, NOPA=0, NOAM=16, WMC=16) |
| model/Transaction.java | 10 | DataClass | 3 | The class 'Transaction' is suspected to be a Data Class (WOC=0.000%, NOPA=0, NOAM=16, WMC=16) |
| service/AccountService.java | 29 | TooManyMethods | 3 | This class has too many methods, consider refactoring it. |
| service/AccountService.java | 90 | LawOfDemeter | 3 | Call to `getAccountType` on foreign value `account` (degree 1) |
| service/AccountService.java | 92 | LawOfDemeter | 3 | Call to `getAccountType` on foreign value `account` (degree 1) |
| service/AccountService.java | 92 | LawOfDemeter | 3 | Call to `getDepositLimit` on foreign value `account.getAccountType()` (degree 2) |
| service/AccountService.java | 94 | ExceptionAsFlowControl | 3 | Exception thrown at line 91 is caught in this block. |
| service/AccountService.java | 99 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 116 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 118 | LawOfDemeter | 3 | Call to `getAccountType` on foreign value `account` (degree 1) |
| service/AccountService.java | 123 | ExceptionAsFlowControl | 3 | Exception thrown at line 116 is caught in this block. |
| service/AccountService.java | 123 | ExceptionAsFlowControl | 3 | Exception thrown at line 121 is caught in this block. |
| service/AccountService.java | 128 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 140 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/AccountService.java | 142 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/AccountService.java | 152 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/AccountService.java | 154 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/AccountService.java | 164 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/AccountService.java | 167 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 168 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 179 | LawOfDemeter | 3 | Call to `getAccountType` on foreign value `account` (degree 1) |
| service/AccountService.java | 180 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 180 | LawOfDemeter | 3 | Call to `multiply` on foreign value `account.getBalance()` (degree 2) |
| service/AccountService.java | 182 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `account` (degree 1) |
| service/AccountService.java | 198 | AvoidLiteralsInIfCondition | 3 | Avoid using literals in if statements |
| service/AccountService.java | 201 | AvoidLiteralsInIfCondition | 3 | Avoid using literals in if statements |
| service/AccountService.java | 229 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/AccountService.java | 231 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/TransactionService.java | 65 | LawOfDemeter | 3 | Call to `getType` on foreign value `t` (degree 1) |
| service/TransactionService.java | 66 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `t` (degree 1) |
| service/TransactionService.java | 67 | LawOfDemeter | 3 | Call to `getTimestamp` on foreign value `t` (degree 1) |
| service/TransactionService.java | 67 | LawOfDemeter | 3 | Call to `toLocalDate` on foreign value `t.getTimestamp()` (degree 2) |
| service/TransactionService.java | 98 | LawOfDemeter | 3 | Call to `getType` on foreign value `t` (degree 1) |
| service/TransactionService.java | 98 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `t` (degree 1) |
| service/TransactionService.java | 106 | CyclomaticComplexity | 3 | The method 'transfer(String, String, BigDecimal)' has a cyclomatic complexity of 11. |
| service/TransactionService.java | 122 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `source` (degree 1) |
| service/TransactionService.java | 126 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `source` (degree 1) |
| service/TransactionService.java | 127 | LawOfDemeter | 3 | Call to `getBalance` on foreign value `target` (degree 1) |
| service/TransactionService.java | 144 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
| service/TransactionService.java | 146 | LawOfDemeter | 3 | Call to `getStatus` on foreign value `account` (degree 1) |
