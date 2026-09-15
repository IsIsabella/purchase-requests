package bft.purchaserequests.server.repository;

import bft.purchaserequests.common.RefRole;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий справочника ролей, предоставляющий доступ к таблице {@code ref_role}
 */
@ThreadSafe
public interface RefRoleRepository extends JpaRepository<RefRole, Integer> {
    /**
     * Находит роль по её названию
     *
     * @param name название роли
     * @return {@link Optional} с найденной ролью, либо пустой, если не найдена
     */
    Optional<RefRole> findByName(String name);
}
