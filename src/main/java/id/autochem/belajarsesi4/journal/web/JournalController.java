package id.autochem.belajarsesi4.journal.web;

import id.autochem.belajarsesi4.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.02
 */
@Tag(name = "Journal")
@RestController
@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService service;

    @Operation(summary = "Buat jurnal multi-baris",
            description = """
                    Validasi per baris: akun ada & aktif, nominal positif, debit atau kredit saja.
                    Validasi header: total debit = total kredit, nomor unik.
                    Satu transaksi: bila satu baris gagal, header & seluruh baris di-rollback.""")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
            @ExampleObject(name = "Valid", summary = "Setoran modal (201)", value = """
                    {"number":"JV-001","date":"2026-11-26","lines":[
                      {"accountCode":"1101","debit":500000,"credit":0},
                      {"accountCode":"3101","debit":0,"credit":500000}]}"""),
            @ExampleObject(name = "Unbalanced", summary = "Debit != kredit (422)", value = """
                    {"number":"JV-002","date":"2026-11-26","lines":[
                      {"accountCode":"5102","debit":300000,"credit":0},
                      {"accountCode":"1102","debit":0,"credit":299999.99}]}"""),
            @ExampleObject(name = "Rollback", summary = "Akun baris kedua tidak ada (404)", value = """
                    {"number":"JV-003","date":"2026-11-26","lines":[
                      {"accountCode":"1101","debit":100000,"credit":0},
                      {"accountCode":"9999","debit":0,"credit":100000}]}""")
    }))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jurnal tersimpan dengan status DRAFT"),
            @ApiResponse(responseCode = "400", description = "Validasi request gagal", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan, tidak ada data parsial", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Jurnal tidak seimbang / akun nonaktif / nominal negatif", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<JournalResponse> create(@Valid @RequestBody CreateJournalRequest req) {
        JournalResponse res = service.create(req);
        return ResponseEntity.created(URI.create("/journals/" + res.id())).body(res);
    }

    @Operation(summary = "Detail jurnal beserta baris", description = "Memakai @EntityGraph: 1 query")
    @ApiResponse(responseCode = "404", description = "Jurnal tidak ditemukan", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public JournalResponse get(@Parameter(description = "ID jurnal", example = "1") @PathVariable Long id) {
        return service.find(id);
    }

    @Operation(summary = "Daftar jurnal per rentang tanggal", description = "Fetch join jurnal + baris + akun: 1 query, tanpa N+1")
    @ApiResponse(responseCode = "422", description = "from setelah to", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public List<JournalResponse> list(
            @Parameter(description = "Tanggal awal (inklusif)", example = "2026-11-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Tanggal akhir (inklusif)", example = "2026-11-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findByDateRange(from, to);
    }
}
