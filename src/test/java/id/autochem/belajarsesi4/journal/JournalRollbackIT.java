package id.autochem.belajarsesi4.journal;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import id.autochem.belajarsesi4.common.exception.BusinessRuleException;
import id.autochem.belajarsesi4.common.exception.NotFoundException;
import id.autochem.belajarsesi4.journal.model.UnbalancedJournalException;
import id.autochem.belajarsesi4.journal.repository.JournalLineRepository;
import id.autochem.belajarsesi4.journal.repository.JournalRepository;
import id.autochem.belajarsesi4.journal.service.JournalService;
import id.autochem.belajarsesi4.journal.web.CreateJournalRequest;
import id.autochem.belajarsesi4.journal.web.JournalResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.04
 */
class JournalRollbackIT extends AbstractIntegrationTest {

    @Autowired
    JournalService journalService;

    @Autowired
    JournalRepository journalRepo;

    @Autowired
    JournalLineRepository lineRepo;

    @Test
    void jurnal_valid_tersimpan_dengan_bigdecimal_scale_2() {
        var req = new CreateJournalRequest("JV-OK-001", LocalDate.now(), List.of(
                line("1101", "150000.5", "0"),
                line("4101", "0", "150000.50")));

        JournalResponse res = journalService.create(req);

        assertThat(res.id()).isNotNull();
        assertThat(res.totalDebit()).isEqualByComparingTo(res.totalCredit());
        assertThat(res.totalDebit()).isEqualTo(new BigDecimal("150000.50"));
        assertThat(lineRepo.count()).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from audit_log", Long.class)).isEqualTo(1);
    }

    @Test
    void tidak_ada_jurnal_parsial_saat_salah_satu_baris_gagal() {
        var req = new CreateJournalRequest("JV-001", LocalDate.now(), List.of(
                line("1101", "100000", "0"),
                line("9999", "0", "100000")));        // akun tidak ada -> gagal

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(NotFoundException.class);

        // Bukti rollback: header maupun baris tidak tersimpan
        assertThat(journalRepo.findByNumber("JV-001")).isEmpty();
        assertThat(lineRepo.count()).isZero();
    }

    @Test
    void rollback_saat_gagal_setelah_header_dan_baris_sudah_di_insert() {
        // INSERT header + baris sudah dikirim ke DB (IDENTITY), lalu audit gagal -> seluruh transaksi batal
        doThrow(new IllegalStateException("audit service down"))
                .when(auditService).record(anyString(), anyString(), any());

        var req = new CreateJournalRequest("JV-002", LocalDate.now(), List.of(
                line("1101", "250000", "0"),
                line("3101", "0", "250000")));

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(IllegalStateException.class);

        assertThat(journalRepo.findByNumber("JV-002")).isEmpty();
        assertThat(lineRepo.count()).isZero();
        assertThat(jdbc.queryForObject("select count(*) from audit_log", Long.class)).isZero();
    }

    @Test
    void jurnal_tidak_seimbang_ditolak() {
        var req = new CreateJournalRequest("JV-003", LocalDate.now(), List.of(
                line("5101", "100000", "0"),
                line("1101", "0", "90000")));

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(UnbalancedJournalException.class)
                .hasMessageContaining("100000.00")
                .hasMessageContaining("90000.00");
        assertThat(journalRepo.count()).isZero();
    }

    @Test
    void akun_nonaktif_ditolak() {
        var req = new CreateJournalRequest("JV-004", LocalDate.now(), List.of(
                line("1999", "50000", "0"),
                line("4101", "0", "50000")));

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Akun nonaktif: 1999");
        assertThat(journalRepo.count()).isZero();
    }

    @Test
    void nominal_negatif_ditolak() {
        var req = new CreateJournalRequest("JV-005", LocalDate.now(), List.of(
                line("1101", "-50000", "0"),
                line("4101", "0", "-50000")));

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Nominal harus positif");
        assertThat(journalRepo.count()).isZero();
    }

    @Test
    void baris_dengan_debit_dan_kredit_sekaligus_ditolak() {
        var req = new CreateJournalRequest("JV-006", LocalDate.now(), List.of(
                line("1101", "50000", "50000"),
                line("4101", "0", "0")));

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("debit atau kredit saja");
    }

    @Test
    void nomor_jurnal_duplikat_ditolak() {
        var req = new CreateJournalRequest("JV-007", LocalDate.now(), List.of(
                line("1101", "1000", "0"),
                line("4101", "0", "1000")));
        journalService.create(req);

        assertThatThrownBy(() -> journalService.create(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("JV-007");
        assertThat(journalRepo.count()).isEqualTo(1);
    }
}
