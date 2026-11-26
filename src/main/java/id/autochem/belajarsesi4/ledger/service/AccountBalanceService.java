package id.autochem.belajarsesi4.ledger.service;

import id.autochem.belajarsesi4.common.exception.NotFoundException;
import id.autochem.belajarsesi4.ledger.model.AccountBalance;
import id.autochem.belajarsesi4.ledger.repository.AccountBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.03
 *
 * Demo terarah optimistic vs pessimistic locking.
 */
@Service
@RequiredArgsConstructor
public class AccountBalanceService {

    private final AccountBalanceRepository repo;

    /**
     * Optimistic (@Version): cocok bila konflik jarang. Gagal -> retry atau beri tahu user (HTTP 409).
     */
    @Transactional
    public AccountBalance adjustOptimistic(Long accountId, BigDecimal delta) {
        AccountBalance b = repo.findById(accountId)
                .orElseThrow(() -> NotFoundException.of("Saldo akun", accountId));
        b.apply(delta);           // dirty checking: UPDATE ... WHERE version = ?
        return b;
    }

    /**
     * Pessimistic (FOR UPDATE): untuk saldo/penomoran yang sering diperebutkan.
     * Lock ditahan sampai commit, jadi jaga transaksi tetap singkat.
     */
    @Transactional
    public AccountBalance adjustPessimistic(Long accountId, BigDecimal delta) {
        AccountBalance b = repo.lockById(accountId)
                .orElseThrow(() -> NotFoundException.of("Saldo akun", accountId));
        b.apply(delta);
        return b;
    }

    @Transactional(readOnly = true)
    public AccountBalance get(Long accountId) {
        return repo.findById(accountId)
                .orElseThrow(() -> NotFoundException.of("Saldo akun", accountId));
    }
}
