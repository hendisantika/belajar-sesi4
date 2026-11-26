package id.autochem.belajarsesi4.journal.web;

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
        @NotBlank @Size(max = 30) String number,
        @NotNull LocalDate date,
        @NotNull @Size(min = 2, message = "jurnal minimal 2 baris") List<@Valid @NotNull LineRequest> lines) {
}
