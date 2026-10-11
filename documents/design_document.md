# Banking Transaction Processor
## Problem Statement

The Banking Transaction Processor is an API application that manages bank accounts 
and processes financial operations.

The service must:

- Create and maintain accounts with balances.
- Deposit money
- Withdraw money
- Transfer money between two accounts.
- Reject invalid amounts and operations
- Maintain ledger 
- Return current balance.
- Return transaction history.

## Exposed REST APIs

- `POST /api/v1/accounts` - Create an account.
- `GET /api/v1/accounts/{accountId}/balance` - Get account balance.
- `GET /api/v1/accounts/{accountId}/transactions` - Get transaction history.
- `POST /api/v1/accounts/{accountId}/deposits` - Deposit money
- `POST /api/v1/accounts/{accountId}/withdrawals` - Withdraw money
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