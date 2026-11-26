-- Saldo per akun: data kritis yang rawan lost update
CREATE TABLE account_balance
(
    account_id BIGINT PRIMARY KEY REFERENCES account (id),
    balance    NUMERIC(19, 2) NOT NULL DEFAULT 0,
    version    BIGINT         NOT NULL DEFAULT 0
);

INSERT INTO account_balance (account_id)
SELECT id
FROM account;
