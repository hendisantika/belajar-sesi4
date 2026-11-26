package id.autochem.belajarsesi4.account.repository;

import id.autochem.belajarsesi4.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

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

    List<Account> findAllByOrderByCodeAsc();
}
