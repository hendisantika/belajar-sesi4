package id.autochem.belajarsesi4.journal.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.02
 */
public record CreateJournalRequest(
        @Schema(description = "Nomor jurnal unik", example = "JV-001") @NotBlank @Size(max = 30) String number,
        @Schema(description = "Tanggal jurnal", example = "2026-11-26") @NotNull LocalDate date,
        @Schema(description = "Minimal 2 baris; total debit harus sama dengan total kredit")
        @NotNull @Size(min = 2, message = "jurnal minimal 2 baris") List<@Valid @NotNull LineRequest> lines) {
}
