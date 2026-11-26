package id.autochem.belajarsesi4.journal.web;

import id.autochem.belajarsesi4.journal.model.JournalStatus;

import java.math.BigDecimal;
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
public record JournalResponse(Long id, String number, LocalDate date, JournalStatus status,
                              BigDecimal totalDebit, BigDecimal totalCredit, List<JournalLineResponse> lines) {
}
