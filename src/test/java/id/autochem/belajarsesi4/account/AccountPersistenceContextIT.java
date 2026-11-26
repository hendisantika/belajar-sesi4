package id.autochem.belajarsesi4.account;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import id.autochem.belajarsesi4.account.model.Account;
import id.autochem.belajarsesi4.account.model.AccountType;
import id.autochem.belajarsesi4.account.repository.AccountRepository;
import id.autochem.belajarsesi4.account.service.AccountService;
import id.autochem.belajarsesi4.account.web.AccountResponse;
import id.autochem.belajarsesi4.account.web.CreateAccountRequest;
import org.hibernate.LazyInitializationException;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

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
 * Persistence context & siklus hidup entity: dirty checking, cache tingkat 1, entity detached.
 */
class AccountPersistenceContextIT extends AbstractIntegrationTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository repo;

    @Autowired
    TransactionTemplate tx;

    @Test
    void rename_tanpa_save_tetap_tersimpan_lewat_dirty_checking() {
        AccountResponse created = accountService.create(
                new CreateAccountRequest(randomCode(), "Kas Cabang", AccountType.ASSET, "1101"));

        accountService.rename(created.id(), "Kas Cabang Bandung");

        String name = jdbc.queryForObject("select name from account where id = ?", String.class, created.id());
        assertThat(name).isEqualTo("Kas Cabang Bandung");
    }

    @Test
    void get_mapping_ke_dto_di_dalam_transaksi_aman_untuk_relasi_lazy() {
        AccountResponse created = accountService.create(
                new CreateAccountRequest(randomCode(), "Kas Cabang Surabaya", AccountType.ASSET, "1101"));

        assertThat(accountService.get(created.id()).parentCode()).isEqualTo("1101");
    }

    @Test
    void entity_detached_akses_relasi_lazy_gagal() {
        AccountResponse created = accountService.create(
                new CreateAccountRequest(randomCode(), "Kas Cabang Medan", AccountType.ASSET, "1101"));

        // Tanpa transaksi: transaksi repository sudah selesai -> entity DETACHED
        Account detached = repo.findById(created.id()).orElseThrow();

        assertThatThrownBy(() -> detached.getParent().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void findById_dua_kali_dalam_satu_transaksi_hanya_satu_query() {
        Long id = repo.findByCode("1102").orElseThrow().getId();
        Statistics stats = statistics();

        tx.executeWithoutResult(s -> {
            Account a = repo.findById(id).orElseThrow();
            Account b = repo.findById(id).orElseThrow();
            assertThat(a).isSameAs(b);       // instance yang sama dari persistence context
        });

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    private static String randomCode() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000L, 99_999_999L));
    }
}
