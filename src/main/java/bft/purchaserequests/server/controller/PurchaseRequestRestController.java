package bft.purchaserequests.server.controller;

import bft.purchaserequests.common.PurchaseRequest;
import bft.purchaserequests.common.RoleName;
import bft.purchaserequests.common.User;
import bft.purchaserequests.server.dto.PurchaseRequestCreateRequest;
import bft.purchaserequests.server.dto.PurchaseRequestUpdateRequest;
import bft.purchaserequests.server.service.PurchaseRequestService;
import bft.purchaserequests.server.service.UserService;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API для управления заявками на закупку
 *
 * <p>Разграничение доступа:</p>
 * <ul>
 *     <li>EMPLOYEE - создаёт заявки со статусом NEW и может редактировать/удалять
 *     собственную заявку, пока она в статусе NEW;</li>
 *     <li>MANAGER - меняет статус любой заявки, утверждающим
 *     автоматически становится MANAGER;</li>
 *     <li>ADMIN - может редактировать содержимое любой заявки в любом
 *     статусе и вручную переназначать утверждающего.</li>
 * </ul>
 *
 * <p>Контроллер не имеет доступа к репозиториям напрямую - вся
 * бизнес-логика инкапсулирована в {@link PurchaseRequestService}.</p>
 */
@RestController
@RequestMapping("/rest/purchase-requests")
@ThreadSafe
public class PurchaseRequestRestController {

  private final PurchaseRequestService purchaseRequestService;
  private final UserService userService;

  /**
   * Создаёт REST-контроллер заявок
   *
   * @param purchaseRequestService сервис заявок
   * @param userService            сервис пользователей
   * @throws NullPointerException если какой-то из входных параметров null
   */
  public PurchaseRequestRestController(PurchaseRequestService purchaseRequestService,
      UserService userService) {
    if (purchaseRequestService == null) {
      throw new NullPointerException("purchaseRequestService");
    }
    if (userService == null) {
      throw new NullPointerException("userService");
    }

    this.purchaseRequestService = purchaseRequestService;
    this.userService = userService;
  }

  /**
   * Возвращает текущего аутентифицированного пользователя как сущность
   *
   * @param authentication текущая аутентификация
   * @return пользователь из базы данных
   * @throws IllegalStateException если аутентифицированный пользователь отсутствует в БД
   */
  private User currentUser(Authentication authentication) {
    return this.userService.findByUsername(authentication.getName())
        .orElseThrow(() -> new IllegalStateException("Текущий пользователь не найден в БД"));
  }

  /**
   * Проверяет наличие роли у пользователя
   *
   * @param authentication текущая аутентификация
   * @param role           название роли
   * @return true, если пользователь имеет указанную роль
   */
  private boolean hasRole(Authentication authentication, String role) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
        .anyMatch(authority -> authority.equals("ROLE_" + role));
  }

  /**
   * Возвращает список заявок
   *
   * @param authentication текущая аутентификация
   * @return список заявок
   * @throws NullPointerException если входной параметр null
   */
  @GetMapping
  public List<PurchaseRequest> getAll(Authentication authentication) {
    if (authentication == null) {
      throw new NullPointerException("authentication");
    }

    if (this.hasRole(authentication, RoleName.ADMIN.getName()) ||
        this.hasRole(authentication, RoleName.MANAGER.getName())) {
      return this.purchaseRequestService.findAll();
    }
    return this.purchaseRequestService.findByCurrentUser(this.currentUser(authentication));
  }

  /**
   * Возвращает заявку по идентификатору
   *
   * @param id идентификатор заявки
   * @return {@code 200 OK} с заявкой, либо {@code 404 Not Found}, если не найдена
   */
  @GetMapping("/{id}")
  @PreAuthorize("""
      hasAnyRole('ADMIN','MANAGER')
      or @requestSecurity.isOwner(#id, authentication)
      """)
  public ResponseEntity<PurchaseRequest> getById(@PathVariable Integer id) {
    return this.purchaseRequestService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Ищет заявки по названию статуса
   *
   * @param status         название статуса
   * @param authentication текущая аутентификация
   * @return список подходящих заявок
   * @throws NullPointerException если входной параметр null
   */
  @GetMapping("/search")
  public List<PurchaseRequest> searchByStatus(@RequestParam String status,
      Authentication authentication) {
    if (authentication == null) {
      throw new NullPointerException("authentication");
    }
    if (this.hasRole(authentication, RoleName.ADMIN.getName()) ||
        this.hasRole(authentication, RoleName.MANAGER.getName())) {
      return this.purchaseRequestService.findByStatus(status);
    }
    return this.purchaseRequestService.findByStatusAndUser(status,
        this.currentUser(authentication));
  }

  /**
   * Создаёт новую заявку от имени текущего пользователя
   *
   * @param request        данные новой заявки
   * @param authentication текущая аутентификация
   * @return созданная заявка
   * @throws NullPointerException если входной параметр null
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
  public PurchaseRequest create(@RequestBody PurchaseRequestCreateRequest request,
      Authentication authentication) {
    if (authentication == null) {
      throw new NullPointerException("authentication");
    }

    return this.purchaseRequestService.create(request, this.currentUser(authentication));
  }

  /**
   * Полностью обновляет содержимое заявки
   *
   * @param id             идентификатор заявки
   * @param request        новые данные
   * @param authentication текущая аутентификация
   * @return {@code 200 OK} с обновлённой заявкой
   * @throws NullPointerException если входной параметр null
   */
  @PutMapping("/{id}")
  @PreAuthorize("""
      hasRole('ADMIN')
      or @requestSecurity.isOwnerAndEditable(#id, authentication)
      """)
  public ResponseEntity<PurchaseRequest> update(
      @PathVariable Integer id,
      @RequestBody PurchaseRequestUpdateRequest request,
      Authentication authentication) {
    if (authentication == null) {
      throw new NullPointerException("authentication");
    }

    boolean isAdmin = this.hasRole(authentication, RoleName.ADMIN.getName());
    PurchaseRequest result = this.purchaseRequestService.update(id, request,
        this.currentUser(authentication), isAdmin);
    return ResponseEntity.ok(result);
  }

  /**
   * Меняет статус заявки
   *
   * @param id             идентификатор заявки
   * @param status         название нового статуса
   * @param authentication текущая аутентификация
   * @return {@code 200 OK} с обновлённой заявкой
   * @throws NullPointerException если входной параметр null
   */
  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
  public ResponseEntity<PurchaseRequest> changeStatus(
      @PathVariable Integer id,
      @RequestParam String status,
      Authentication authentication) {
    if (authentication == null) {
      throw new NullPointerException("authentication");
    }

    PurchaseRequest result = this.purchaseRequestService.changeStatus(id, status,
        this.currentUser(authentication));
    return ResponseEntity.ok(result);
  }

  /**
   * Удаляет заявку
   *
   * @param id             идентификатор заявки
   * @param authentication текущая аутентификация
   * @return {@code 204 No Content}
   * @throws NullPointerException если входной параметр null
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("""
      hasRole('ADMIN')
      or @requestSecurity.isOwnerAndEditable(#id, authentication)
      """)
  public ResponseEntity<Void> delete(@PathVariable Integer id, Authentication authentication) {
    if (authentication == null) {
      throw new NullPointerException("authentication");
    }

    boolean isAdmin = this.hasRole(authentication, RoleName.ADMIN.getName());
    this.purchaseRequestService.delete(id, this.currentUser(authentication), isAdmin);
    return ResponseEntity.noContent().build();
  }
}
