package id.autochem.belajarsesi4.journal.web;

import id.autochem.belajarsesi4.journal.service.JournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RestController
@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService service;

    @PostMapping
    public ResponseEntity<JournalResponse> create(@Valid @RequestBody CreateJournalRequest req) {
        JournalResponse res = service.create(req);
        return ResponseEntity.created(URI.create("/journals/" + res.id())).body(res);
    }

    @GetMapping("/{id}")
    public JournalResponse get(@PathVariable Long id) {
        return service.find(id);
    }

    @GetMapping
    public List<JournalResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findByDateRange(from, to);
    }
}
