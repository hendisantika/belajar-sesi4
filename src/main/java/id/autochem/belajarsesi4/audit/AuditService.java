package id.autochem.belajarsesi4.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * Propagation REQUIRED (default): bergabung ke transaksi pemanggil,
 * sehingga audit ikut di-rollback bila jurnal gagal (dan sebaliknya).
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    @Transactional
    public void record(String action, String entityType, Long entityId) {
        repo.save(new AuditLog(action, entityType, entityId));
    }
}
