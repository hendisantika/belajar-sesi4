package id.autochem.belajarsesi4.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.03
 *
 * Optimistic locking lewat @Version:
 * UPDATE account_balance SET balance=?, version=6 WHERE account_id=? AND version=5
 * 0 baris ter-update -> ObjectOptimisticLockingFailureException.
 */
@Entity
@Table(name = "account_balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBalance {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;

    public static AccountBalance open(Long accountId) {
        AccountBalance b = new AccountBalance();
        b.accountId = accountId;
        return b;
    }

    public void apply(BigDecimal delta) {
        this.balance = this.balance.add(delta);
    }
}
