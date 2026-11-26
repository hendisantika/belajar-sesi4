package id.autochem.belajarsesi4.journal;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import id.autochem.belajarsesi4.journal.model.JournalEntry;
import id.autochem.belajarsesi4.journal.model.JournalLine;
import id.autochem.belajarsesi4.journal.model.JournalStatus;
import id.autochem.belajarsesi4.journal.repository.JournalRepository;
import id.autochem.belajarsesi4.journal.service.JournalService;
import id.autochem.belajarsesi4.journal.web.CreateJournalRequest;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Time: 13.04
 *
 * Ukur jumlah query, jangan menebak.
 */
class JournalRepositoryIT extends AbstractIntegrationTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 1);

    @Autowired
    JournalService journalService;

    @Autowired
    JournalRepository journalRepo;

    @Autowired
    TransactionTemplate tx;

    @BeforeEach
    void seed() {
        for (int i = 1; i <= 3; i++) {
            journalService.create(new CreateJournalRequest("JV-N1-" + i, DAY.plusDays(i), List.of(
                    line("1101", "1000", "0"),
                    line("4101", "0", "1000"))));
        }
    }

    @Test
    void simpan_jurnal_dua_baris_baca_ulang_total_debit_sama_dengan_kredit() {
        tx.executeWithoutResult(s -> {
            JournalEntry j = journalRepo.findByNumber("JV-N1-1").orElseThrow();
            assertThat(j.getLines()).hasSize(2);
            assertThat(j.totalDebit()).isEqualByComparingTo(j.totalCredit());
            assertThat(j.getStatus()).isEqualTo(JournalStatus.DRAFT);
        });
    }

    @Test
    void findAll_lalu_akses_lines_menghasilkan_n_plus_1() {
        Statistics stats = statistics();

        tx.executeWithoutResult(s -> {
            List<JournalEntry> list = journalRepo.findAll();
            list.forEach(j -> j.getLines().size());  // lazy loading di dalam loop
        });

        // 1 query journal_entry + N(3) query journal_line
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1 + 3);
    }

    @Test
    void fetch_join_per_rentang_tanggal_cukup_1_query() {
        Statistics stats = statistics();

        List<String> accountCodes = tx.execute(s -> journalRepo.findWithLines(DAY, DAY.plusDays(10)).stream()
                .flatMap(j -> j.getLines().stream())
                .map(l -> l.getAccount().getCode())
                .toList());

        assertThat(accountCodes).hasSize(6);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void entity_graph_by_status_cukup_1_query() {
        Statistics stats = statistics();

        BigDecimal total = tx.execute(s -> journalRepo.findByStatus(JournalStatus.DRAFT).stream()
                .flatMap(j -> j.getLines().stream())
                .map(JournalLine::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        assertThat(total).isEqualByComparingTo("3000");
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void service_daftar_jurnal_per_tanggal_cukup_1_query() {
        Statistics stats = statistics();

        var result = journalService.findByDateRange(DAY.plusDays(2), DAY.plusDays(3));

        assertThat(result).extracting(r -> r.number()).containsExactly("JV-N1-2", "JV-N1-3");
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}
