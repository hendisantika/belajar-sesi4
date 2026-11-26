package id.autochem.belajarsesi4.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.09
 *
 * Dokumentasi OpenAPI 3: Swagger UI di /swagger-ui.html, spesifikasi JSON di /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI belajarSesi4OpenApi(@Value("${server.port:8080}") int port) {
        return new OpenAPI()
                .info(new Info()
                        .title("Belajar Sesi 4 - COA & Journal API")
                        .description("""
                                Journal Entry, Transactions & JPA Performance.
                                Jurnal multi-baris double-entry: akun harus aktif, nominal positif, total debit = total kredit,
                                dan satu jurnal disimpan dalam satu transaksi (gagal satu, batal semua).
                                Error mengikuti format ProblemDetail (RFC 9457).""")
                        .version("v1")
                        .contact(new Contact().name("hendisantika").url("https://s.id/hendisantika"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(new Server().url("http://localhost:" + port).description("Local")))
                .tags(List.of(
                        new Tag().name("Account").description("Chart of Accounts (COA)"),
                        new Tag().name("Journal").description("Journal Entry multi-baris")));
    }
}
