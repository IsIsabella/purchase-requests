package bft.purchaserequests.client;

import bft.purchaserequests.common.PurchaseRequest;
import bft.purchaserequests.common.RoleName;
import bft.purchaserequests.common.StatusName;
import bft.purchaserequests.common.User;
import bft.purchaserequests.server.dto.PurchaseRequestCreateRequest;
import bft.purchaserequests.server.dto.PurchaseRequestUpdateRequest;
import bft.purchaserequests.server.service.PurchaseRequestService;
import bft.purchaserequests.server.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.Objects;
import javax.annotation.concurrent.NotThreadSafe;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Форма создания и редактирования заявки на закупку
 *
 * <p>Набор редактируемых полей зависит от роли текущего пользователя и
 * состояния заявки</p>
 */
@Route(value = "request", layout = MainLayout.class)
@PageTitle("Заявка")
@PermitAll
@NotThreadSafe
public class RequestFormView extends VerticalLayout implements HasUrlParameter<Integer> {

  private final PurchaseRequestService purchaseRequestService;
  private final UserService userService;
  private final TextField name = new TextField("Название");
  private final TextArea description = new TextArea("Описание");
  private final BigDecimalField amount = new BigDecimalField("Сумма");
  private final ComboBox<StatusName> status = new ComboBox<>("Статус");
  private final ComboBox<User> approvedBy = new ComboBox<>("Утвердил");
  private final TextField createdBy = new TextField("Создал");
  private final TextField createdAt = new TextField("Создана");
  private final TextField updatedAt = new TextField("Обновлена");
  private final Button saveContent = new Button("Сохранить содержимое");
  private final Button saveStatus = new Button("Сохранить статус");
  private PurchaseRequest purchaseRequest;

  /**
   * Создаёт форму заявки
   *
   * @param purchaseRequestService сервис заявок
   * @param userService            сервис пользователей
   * @throws NullPointerException если входные параметры null
   */
  public RequestFormView(PurchaseRequestService purchaseRequestService, UserService userService) {
    if (purchaseRequestService == null) {
      throw new NullPointerException("purchaseRequestService");
    }
    if (userService == null) {
      throw new NullPointerException("userService");
    }

    this.purchaseRequestService = purchaseRequestService;
    this.userService = userService;

    this.status.setItems(StatusName.values());
    this.status.setAllowCustomValue(false);

    this.approvedBy.setItems(this.userService.findAll());
    this.approvedBy.setItemLabelGenerator(User::getUsername);

    this.createdBy.setReadOnly(true);
    this.createdAt.setReadOnly(true);
    this.updatedAt.setReadOnly(true);

    this.saveContent.addClickListener(e -> this.saveContent());
    this.saveStatus.addClickListener(e -> this.saveStatus());

    Button cancel = new Button("Отмена", e ->
        this.getUI().ifPresent(ui -> ui.navigate(RequestListView.class)));

    FormLayout form = new FormLayout(
        this.name, this.description, this.amount, this.status, this.approvedBy,
        this.createdBy, this.createdAt, this.updatedAt);
    form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

    this.add(form, new HorizontalLayout(this.saveContent, this.saveStatus, cancel));
  }

