# belajar-sesi4

Implementasi **Sesi 4: Journal Entry, Transactions & JPA Performance**. Isinya relasi COA–JournalEntry–JournalLine,
jurnal multi-baris, `@Transactional` dan rollback, optimistic/pessimistic locking, serta deteksi N+1.

Stack: Java 25, Spring Boot 4.2, Spring Data JPA (Hibernate 7), Flyway, PostgreSQL 17, Testcontainers.

## Menjalankan

```bash
# 1. PostgreSQL lokal
docker compose up -d

# 2. Aplikasi dengan profile local (log SQL + nilai parameter + statistik query)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 3. Semua test (unit + integration *IT, butuh Docker)
./mvnw verify
```

Koneksi DB dapat diubah lewat env `DB_URL`, `DB_USERNAME`, dan `DB_PASSWORD`.

Jika port 5432 atau 8080 sudah terpakai (misalnya ada PostgreSQL native atau SSH tunnel), jalankan di port lain:

```bash
POSTGRES_PORT=55432 docker compose up -d
DB_URL=jdbc:postgresql://localhost:55432/belajar_sesi4 SERVER_PORT=18081 \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Swagger UI

Setelah aplikasi berjalan:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Spesifikasi OpenAPI (JSON): http://localhost:8080/v3/api-docs

Endpoint `POST /journals` menyediakan tiga contoh request yang bisa dipilih di Swagger UI: **Valid** (201),
**Unbalanced** (422), dan **Rollback** (404, tanpa data parsial).

## Migrasi Flyway

| Versi | Isi                                                                   |
|-------|-----------------------------------------------------------------------|
| V1    | Tabel `account` (COA)                                                 |
| V2    | Seed COA (`1101` Kas, `4101` Pendapatan Jasa, `1999` akun nonaktif, …) |
| V3    | Tabel `audit_log`                                                     |
| V4    | `journal_entry` + `journal_line` (FK ke header & akun, index dasar)   |
| V5    | `account_balance` dengan kolom `version` untuk locking                |

## Endpoint

| Method | Path                                    | Keterangan                                    |
|--------|-----------------------------------------|-----------------------------------------------|
| POST   | `/accounts`                             | Buat akun COA                                 |
| GET    | `/accounts`, `/accounts/{id}`           | Daftar / detail akun                          |
| PATCH  | `/accounts/{id}/name`                   | Rename lewat dirty checking (tanpa `save()`)  |
| PATCH  | `/accounts/{id}/deactivate`             | Nonaktifkan akun                              |
| POST   | `/journals`                             | Buat jurnal multi-baris (satu transaksi)      |
| GET    | `/journals/{id}`                        | Detail jurnal (`@EntityGraph`)                |
| GET    | `/journals?from=YYYY-MM-DD&to=YYYY-MM-DD` | Daftar jurnal per tanggal (fetch join, 1 query) |

Semua error memakai format `ProblemDetail` (RFC 9457): `400` untuk validasi, `404` jika data tidak ditemukan,
`422` jika aturan bisnis dilanggar, dan `409` untuk konflik optimistic lock.

## Bukti deliverable

**Jurnal valid → `201 Created`**

```bash
curl -i -X POST localhost:8080/journals -H 'Content-Type: application/json' -d '{
  "number": "JV-001", "date": "2026-11-26",
  "lines": [
    {"accountCode": "1101", "debit": 500000, "credit": 0},
    {"accountCode": "3101", "debit": 0, "credit": 500000}
  ]}'
```

**Jurnal tidak seimbang → `422`**

```bash
curl -i -X POST localhost:8080/journals -H 'Content-Type: application/json' -d '{
  "number": "JV-002", "date": "2026-11-26",
  "lines": [
    {"accountCode": "5102", "debit": 300000, "credit": 0},
    {"accountCode": "1102", "debit": 0, "credit": 299999.99}
  ]}'
# "detail": "Jurnal tidak seimbang: total debit 300000.00 != total kredit 299999.99"
```

**Rollback: baris kedua gagal → `404`, tidak ada data parsial**

```bash
curl -i -X POST localhost:8080/journals -H 'Content-Type: application/json' -d '{
  "number": "JV-003", "date": "2026-11-26",
  "lines": [
    {"accountCode": "1101", "debit": 100000, "credit": 0},
    {"accountCode": "9999", "debit": 0, "credit": 100000}
  ]}'

psql -h localhost -U postgres belajar_sesi4 -c "select * from journal_entry where number = 'JV-003'"   # 0 rows
```

Aturan validasi jurnal:

- akun harus ada dan aktif
- nominal tidak boleh negatif
- tiap baris diisi debit **atau** kredit saja
- total debit harus sama dengan total kredit
- nomor jurnal unik

Semua nominal memakai `BigDecimal` dengan kolom `NUMERIC(19,2)`.

## Test yang membuktikan materi

| Test                          | Yang dibuktikan                                                                    |
|-------------------------------|------------------------------------------------------------------------------------|
| `JournalRollbackIT`           | Tidak ada jurnal parsial saat baris gagal, termasuk kegagalan **setelah** INSERT (audit gagal); unbalanced, akun nonaktif, dan nominal negatif ditolak |
| `JournalApiIT`                | Kontrak HTTP 201 / 400 / 404 / 422 dan tabel tetap kosong setelah gagal            |
| `JournalRepositoryIT`         | `findAll()` + `getLines()` = 1 + N query, sedangkan fetch join / `@EntityGraph` = **1 query** (diukur via Hibernate Statistics) |
| `AccountPersistenceContextIT` | Dirty checking tanpa `save()`, cache tingkat 1, `LazyInitializationException` pada entity detached |
| `AccountBalanceLockingIT`     | `@Version` menolak data basi (`ObjectOptimisticLockingFailureException`); `PESSIMISTIC_WRITE` menjaga 10 update bersamaan tanpa lost update |

## Demo N+1 di log SQL

Jalankan aplikasi dengan profile `local`, lalu panggil `GET /journals?from=...&to=...`. Log hanya menampilkan satu query:

```sql
select je1_0.id, je1_0.date, l1_0.id, l1_0.debit, a1_0.code ...
  from journal_entry je1_0
  join journal_line l1_0 on je1_0.id = l1_0.entry_id
  join account a1_0 on a1_0.id = l1_0.account_id
 where je1_0.date between ? and ?
```

Salin query tersebut ke `psql`, lalu jalankan `EXPLAIN ANALYZE` untuk memeriksa pemakaian index `idx_journal_entry_date`
dan `idx_journal_line_entry`.
