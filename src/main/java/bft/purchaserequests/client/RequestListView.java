package bft.purchaserequests.client;

import bft.purchaserequests.common.PurchaseRequest;
import bft.purchaserequests.common.RoleName;
import bft.purchaserequests.common.StatusName;
import bft.purchaserequests.common.User;
import bft.purchaserequests.server.service.PurchaseRequestService;
import bft.purchaserequests.server.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import javax.annotation.concurrent.NotThreadSafe;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Список заявок на закупку
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Список заявок")
@PermitAll
@NotThreadSafe
public class RequestListView extends VerticalLayout {

  private final Grid<PurchaseRequest> grid = new Grid<>(PurchaseRequest.class, false);
  private final PurchaseRequestService purchaseRequestService;
  private final UserService userService;

  /**
   * Создаёт список заявок
   *
   * @param purchaseRequestService сервис заявок
   * @param userService            сервис пользователей
   * @throws NullPointerException если входные параметры null
   */
  public RequestListView(PurchaseRequestService purchaseRequestService, UserService userService) {
    if (purchaseRequestService == null) {
      throw new NullPointerException("purchaseRequestService");
    }
    if (userService == null) {
      throw new NullPointerException("userService");
    }

    this.purchaseRequestService = purchaseRequestService;
    this.userService = userService;

    this.grid.addColumn(PurchaseRequest::getId).setHeader("ID").setWidth("70px");
    this.grid.addColumn(PurchaseRequest::getName).setHeader("Название");
    this.grid.addColumn(
            request -> request.getAmount() != null ? request.getAmount().toString() : "")
        .setHeader("Сумма");
    this.grid.addColumn(
            request -> request.getStatus() != null ? request.getStatus().getTranscript() : "")
        .setHeader("Статус");
    this.grid.addColumn(
            request -> request.getCreatedBy() != null ? request.getCreatedBy().getUsername() : "")
        .setHeader("Создал");
    this.grid.addColumn(
            request -> request.getApprovedBy() != null ? request.getApprovedBy().getUsername() : "—")
        .setHeader("Утвердил");
    this.grid.addColumn(PurchaseRequest::getCreatedAt).setHeader("Создана");

    this.grid.addComponentColumn(this::createActionsColumn).setHeader("Действия");

    Button addNew = new Button("Новая заявка", e ->
        this.getUI().ifPresent(ui -> ui.navigate(RequestFormView.class)));

    this.add(addNew, this.grid);
    this.setSizeFull();
    this.refresh();
  }

  /**
   * Строит колонку с кнопками действий для конкретной строки таблицы
   *
   * @param request заявка, для которой строится колонка
   * @return layout с кнопками действий
   */
  private HorizontalLayout createActionsColumn(PurchaseRequest request) {
    Button edit = new Button("Изменить", e ->
        this.getUI().ifPresent(ui -> ui.navigate(RequestFormView.class, request.getId())));

    HorizontalLayout actions = new HorizontalLayout(edit);

    if (this.canDelete(request)) {
      Button delete = new Button("Удалить", e -> this.deleteRequest(request));
      actions.add(delete);
    }

    return actions;
  }

  /**
   * Проверяет, стоит ли показывать кнопку "Удалить" для этой заявки
   *
   * @param request заявка, для которой проверяется право на удаление
   * @return true, если кнопку стоит показать
   */
  private boolean canDelete(PurchaseRequest request) {
    if (this.hasRole(RoleName.ADMIN)) {
      return true;
    }
    User current = this.getCurrentUser();
    boolean isOwner = request.getCreatedBy() != null
        && request.getCreatedBy().getId().equals(current.getId());
    boolean isNew = request.getStatus() != null
        && StatusName.NEW.getName().equals(request.getStatus().getName());
    return isOwner && isNew;
  }

  /**
   * Удаляет заявку и обновляет таблицу; при отказе сервера показывает уведомление
   *
   * @param request заявка для удаления
   */
  private void deleteRequest(PurchaseRequest request) {
    try {
      User current = this.getCurrentUser();
      boolean isAdmin = this.hasRole(RoleName.ADMIN);
      this.purchaseRequestService.delete(request.getId(), current, isAdmin);
      this.refresh();
      Notification.show("Заявка удалена");
    } catch (AccessDeniedException ex) {
      this.showError("Недостаточно прав для удаления заявки");
    } catch (IllegalArgumentException ex) {
      this.showError("Заявка не найдена (возможно, уже удалена): " + ex.getMessage());
    }
  }

  /**
   * Перезагружает список заявок с учётом роли текущего пользователя
   */
  private void refresh() {
    User current = this.getCurrentUser();
    if (this.hasRole(RoleName.ADMIN) || this.hasRole(RoleName.MANAGER)) {
      this.grid.setItems(this.purchaseRequestService.findAll());
    } else {
      this.grid.setItems(this.purchaseRequestService.findByCurrentUser(current));
    }
  }

  /**
   * Обработчик ошибок
   */
  private void showError(String message) {
    Notification notification = Notification.show(message);
    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
  }

  /**
   * Получение текущего пользователя
   *
   * @throws IllegalArgumentException если пользовательн не найден
   */
  private User getCurrentUser() {
    String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
        .getName();
    return this.userService.findByUsername(username)
        .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
  }

  private boolean hasRole(RoleName role) {
    return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
        .getAuthorities().stream()
        .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_" + role.getName()));
  }
}
