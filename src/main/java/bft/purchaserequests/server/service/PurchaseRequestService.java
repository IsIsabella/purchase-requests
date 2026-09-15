package bft.purchaserequests.server.service;

import bft.purchaserequests.common.PurchaseRequest;
import bft.purchaserequests.common.RefStatus;
import bft.purchaserequests.common.RoleName;
import bft.purchaserequests.common.StatusName;
import bft.purchaserequests.common.User;
import bft.purchaserequests.server.dto.PurchaseRequestCreateRequest;
import bft.purchaserequests.server.dto.PurchaseRequestUpdateRequest;
import bft.purchaserequests.server.repository.PurchaseRequestRepository;
import bft.purchaserequests.server.repository.RefStatusRepository;
import java.util.List;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Сервис управления заявками на закупку
 *
 * <p>REST-контроллер и Vaadin-формы вызывают эти методы, что позволяет
 * использовать все правила в одном месте.</p>
 *
 * <p>Правила доступа:</p>
 * <ul>
 *     <li>EMPLOYEE - создаёт заявки со статусом NEW и может редактировать/удалять
 *     собственную заявку, пока она в статусе NEW;</li>
 *     <li>MANAGER - меняет статус любой заявки, утверждающим
 *     автоматически становится MANAGER;</li>
 *     <li>ADMIN - может редактировать содержимое любой заявки в любом
 *     статусе и вручную переназначать утверждающего.</li>
 * </ul>
 * Реализует паттерн singleton
 */
@Service
@ThreadSafe
public class PurchaseRequestService {

  private final PurchaseRequestRepository purchaseRequestRepository;
  private final RefStatusRepository statusRepository;
  private final UserService userService;

  /**
   * Создаёт сервис заявок
   *
   * @param purchaseRequestRepository репозиторий заявок
   * @param statusRepository          репозиторий справочника статусов
   * @param userService               сервис пользователей (нужен для поиска утверждающего при
   *                                  ручном назначении администратором)
   * @throws NullPointerException если какой-то из входных параметров null
   */
  public PurchaseRequestService(PurchaseRequestRepository purchaseRequestRepository,
      RefStatusRepository statusRepository, UserService userService) {
    if (purchaseRequestRepository == null) {
      throw new NullPointerException("purchaseRequestRepository");
    }
    if (statusRepository == null) {
      throw new NullPointerException("statusRepository");
    }
    if (userService == null) {
      throw new NullPointerException("userService");
    }

    this.purchaseRequestRepository = purchaseRequestRepository;
    this.statusRepository = statusRepository;
    this.userService = userService;
  }

  /**
   * Возвращает все заявки
   *
   * @return список всех заявок
   */
  public List<PurchaseRequest> findAll() {
    return this.purchaseRequestRepository.findAll();
  }

  /**
   * Возвращает заявки конкретного пользователя
   *
   * @param user пользователь-создатель
   * @return заявки пользователя
   * @throws NullPointerException если входной параметр null
   */
  public List<PurchaseRequest> findByCurrentUser(User user) {
    if (user == null) {
      throw new NullPointerException("user");
    }

    return this.purchaseRequestRepository.findByCreatedBy(user);
  }

  /**
   * Находит заявку по идентификатору
   *
   * @param id идентификатор заявки
   * @return заявка, либо пустой {@link Optional}, если не найдена
   */
  public Optional<PurchaseRequest> findById(Integer id) {
    return this.purchaseRequestRepository.findById(id);
  }

  /**
   * Находит заявки по названию статуса
   *
   * @param statusName название статуса
   * @return список заявок с этим статусом
   * @throws IllegalArgumentException если статус не передан
   */
  public List<PurchaseRequest> findByStatus(String statusName) {
    if (statusName.isEmpty()) {
      throw new IllegalArgumentException("Статус не может быть пустым");
    }

    return this.purchaseRequestRepository.findByStatus(this.resolveStatus(statusName));
  }

  /**
   * Возвращает заявки пользователя с указанным статусом
   *
   * @param statusName название статуса
   * @param user       пользователь, чьи заявки нужно найти
   * @return отфильтрованный список заявок
   * @throws IllegalArgumentException если статус не передан
   * @throws NullPointerException     если пользователь null
   */
  public List<PurchaseRequest> findByStatusAndUser(String statusName, User user) {
    if (statusName == null || statusName.isEmpty()) {
      throw new IllegalArgumentException("Статус не может быть пустым");
    }
    if (user == null) {
      throw new NullPointerException("user");
    }

    RefStatus status = this.resolveStatus(statusName);
    return this.purchaseRequestRepository.findByStatus(status).stream()
        .filter(request -> request.getCreatedBy().getId().equals(user.getId()))
        .toList();
  }

  /**
   * Создаёт новую заявку
   *
   * @param input       данные новой заявки
   * @param currentUser пользователь, от имени которого создаётся заявка
   * @return сохранённая заявка
   * @throws IllegalArgumentException если название пустое или сумма не является положительным
   *                                  числом
   * @throws NullPointerException     если входные параметры null
   */
  public PurchaseRequest create(PurchaseRequestCreateRequest input, User currentUser) {
    if (input == null) {
      throw new NullPointerException("input");
    }
    if (currentUser == null) {
      throw new NullPointerException("currentUser");
    }
    if (input.name().isEmpty()) {
      throw new IllegalArgumentException("Название заявки не может быть пустым");
    }
    if (input.amount() == null || input.amount().signum() <= 0) {
      throw new IllegalArgumentException("Сумма заявки должна быть положительным числом");
    }

    PurchaseRequest request = new PurchaseRequest();
    request.setName(input.name());
    request.setDescription(input.description());
    request.setAmount(input.amount());
    request.setCreatedBy(currentUser);
    request.setStatus(this.requireInitialStatus());

    return this.purchaseRequestRepository.save(request);
  }

