package id.autochem.belajarsesi4.account.web;

import id.autochem.belajarsesi4.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

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
@Tag(name = "Account")
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @Operation(summary = "Buat akun COA", description = "Kode akun unik 4-20 digit; parentCode opsional")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Akun dibuat"),
            @ApiResponse(responseCode = "400", description = "Validasi gagal", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Parent tidak ditemukan", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Kode akun sudah dipakai", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
        AccountResponse res = service.create(req);
        return ResponseEntity.created(URI.create("/accounts/" + res.id())).body(res);
    }

    @Operation(summary = "Daftar akun COA", description = "Diurutkan berdasarkan kode")
    @GetMapping
    public List<AccountResponse> list() {
        return service.list();
    }

    @Operation(summary = "Detail akun")
    @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public AccountResponse get(@Parameter(description = "ID akun", example = "1") @PathVariable Long id) {
        return service.get(id);
    }

    @Operation(summary = "Rename akun", description = "Tanpa repo.save(): dirty checking membuat UPDATE saat commit")
    @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{id}/name")
    public AccountResponse rename(@PathVariable Long id, @Valid @RequestBody RenameAccountRequest req) {
        return service.rename(id, req.name());
    }

    @Operation(summary = "Nonaktifkan akun", description = "Akun nonaktif tidak bisa dipakai di jurnal baru")
    @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping("/{id}/deactivate")
    public AccountResponse deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }
}
