package id.autochem.belajarsesi4.journal.service;

import id.autochem.belajarsesi4.account.model.Account;
import id.autochem.belajarsesi4.account.repository.AccountRepository;
import id.autochem.belajarsesi4.audit.AuditService;
import id.autochem.belajarsesi4.common.exception.BusinessRuleException;
import id.autochem.belajarsesi4.common.exception.NotFoundException;
import id.autochem.belajarsesi4.journal.model.JournalEntry;
import id.autochem.belajarsesi4.journal.model.JournalLine;
import id.autochem.belajarsesi4.journal.repository.JournalRepository;
import id.autochem.belajarsesi4.journal.web.CreateJournalRequest;
import id.autochem.belajarsesi4.journal.web.JournalResponse;
import id.autochem.belajarsesi4.journal.web.LineRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
 * Batas transaksi ada di service layer (use case), bukan controller atau repository.
 */
@Service
@RequiredArgsConstructor
public class JournalService {

    private static final int SCALE = 2;

    private final JournalRepository journals;
    private final AccountRepository accounts;
    private final AuditService audit;
    private final JournalMapper mapper;

    /**
     * Satu transaksi: gagal satu, batal semua.
     * Header, baris, dan audit di-COMMIT saat method selesai; RuntimeException -> ROLLBACK seluruhnya.
     */
    @Transactional
    public JournalResponse create(CreateJournalRequest req) {
        if (journals.existsByNumber(req.number())) {
            throw new BusinessRuleException("Nomor jurnal sudah dipakai: " + req.number());
        }
        JournalEntry entry = JournalEntry.draft(req.number(), req.date());
        Map<String, Account> accountsByCode = loadAccounts(req.lines());

        for (LineRequest l : req.lines()) {
            Account acc = accountsByCode.get(l.accountCode());
            if (acc == null) {
                throw NotFoundException.account(l.accountCode());
            }
            if (!acc.isActive()) {
                throw new BusinessRuleException("Akun nonaktif: " + acc.getCode());
            }
            BigDecimal debit = normalize(l.debit());
            BigDecimal credit = normalize(l.credit());
            if (debit.signum() < 0 || credit.signum() < 0) {
                throw new BusinessRuleException("Nominal harus positif");
            }
            if (debit.signum() == credit.signum()) {
                throw new BusinessRuleException(
                        "Baris akun " + acc.getCode() + " harus berisi debit atau kredit saja (salah satu > 0)");
            }
            entry.addLine(new JournalLine(acc, debit, credit));
        }
        entry.assertBalanced();                       // debit == credit

        JournalEntry saved = journals.save(entry);
        audit.record("CREATE", "JOURNAL_ENTRY", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public JournalResponse find(Long id) {
        return journals.findWithLinesById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> NotFoundException.of("Jurnal", id));
    }

    /**
     * Fetch join: daftar jurnal per rentang tanggal = 1 query (tanpa N+1).
     */
    @Transactional(readOnly = true)
    public List<JournalResponse> findByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessRuleException("Tanggal 'from' tidak boleh setelah 'to'");
        }
        return journals.findWithLines(from, to).stream().map(mapper::toResponse).toList();
    }

    /**
     * Semua akun di jurnal diambil dengan 1 query (WHERE code IN ...), bukan 1 query per baris.
     */
    private Map<String, Account> loadAccounts(List<LineRequest> lines) {
        List<String> codes = lines.stream().map(LineRequest::accountCode).distinct().toList();
        return accounts.findByCodeIn(codes).stream()
                .collect(Collectors.toMap(Account::getCode, Function.identity()));
    }

    private static BigDecimal normalize(BigDecimal amount) {
        // Validasi @Digits(fraction = 2) sudah menjamin maksimal 2 desimal, jadi tidak ada pembulatan diam-diam
        return amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }
}
