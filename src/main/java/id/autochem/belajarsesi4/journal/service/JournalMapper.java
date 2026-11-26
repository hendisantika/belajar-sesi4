package id.autochem.belajarsesi4.journal.service;

import id.autochem.belajarsesi4.journal.model.JournalEntry;
import id.autochem.belajarsesi4.journal.model.JournalLine;
import id.autochem.belajarsesi4.journal.web.JournalLineResponse;
import id.autochem.belajarsesi4.journal.web.JournalResponse;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.02
 *
 * Entity -> DTO. Dipanggil di dalam transaksi agar relasi LAZY masih bisa dibaca.
 */
@Component
public class JournalMapper {

    public JournalResponse toResponse(JournalEntry entry) {
        return new JournalResponse(entry.getId(), entry.getNumber(), entry.getDate(), entry.getStatus(),
                entry.totalDebit(), entry.totalCredit(),
                entry.getLines().stream().map(this::toLine).toList());
    }

    private JournalLineResponse toLine(JournalLine line) {
        return new JournalLineResponse(line.getId(), line.getAccount().getCode(), line.getAccount().getName(),
                line.getDebit(), line.getCredit());
    }
}
