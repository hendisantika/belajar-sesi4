-- Data awal COA untuk latihan jurnal
INSERT INTO account (code, name, type, active)
VALUES ('1101', 'Kas', 'ASSET', TRUE),
       ('1102', 'Bank', 'ASSET', TRUE),
       ('1201', 'Piutang Usaha', 'ASSET', TRUE),
       ('2101', 'Utang Usaha', 'LIABILITY', TRUE),
       ('3101', 'Modal Pemilik', 'EQUITY', TRUE),
       ('4101', 'Pendapatan Jasa', 'REVENUE', TRUE),
       ('5101', 'Beban Gaji', 'EXPENSE', TRUE),
       ('5102', 'Beban Sewa', 'EXPENSE', TRUE),
       ('1999', 'Kas Kecil (Nonaktif)', 'ASSET', FALSE);
