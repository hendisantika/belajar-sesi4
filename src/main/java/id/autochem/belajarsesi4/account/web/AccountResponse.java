package id.autochem.belajarsesi4.account.web;

import id.autochem.belajarsesi4.account.model.Account;
import id.autochem.belajarsesi4.account.model.AccountType;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 26/11/26
 * Time: 13.00
 *
 * DTO: entity tidak pernah dikirim langsung ke API.
 */
public record AccountResponse(Long id, String code, String name, AccountType type, boolean active,
                              String parentCode) {

    /**
     * Harus dipanggil di dalam transaksi bila parent ikut dibaca (relasi LAZY).
     */
    public static AccountResponse from(Account acc) {
        return new AccountResponse(acc.getId(), acc.getCode(), acc.getName(), acc.getType(), acc.isActive(),
                acc.getParent() == null ? null : acc.getParent().getCode());
    }
}
