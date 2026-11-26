package id.autochem.belajarsesi4;

import id.autochem.belajarsesi4.audit.AuditService;
import id.autochem.belajarsesi4.journal.web.LineRequest;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;

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
 * Basis integration test: satu Spring context + satu container PostgreSQL dipakai bersama semua *IT.
 * Spy AuditService diletakkan di sini agar context tidak dibuat ulang per kelas test.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected EntityManagerFactory emf;

    @MockitoSpyBean
    protected AuditService auditService;

    @BeforeEach
    void cleanJournalTables() {
        jdbc.execute("TRUNCATE journal_line, journal_entry, audit_log RESTART IDENTITY");
    }

    protected Statistics statistics() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        return stats;
    }

    protected static LineRequest line(String accountCode, String debit, String credit) {
        return new LineRequest(accountCode, new BigDecimal(debit), new BigDecimal(credit));
    }
}