  /**
   * Обновляет содержимое заявки
   *
   * @param id          идентификатор заявки
   * @param input       новые данные
   * @param currentUser текущий пользователь
   * @param isAdmin     true, если вызывающий ADMIN
   * @return обновлённая заявка
   * @throws IllegalArgumentException если заявка не найдена, данные не прошли валидацию, либо
   *                                  указанный утверждающий не найден
   * @throws NullPointerException     если не переданы данные для обновления, либо пользователь
   * @throws AccessDeniedException    если вызывающий не ADMIN и не является владельцем заявки в
   *                                  статусе NEW
   */
  public PurchaseRequest update(Integer id, PurchaseRequestUpdateRequest input,
      User currentUser, boolean isAdmin) {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("id");
    }
    if (input == null) {
      throw new NullPointerException("input");
    }
    if (currentUser == null) {
      throw new NullPointerException("currentUser");
    }

    PurchaseRequest existing = this.purchaseRequestRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));
    boolean isOwner = existing.getCreatedBy().getId().equals(currentUser.getId());
    boolean editable = this.isEqualsStatus(existing, StatusName.NEW);

    if (!isAdmin && !(isOwner && editable)) {
      throw new AccessDeniedException(
          "Редактировать заявку может только её владелец в статусе " +
              StatusName.NEW.getName() + " либо " + RoleName.ADMIN.getName());
    }
    if (input.name().isEmpty()) {
      throw new IllegalArgumentException("Название заявки не может быть пустым");
    }
    if (input.amount() == null || input.amount().signum() <= 0) {
      throw new IllegalArgumentException("Сумма заявки должна быть положительным числом");
    }

    existing.setName(input.name());
    existing.setDescription(input.description());
    existing.setAmount(input.amount());

    if (isAdmin && input.approvedById() != null) {
      User approver = this.userService.findById(input.approvedById())
          .orElseThrow(() -> new IllegalArgumentException(
              "Утверждающий не найден: " + input.approvedById()));
      existing.setApprovedBy(approver);
    }

    return this.purchaseRequestRepository.save(existing);
  }

  /**
   * Меняет статус заявки
   *
   * @param id         идентификатор заявки
   * @param statusName название нового статуса
   * @param approver   пользователь, выполняющий смену статуса
   * @return обновлённая заявка
   * @throws IllegalArgumentException если заявка не найдена, либо статус с таким названием
   *                                  отсутствует в справочнике
   * @throws NullPointerException     пользователь, выполняющий смену статуса, не передан
   */
  public PurchaseRequest changeStatus(Integer id, String statusName, User approver) {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("id");
    }
    if (statusName == null || statusName.isBlank()) {
      throw new IllegalArgumentException("Статус не может быть пустым");
    }
    if (approver == null) {
      throw new NullPointerException("approver");
    }

    RefStatus newStatus = this.resolveStatus(statusName);
    PurchaseRequest request = this.purchaseRequestRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));

    request.setStatus(newStatus);
    request.setApprovedBy(approver);

    return this.purchaseRequestRepository.save(request);
  }

  /**
   * Удаляет заявку
   *
   * @param id          идентификатор заявки
   * @param currentUser текущий пользователь
   * @param isAdmin     true, если вызывающий пользователь - ADMIN
   * @throws IllegalArgumentException если заявка не найдена
   * @throws NullPointerException     если пользователь не передан
   * @throws AccessDeniedException    если вызывающий не ADMIN и не является владельцем заявки в
   *                                  статусе {@code NEW}
   */
  public void delete(Integer id, User currentUser, boolean isAdmin) {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("id");
    }
    if (currentUser == null) {
      throw new NullPointerException("currentUser");
    }

    PurchaseRequest existing = this.purchaseRequestRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));

    boolean isOwner = existing.getCreatedBy().getId().equals(currentUser.getId());
    boolean editable = this.isEqualsStatus(existing, StatusName.NEW);

    if (!isAdmin && !(isOwner && editable)) {
      throw new AccessDeniedException(
          "Удалить заявку может только её владелец в статусе " +
              StatusName.NEW.getName() + " либо " + RoleName.ADMIN.getName());
    }

    this.purchaseRequestRepository.delete(existing);
  }

  /**
   * Находит запись справочника статусов по названию, используется для значений, которые пришли от
   * клиента
   *
   * @param statusName название статуса
   * @return запись справочника
   */
  private RefStatus resolveStatus(String statusName) {
    return this.statusRepository.findByName(statusName)
        .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус: " + statusName));
  }

  /**
   * Находит начальный статус NEW для вновь создаваемой заявки, отсутствие этой записи - признак
   * того, что миграции Liquibase не применились
   *
   * @return запись справочника для статуса NEW
   * @throws IllegalStateException если статус NEW отсутствует в БД
   */
  private RefStatus requireInitialStatus() {
    return this.statusRepository.findByName(StatusName.NEW.getName())
        .orElseThrow(() -> new IllegalStateException(
            "В БД отсутствует статус " + StatusName.NEW.getName()));
  }

  /**
   * Проверяет, соответствует ли статус заявки указанному значению справочника
   *
   * @param request  заявка
   * @param expected ожидаемый статус
   * @return true, если статус совпадает
   */
  private boolean isEqualsStatus(PurchaseRequest request, StatusName expected) {
    return request.getStatus() != null
        && expected.getName().equals(request.getStatus().getName());
  }
}