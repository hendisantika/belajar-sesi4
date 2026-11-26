package id.autochem.belajarsesi4.journal;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

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
 * Deliverable: bukti transaksi valid, jurnal unbalanced, dan rollback lewat API.
 */
class JournalApiIT extends AbstractIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void post_jurnal_valid_201() {
        String body = """
                {"number":"JV-API-001","date":"2026-11-26","lines":[
                  {"accountCode":"1101","debit":500000,"credit":0},
                  {"accountCode":"3101","debit":0,"credit":500000}]}""";

        assertThat(mvc.post().uri("/journals").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .hasPathSatisfying("$.status", v -> v.assertThat().isEqualTo("DRAFT"))
                .hasPathSatisfying("$.totalDebit", v -> v.assertThat().isEqualTo(500000.00))
                .hasPathSatisfying("$.lines.length()", v -> v.assertThat().isEqualTo(2));
    }

    @Test
    void post_jurnal_unbalanced_422() {
        String body = """
                {"number":"JV-API-002","date":"2026-11-26","lines":[
                  {"accountCode":"5102","debit":300000,"credit":0},
                  {"accountCode":"1102","debit":0,"credit":299999.99}]}""";

        assertThat(mvc.post().uri("/journals").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson()
                .hasPathSatisfying("$.detail", v -> v.assertThat().asString().contains("tidak seimbang"));
        assertThat(countJournalRows()).isZero();
    }

    @Test
    void post_jurnal_dengan_akun_tidak_ada_404_dan_tidak_ada_data_parsial() {
        String body = """
                {"number":"JV-API-003","date":"2026-11-26","lines":[
                  {"accountCode":"1101","debit":100000,"credit":0},
                  {"accountCode":"9999","debit":0,"credit":100000}]}""";

        assertThat(mvc.post().uri("/journals").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.NOT_FOUND);
        assertThat(countJournalRows()).isZero();
    }

    @Test
    void post_jurnal_tanpa_baris_400() {
        String body = """
                {"number":"JV-API-004","date":"2026-11-26","lines":[]}""";

        assertThat(mvc.post().uri("/journals").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.errors.lines", v -> v.assertThat().isEqualTo("jurnal minimal 2 baris"));
    }

    @Test
    void get_jurnal_per_id_dan_rentang_tanggal() {
        String body = """
                {"number":"JV-API-005","date":"2026-11-20","lines":[
                  {"accountCode":"1201","debit":75000.25,"credit":0},
                  {"accountCode":"4101","debit":0,"credit":75000.25}]}""";
        assertThat(mvc.post().uri("/journals").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.CREATED);

        assertThat(mvc.get().uri("/journals/1"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.number", v -> v.assertThat().isEqualTo("JV-API-005"));

        assertThat(mvc.get().uri("/journals?from=2026-11-01&to=2026-11-30"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", v -> v.assertThat().isEqualTo(1));
    }

    private long countJournalRows() {
        Long entries = jdbc.queryForObject("select count(*) from journal_entry", Long.class);
        Long lines = jdbc.queryForObject("select count(*) from journal_line", Long.class);
        return entries + lines;
    }
}
