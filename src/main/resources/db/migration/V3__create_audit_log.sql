-- Audit trail: ikut transaksi yang sama dengan perubahan datanya
CREATE TABLE audit_log
(
    id          BIGSERIAL PRIMARY KEY,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
