package bft.purchaserequests.server.repository;

import bft.purchaserequests.common.PurchaseRequest;
import bft.purchaserequests.common.RefStatus;
import bft.purchaserequests.common.User;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий заявок на закупку, предоставляет CRUD-операции и специализированный поиск заявок
 *
 * <p>Тип идентификатора - {@link Integer}, в соответствии с типом поля
 * {@code PurchaseRequest.id}</p>
 */
@ThreadSafe
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Integer> {

  /**
   * Возвращает заявки с указанным статусом
   *
   * @param status статус заявки
   * @return список заявок с этим статусом
   */
  List<PurchaseRequest> findByStatus(RefStatus status);

  /**
   * Возвращает заявки конкретного пользователя
   *
   * @param createdBy создатель
   * @return список заявок пользователя
   */
  List<PurchaseRequest> findByCreatedBy(User createdBy);
}
