-- Users 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    provider VARCHAR(20),
    provider_id VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Books 테이블
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    book_type VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_book_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Accounts 테이블
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    book_type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Transactions 테이블
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    memo VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_transaction_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE INDEX idx_transaction_book_date ON transactions(book_id, date, is_active);
CREATE INDEX idx_transaction_book_type ON transactions(book_id, type, is_active);

-- Journal Entries 테이블
CREATE TABLE journal_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_journal_entry_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE INDEX idx_journal_entry_transaction_id ON journal_entries(transaction_id);

-- Transaction Details 테이블
CREATE TABLE transaction_details (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    detail_type VARCHAR(10) NOT NULL,
    debit_amount DECIMAL(15, 2) NOT NULL,
    credit_amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_transaction_detail_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id),
    CONSTRAINT fk_transaction_detail_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_transaction_detail_journal_entry_id ON transaction_details(journal_entry_id);
CREATE INDEX idx_detail_account_id ON transaction_details(account_id);