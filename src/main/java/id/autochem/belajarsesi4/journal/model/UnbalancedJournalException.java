package id.autochem.belajarsesi4.journal.model;

import id.autochem.belajarsesi4.common.exception.BusinessRuleException;
import lombok.Getter;

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
@Getter
public class UnbalancedJournalException extends BusinessRuleException {

    private final BigDecimal totalDebit;
    private final BigDecimal totalCredit;

    public UnbalancedJournalException(BigDecimal totalDebit, BigDecimal totalCredit) {
        super("Jurnal tidak seimbang: total debit " + totalDebit.toPlainString()
                + " != total kredit " + totalCredit.toPlainString());
        this.totalDebit = totalDebit;
        this.totalCredit = totalCredit;
    }
}
