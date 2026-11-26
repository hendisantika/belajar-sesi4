package id.autochem.belajarsesi4.performance;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import id.autochem.belajarsesi4.account.model.AccountType;
import id.autochem.belajarsesi4.account.service.AccountService;
import id.autochem.belajarsesi4.account.web.CreateAccountRequest;
import id.autochem.belajarsesi4.journal.service.JournalService;
import id.autochem.belajarsesi4.journal.web.CreateJournalRequest;
import id.autochem.belajarsesi4.journal.web.LineRequest;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.56
 *
 * Penjaga N+1: jumlah SELECT tidak boleh tumbuh mengikuti jumlah data, dan tidak boleh ada lazy fetch
 * (entity/collection fetch) di jalur baca maupun tulis.
 */
class NPlusOneGuardIT extends AbstractIntegrationTest {

    private static final String[] PARENTS = {"1101", "1102", "1201", "2101", "3101"};

    @Autowired
    AccountService accountService;

    @Autowired
    JournalService journalService;

    @AfterEach
    void removeChildAccounts() {
        jdbc.update("delete from account where code like '77%'");
    }

    @Test
    void daftar_akun_dengan_parent_tanpa_n_plus_1() {
        for (int i = 0; i < PARENTS.length; i++) {
            accountService.create(new CreateAccountRequest("77" + i + "001", "Sub " + i, AccountType.ASSET, PARENTS[i]));
        }
        Statistics stats = statistics();

        var list = accountService.list();

        assertThat(list).filteredOn(a -> a.code().startsWith("77")).extracting(a -> a.parentCode())
                .containsExactlyInAnyOrder(PARENTS);
        assertThat(stats.getEntityFetchCount()).as("lazy fetch parent").isZero();
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void detail_akun_dengan_parent_cukup_1_query() {
        Long id = accountService.create(new CreateAccountRequest("77009", "Sub detail", AccountType.ASSET, "1101")).id();
        Statistics stats = statistics();

        assertThat(accountService.get(id).parentCode()).isEqualTo("1101");
        assertThat(stats.getEntityFetchCount()).isZero();
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void create_jurnal_jumlah_select_konstan_berapapun_jumlah_baris() {
        long select2 = selectsForJournal("JV-G-2", 2);
        long select8 = selectsForJournal("JV-G-8", 8);

        assertThat(select8).as("SELECT untuk 8 baris vs 2 baris").isEqualTo(select2);
    }

    @Test
    void baca_jurnal_tanpa_lazy_fetch() {
        selectsForJournal("JV-G-R", 6);
        Long id = jdbc.queryForObject("select id from journal_entry where number = 'JV-G-R'", Long.class);
        Statistics stats = statistics();

        journalService.find(id);
        journalService.findByDateRange(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(stats.getEntityFetchCount()).isZero();
        assertThat(stats.getCollectionFetchCount()).isZero();
        assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
    }

    /**
     * Jumlah SELECT = total statement dikurangi INSERT (header + baris + audit).
     */
    private long selectsForJournal(String number, int lineCount) {
        List<LineRequest> lines = new ArrayList<>();
        for (int i = 0; i < lineCount / 2; i++) {
            lines.add(line(PARENTS[i % PARENTS.length], "1000", "0"));
            lines.add(line("4101", "0", "1000"));
        }
        Statistics stats = statistics();

        journalService.create(new CreateJournalRequest(number, LocalDate.now(), lines));

        assertThat(stats.getEntityFetchCount()).isZero();
        long inserts = stats.getEntityInsertCount();
        assertThat(inserts).isEqualTo(1 + lineCount + 1);
        return stats.getPrepareStatementCount() - inserts;
    }
}
