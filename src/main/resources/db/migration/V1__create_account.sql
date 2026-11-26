-- Chart of Accounts (COA)
CREATE TABLE account
(
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(20)  NOT NULL,
    name       VARCHAR(150) NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    parent_id  BIGINT REFERENCES account (id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_account_code UNIQUE (code),
    CONSTRAINT ck_account_type CHECK (type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'))
);

CREATE INDEX idx_account_parent ON account (parent_id);
