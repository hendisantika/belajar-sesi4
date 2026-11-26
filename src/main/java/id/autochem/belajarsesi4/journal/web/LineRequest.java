package id.autochem.belajarsesi4.journal.web;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.02
 *
 * Nominal selalu BigDecimal (bukan double) agar precision/scale tidak hilang.
 */
public record LineRequest(
        @NotBlank String accountCode,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal debit,
        @NotNull @Digits(integer = 17, fraction = 2) BigDecimal credit) {
}
