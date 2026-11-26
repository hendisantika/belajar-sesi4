package id.autochem.belajarsesi4.common.exception;

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
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException account(String code) {
        return new NotFoundException("Akun tidak ditemukan: " + code);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " tidak ditemukan: " + id);
    }
}
