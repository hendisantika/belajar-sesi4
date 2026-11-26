package id.autochem.belajarsesi4.account.web;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(example = "1103") @NotBlank @Pattern(regexp = "\\d{4,20}", message = "kode akun harus 4-20 digit angka") String code,
        @Schema(example = "Kas Cabang Bandung") @NotBlank @Size(max = 150) String name,
        @Schema(example = "ASSET") @NotNull AccountType type,
        @Schema(description = "Kode akun induk (opsional)", example = "1101") String parentCode) {
}
