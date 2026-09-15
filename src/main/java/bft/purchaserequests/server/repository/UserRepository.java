package bft.purchaserequests.server.repository;

import bft.purchaserequests.common.User;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий пользователей, предоставляющий CRUD-операции для пользователей
 *
 * <p>Тип идентификатора - {@link Integer}, в соответствии с типом поля
 * {@code User.id}</p>
 */
@ThreadSafe
public interface UserRepository extends JpaRepository<User, Integer> {

  /**
   * Находит пользователя по имени
   *
   * @param username имя пользователя
   * @return {@link Optional} с найденным пользователем, либо пустой, если пользователь не найден
   */
  Optional<User> findByUsername(String username);
}
