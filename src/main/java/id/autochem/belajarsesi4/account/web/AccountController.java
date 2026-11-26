package id.autochem.belajarsesi4.account.web;

import id.autochem.belajarsesi4.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
        AccountResponse res = service.create(req);
        return ResponseEntity.created(URI.create("/accounts/" + res.id())).body(res);
    }

    @GetMapping
    public List<AccountResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/name")
    public AccountResponse rename(@PathVariable Long id, @Valid @RequestBody RenameAccountRequest req) {
        return service.rename(id, req.name());
    }

    @PatchMapping("/{id}/deactivate")
    public AccountResponse deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }
}