  /**
   * Загружает заявку по id, либо готовит форму к созданию новой заявки, если параметр не передан
   *
   * @param event событие перехода на страницу
   * @param id    идентификатор заявки, либо null для создания новой
   * @throws IllegalArgumentException если заявка с указанным id не найдена
   */
  @Override
  public void setParameter(BeforeEvent event, @OptionalParameter Integer id) {
    if (id != null) {
      this.purchaseRequest = this.purchaseRequestService.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));
    } else {
      this.purchaseRequest = null;
    }
    this.bindFields();
    this.applyAccessRules();
  }

  /**
   * Заполняет поля формы данными текущей заявки, либо очищает их в режиме создания новой заявки
   */
  private void bindFields() {
    if (this.purchaseRequest == null) {
      this.name.clear();
      this.description.clear();
      this.amount.clear();
      this.status.setValue(StatusName.NEW);
      this.approvedBy.clear();
      this.createdBy.clear();
      this.createdAt.clear();
      this.updatedAt.clear();
      return;
    }

    this.name.setValue(
        this.purchaseRequest.getName() != null ? this.purchaseRequest.getName() : "");
    this.description.setValue(
        this.purchaseRequest.getDescription() != null ? this.purchaseRequest.getDescription() : "");
    this.amount.setValue(this.purchaseRequest.getAmount());
    if (this.purchaseRequest.getStatus() != null) {
      this.status.setValue(StatusName.valueOf(this.purchaseRequest.getStatus().getName()));
    }
    this.approvedBy.setValue(this.purchaseRequest.getApprovedBy());
    this.createdBy.setValue(this.purchaseRequest.getCreatedBy() != null
        ? this.purchaseRequest.getCreatedBy().getUsername() : "");
    this.createdAt.setValue(this.purchaseRequest.getCreatedAt() != null
        ? this.purchaseRequest.getCreatedAt().toString() : "");
    this.updatedAt.setValue(this.purchaseRequest.getUpdatedAt() != null
        ? this.purchaseRequest.getUpdatedAt().toString() : "");
  }

  /**
   * Включает/выключает поля и кнопки в зависимости от роли текущего пользователя и того, разрешено
   * ли ему редактировать именно эту заявку
   */
  private void applyAccessRules() {
    boolean isAdmin = this.hasRole(RoleName.ADMIN);
    boolean isManager = this.hasRole(RoleName.MANAGER);
    boolean isCreateMode = this.purchaseRequest == null;

    boolean isOwner = !isCreateMode
        && this.purchaseRequest.getCreatedBy() != null
        && this.purchaseRequest.getCreatedBy().getId().equals(this.getCurrentUser().getId());
    boolean isNew = isCreateMode
        || (this.purchaseRequest.getStatus() != null
        && StatusName.NEW.getName().equals(this.purchaseRequest.getStatus().getName()));

    boolean canEditContent = isAdmin || isCreateMode || (isOwner && isNew);
    boolean canChangeStatus = !isCreateMode && (isAdmin || isManager);
    boolean canReassignApprover = isAdmin && !isCreateMode;

    this.name.setReadOnly(!canEditContent);
    this.description.setReadOnly(!canEditContent);
    this.amount.setReadOnly(!canEditContent);
    this.saveContent.setVisible(canEditContent);

    this.status.setReadOnly(!canChangeStatus);
    this.saveStatus.setVisible(canChangeStatus);

    this.approvedBy.setReadOnly(!canReassignApprover);
    this.approvedBy.setVisible(!isCreateMode);
  }

  /**
   * Сохраняет содержимое заявки: создаёт новую либо обновляет существующую
   *
   * @throws IllegalArgumentException если не удалось создать или сохранить заявку
   * @throws AccessDeniedException    если отказано в доступе
   */
  private void saveContent() {
    try {
      User current = this.getCurrentUser();
      boolean isAdmin = this.hasRole(RoleName.ADMIN);
      Integer approvedById = isAdmin && this.approvedBy.getValue() != null
          ? this.approvedBy.getValue().getId()
          : null;

      if (this.purchaseRequest == null) {
        PurchaseRequestCreateRequest request = new PurchaseRequestCreateRequest(
            this.name.getValue(), this.description.getValue(), this.amount.getValue());
        PurchaseRequest saved = this.purchaseRequestService.create(request, current);
        Notification.show("Заявка создана");
        this.getUI().ifPresent(ui -> ui.navigate(RequestFormView.class, saved.getId()));
      } else {
        PurchaseRequestUpdateRequest request = new PurchaseRequestUpdateRequest(
            this.name.getValue(), this.description.getValue(), this.amount.getValue(),
            approvedById);
        this.purchaseRequest = this.purchaseRequestService.update(this.purchaseRequest.getId(),
            request, current, isAdmin);
        Notification.show("Изменения сохранены");
        this.bindFields();
        this.applyAccessRules();
      }
    } catch (AccessDeniedException | IllegalArgumentException ex) {
      this.showError(ex.getMessage());
    }
  }

  /**
   * Сохраняет новый статус заявки
   */
  private void saveStatus() {
    if (this.purchaseRequest == null) {
      return;
    }
    try {
      User current = this.getCurrentUser();
      this.purchaseRequest = this.purchaseRequestService.changeStatus(
          this.purchaseRequest.getId(), this.status.getValue().getName(), current);
      Notification.show("Статус обновлён");
      this.bindFields();
      this.applyAccessRules();
    } catch (AccessDeniedException | IllegalArgumentException ex) {
      this.showError(ex.getMessage());
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
