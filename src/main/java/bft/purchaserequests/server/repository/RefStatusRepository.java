package bft.purchaserequests.server.repository;

import bft.purchaserequests.common.RefStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

/**
 * Репозиторий справочника статусов, предоставляющий доступ к таблице {@code ref_status}
 */
@ThreadSafe
public interface RefStatusRepository extends JpaRepository<RefStatus, Integer> {
    /**
     * Находит статус по его названию
     *
     * @param name название статуса
     * @return {@link Optional} с найденным статусом, либо пустой, если не найден
     */
    Optional<RefStatus> findByName(String name);
}
