package id.autochem.belajarsesi4.journal.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
 * Header jurnal double-entry. Aturan bisnis (seimbang) menempel di entity (rich domain model).
 */
@Entity
@Table(name = "journal_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String number;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JournalStatus status = JournalStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalLine> lines = new ArrayList<>();

    public static JournalEntry draft(String number, LocalDate date) {
        JournalEntry entry = new JournalEntry();
        entry.number = number;
        entry.date = date;
        return entry;
    }

    /**
     * Jaga dua sisi relasi tetap sinkron.
     */
    public void addLine(JournalLine line) {
        lines.add(line);
        line.setEntry(this);
    }

    public void removeLine(JournalLine line) {
        lines.remove(line);
        line.setEntry(null);
    }

    public List<JournalLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public BigDecimal totalDebit() {
        return sum(JournalLine::getDebit);
    }

    public BigDecimal totalCredit() {
        return sum(JournalLine::getCredit);
    }

    public void assertBalanced() {
        BigDecimal dr = totalDebit();
        BigDecimal cr = totalCredit();
        if (dr.signum() == 0 || dr.compareTo(cr) != 0) {
            throw new UnbalancedJournalException(dr, cr);
        }
    }

    private BigDecimal sum(Function<JournalLine, BigDecimal> amount) {
        return lines.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
