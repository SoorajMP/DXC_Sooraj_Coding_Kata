# Banking Transaction Processor

## Problem Statement

The Banking Transaction Processor is a REST-based service that manages bank accounts and processes financial operations against them.

The service must:

- Create and maintain accounts with unique identifiers and balances.
- Deposit money into an account.
- Withdraw money from an account.
- Transfer money between two accounts.
- Reject invalid amounts and operations that would overdraw an account.
- Maintain a timestamped ledger for every account.
- Return an account's current balance.
- Return an account's transaction history.

## Exposed REST APIs

- `POST /api/v1/accounts` - Create an account.
- `GET /api/v1/accounts/{accountId}/balance` - Get an account balance.
- `GET /api/v1/accounts/{accountId}/transactions` - Get an account's transaction history.
- `POST /api/v1/accounts/{accountId}/deposits` - Deposit money into an account.
- `POST /api/v1/accounts/{accountId}/withdrawals` - Withdraw money from an account.
- `POST /api/v1/transfers` - Transfer money between two accounts.

## Entity Classes

### Account

- `id: UUID`
- `balance: BigDecimal`
- `version: Long`
- `createdAt: Instant`

### BankingTransaction

- `id: UUID`
- `type: TransactionType`
- `amount: BigDecimal`
- `sourceAccount: Account`
- `destinationAccount: Account`
- `occurredAt: Instant`

### LedgerEntry

- `id: UUID`
- `account: Account`
- `transaction: BankingTransaction`
- `entryType: EntryType`
- `amount: BigDecimal`
- `balanceAfter: BigDecimal`
- `occurredAt: Instant`

## Repository Classes

- `AccountRepository`
- `BankingTransactionRepository`
- `LedgerEntryRepository`

## Controller Classes

- `AccountController` - Creates accounts and provides balance and transaction-history APIs.
- `TransactionController` - Provides deposit, withdrawal, and transfer APIs.

## Service Classes

- `AccountService` - Creates accounts and retrieves account information.
- `TransactionService` - Processes deposits, withdrawals, and transfers.
