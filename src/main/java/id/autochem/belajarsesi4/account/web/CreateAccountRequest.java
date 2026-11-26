package id.autochem.belajarsesi4.account.web;

import id.autochem.belajarsesi4.account.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.00
 */
public record CreateAccountRequest(
        @NotBlank @Pattern(regexp = "\\d{4,20}", message = "kode akun harus 4-20 digit angka") String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull AccountType type,
        String parentCode) {
}
