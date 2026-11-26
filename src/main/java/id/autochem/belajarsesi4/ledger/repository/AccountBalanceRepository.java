package id.autochem.belajarsesi4.ledger.repository;

import id.autochem.belajarsesi4.ledger.model.AccountBalance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.03
 */
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    /**
     * Pessimistic locking: SELECT ... FOR UPDATE.
     * Transaksi lain menunggu sampai lock dilepas (commit/rollback); timeout agar tidak menunggu selamanya.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select b from AccountBalance b where b.accountId = :id")
    Optional<AccountBalance> lockById(@Param("id") Long id);
}
