package bft.purchaserequests.server.security;

import bft.purchaserequests.common.StatusName;
import bft.purchaserequests.server.repository.PurchaseRequestRepository;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Компонент проверки прав доступа к конкретной заявке
 */
@Component("requestSecurity")
@ThreadSafe
public class PurchaseRequestSecurity {

  private final PurchaseRequestRepository purchaseRequestRepository;

  /**
   * Создаёт компонент проверки прав
   *
   * @param purchaseRequestRepository репозиторий заявок
   * @throws NullPointerException если входной параметр null
   */
  public PurchaseRequestSecurity(PurchaseRequestRepository purchaseRequestRepository) {
    if (purchaseRequestRepository == null) {
      throw new NullPointerException("repository");
    }

    this.purchaseRequestRepository = purchaseRequestRepository;
  }

  /**
   * Проверяет владение заявкой
   *
   * @param requestId      идентификатор заявки
   * @param authentication текущая аутентификация
   * @return true, если заявка принадлежит текущему пользователю. Если заявка с таким id не найдена,
   * то возвращает false
   */
  public boolean isOwner(Integer requestId, Authentication authentication) {
    return this.purchaseRequestRepository.findById(requestId)
        .map(request -> request.getCreatedBy().getUsername().equals(authentication.getName()))
        .orElse(false);
  }

  /**
   * Проверяет, может ли текущий пользователь редактировать заявку
   *
   * @param requestId      идентификатор заявки
   * @param authentication текущая аутентификация
   * @return true, если заявка принадлежит пользователю и находится в статусе NEW
   */
  public boolean isOwnerAndEditable(Integer requestId, Authentication authentication) {
    return this.purchaseRequestRepository.findById(requestId)
        .map(request -> request.getCreatedBy().getUsername().equals(authentication.getName())
            && StatusName.NEW.getName().equals(request.getStatus().getName()))
        .orElse(false);
  }
}
