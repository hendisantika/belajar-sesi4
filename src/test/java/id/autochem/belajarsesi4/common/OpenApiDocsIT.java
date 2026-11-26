package id.autochem.belajarsesi4.common;

import id.autochem.belajarsesi4.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Time: 13.09
 */
class OpenApiDocsIT extends AbstractIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void api_docs_memuat_endpoint_account_dan_journal() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.info.title", v -> v.assertThat().isEqualTo("Belajar Sesi 4 - COA & Journal API"))
                .hasPathSatisfying("$.paths['/journals'].post.summary",
                        v -> v.assertThat().isEqualTo("Buat jurnal multi-baris"))
                .hasPathSatisfying("$.paths['/accounts/{id}/name']", v -> v.assertThat().isNotNull())
                .hasPathSatisfying("$.components.schemas.CreateJournalRequest", v -> v.assertThat().isNotNull());
    }

    @Test
    void swagger_ui_dapat_diakses() {
        assertThat(mvc.get().uri("/swagger-ui.html")).hasStatus3xxRedirection();
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatusOk();
    }
}
