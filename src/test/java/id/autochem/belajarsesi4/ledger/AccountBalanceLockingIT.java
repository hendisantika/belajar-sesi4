package id.autochem.belajarsesi4.ledger;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import id.autochem.belajarsesi4.account.model.Account;
import id.autochem.belajarsesi4.account.model.AccountType;
import id.autochem.belajarsesi4.account.repository.AccountRepository;
import id.autochem.belajarsesi4.ledger.model.AccountBalance;
import id.autochem.belajarsesi4.ledger.repository.AccountBalanceRepository;
import id.autochem.belajarsesi4.ledger.service.AccountBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.04
 *
 * Simulasi konflik: optimistic (@Version) vs pessimistic (SELECT ... FOR UPDATE).
 */
class AccountBalanceLockingIT extends AbstractIntegrationTest {

    @Autowired
    AccountRepository accountRepo;

    @Autowired
    AccountBalanceRepository balanceRepo;

    @Autowired
    AccountBalanceService balanceService;

    Long accountId;

    @BeforeEach
    void openBalance() {
        String code = String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000L, 99_999_999L));
        accountId = accountRepo.save(Account.of(code, "Akun Uji Locking", AccountType.ASSET)).getId();
        balanceRepo.save(AccountBalance.open(accountId));
    }

    @Test
    void optimistic_lock_user_kedua_gagal_saat_menyimpan_data_basi() {
        // Dua user membaca versi yang sama (entity detached)
        AccountBalance userA = balanceRepo.findById(accountId).orElseThrow();
        AccountBalance userB = balanceRepo.findById(accountId).orElseThrow();
        assertThat(userA.getVersion()).isEqualTo(userB.getVersion());

        userA.apply(new BigDecimal("100.00"));
        balanceRepo.save(userA);                                   // version 0 -> 1

        userB.apply(new BigDecimal("50.00"));
        assertThatThrownBy(() -> balanceRepo.save(userB))          // WHERE version = 0 -> 0 baris
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        // Tidak ada lost update: perubahan userB tidak menimpa userA
        assertThat(balanceService.get(accountId).getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void pessimistic_lock_update_bersamaan_tidak_ada_lost_update() throws Exception {
        int threads = 10;
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Callable<AccountBalance>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> balanceService.adjustPessimistic(accountId, new BigDecimal("10.00")));
            }
            for (Future<AccountBalance> f : pool.invokeAll(tasks)) {
                f.get();                                             // gagal bila ada exception
            }
        }

        AccountBalance result = balanceService.get(accountId);
        assertThat(result.getBalance()).isEqualByComparingTo("100.00");
        assertThat(result.getVersion()).isEqualTo(threads);
    }

    @Test
    void optimistic_lock_update_bersamaan_sebagian_gagal_tanpa_lost_update() throws Exception {
        int threads = 10;
        int success = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Callable<AccountBalance>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tasks.add(() -> balanceService.adjustOptimistic(accountId, new BigDecimal("10.00")));
            }
            for (Future<AccountBalance> f : pool.invokeAll(tasks)) {
                try {
                    f.get();
                    success++;
                } catch (java.util.concurrent.ExecutionException e) {
                    assertThat(e.getCause()).isInstanceOf(ObjectOptimisticLockingFailureException.class);
                }
            }
        }

        // Saldo akhir selalu konsisten dengan jumlah transaksi yang berhasil
        AccountBalance result = balanceService.get(accountId);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("10.00").multiply(BigDecimal.valueOf(success)));
        assertThat(result.getVersion()).isEqualTo(success);
    }
}
