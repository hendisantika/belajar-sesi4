-- Header jurnal
CREATE TABLE journal_entry
(
    id         BIGSERIAL PRIMARY KEY,
    number     VARCHAR(30) NOT NULL,
    date       DATE        NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_journal_entry_number UNIQUE (number),
    CONSTRAINT ck_journal_entry_status CHECK (status IN ('DRAFT', 'APPROVED', 'POSTED'))
);

-- Detail jurnal: FK ke header & master akun
CREATE TABLE journal_line
(
    id         BIGSERIAL PRIMARY KEY,
    entry_id   BIGINT         NOT NULL REFERENCES journal_entry (id) ON DELETE CASCADE,
    account_id BIGINT         NOT NULL REFERENCES account (id),
    debit      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    credit     NUMERIC(19, 2) NOT NULL DEFAULT 0,
    CONSTRAINT ck_journal_line_non_negative CHECK (debit >= 0 AND credit >= 0),
    CONSTRAINT ck_journal_line_one_side CHECK ((debit > 0 AND credit = 0) OR (debit = 0 AND credit > 0))
);

-- Index dasar: FK tidak otomatis ter-index di PostgreSQL
CREATE INDEX idx_journal_line_entry ON journal_line (entry_id);
CREATE INDEX idx_journal_line_account ON journal_line (account_id);
CREATE INDEX idx_journal_entry_date ON journal_entry (date);
