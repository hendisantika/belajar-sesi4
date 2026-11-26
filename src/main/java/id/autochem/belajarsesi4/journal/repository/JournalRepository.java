package id.autochem.belajarsesi4.journal.repository;

import id.autochem.belajarsesi4.journal.model.JournalEntry;
import id.autochem.belajarsesi4.journal.model.JournalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
public interface JournalRepository extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByNumber(String number);

    boolean existsByNumber(String number);

    /**
     * [X] Rawan N+1: findAll() lalu akses getLines() di loop -> 1 query journal + N query journal_line.
     * [OK] Fetch join: jurnal + baris + akun dalam 1 query.
     */
    @Query("""
            select distinct j from JournalEntry j
            join fetch j.lines l
            join fetch l.account
            where j.date between :from and :to
            order by j.date, j.id""")
    List<JournalEntry> findWithLines(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * [OK] Atau secara deklaratif dengan EntityGraph.
     */
    @EntityGraph(attributePaths = {"lines", "lines.account"})
    List<JournalEntry> findByStatus(JournalStatus status);

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    Optional<JournalEntry> findWithLinesById(Long id);
}
