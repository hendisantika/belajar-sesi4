package id.autochem.belajarsesi4.account.service;

import id.autochem.belajarsesi4.account.model.Account;
import id.autochem.belajarsesi4.account.repository.AccountRepository;
import id.autochem.belajarsesi4.account.web.AccountResponse;
import id.autochem.belajarsesi4.account.web.CreateAccountRequest;
import id.autochem.belajarsesi4.common.exception.BusinessRuleException;
import id.autochem.belajarsesi4.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repo;

    @Transactional
    public AccountResponse create(CreateAccountRequest req) {
        if (repo.existsByCode(req.code())) {
            throw new BusinessRuleException("Kode akun sudah dipakai: " + req.code());
        }
        Account acc = Account.of(req.code(), req.name(), req.type());   // TRANSIENT
        if (req.parentCode() != null && !req.parentCode().isBlank()) {
            acc.assignParent(findByCode(req.parentCode()));
        }
        return AccountResponse.from(repo.save(acc));                     // MANAGED -> INSERT
    }

    @Transactional
    public AccountResponse rename(Long id, String newName) {
        Account acc = findById(id);                                       // MANAGED
        acc.rename(newName);
        // Tanpa repo.save(): dirty checking membuat UPDATE saat commit
        return AccountResponse.from(acc);
    }

    @Transactional
    public AccountResponse deactivate(Long id) {
        Account acc = findById(id);
        acc.deactivate();                                                 // dirty checking
        return AccountResponse.from(acc);
    }

    /**
     * Map ke DTO di dalam transaksi (readOnly) agar akses relasi LAZY (parent) aman.
     * Tanpa @Transactional, entity sudah DETACHED dan acc.getParent().getCode() -> LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public AccountResponse get(Long id) {
        return AccountResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list() {
        return repo.findAllByOrderByCodeAsc().stream().map(AccountResponse::from).toList();
    }

    private Account findById(Long id) {
        // Parent ikut di-load: AccountResponse.from() membaca parent.code tanpa query tambahan
        return repo.findWithParentById(id).orElseThrow(() -> NotFoundException.of("Akun", id));
    }

    private Account findByCode(String code) {
        return repo.findByCode(code).orElseThrow(() -> NotFoundException.account(code));
    }
}
