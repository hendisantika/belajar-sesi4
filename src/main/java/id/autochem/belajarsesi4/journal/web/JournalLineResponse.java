package id.autochem.belajarsesi4.journal.web;

import java.math.BigDecimal;

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
public record JournalLineResponse(Long id, String accountCode, String accountName, BigDecimal debit,
                                  BigDecimal credit) {
}
