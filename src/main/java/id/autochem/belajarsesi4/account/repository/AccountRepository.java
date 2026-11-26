package id.autochem.belajarsesi4.account.repository;

import id.autochem.belajarsesi4.account.model.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
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
 * Time: 13.00
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Satu query untuk banyak kode akun (menghindari findByCode per baris jurnal).
     */
    List<Account> findByCodeIn(Collection<String> codes);

    /**
     * Parent ikut di-load (left join) agar mapping ke DTO tidak memicu lazy fetch per akun.
     */
    @EntityGraph(attributePaths = "parent")
    List<Account> findAllByOrderByCodeAsc();

    @EntityGraph(attributePaths = "parent")
    Optional<Account> findWithParentById(Long id);
}
