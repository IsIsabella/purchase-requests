package bft.purchaserequests.client;

import bft.purchaserequests.common.RoleName;
import bft.purchaserequests.server.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import javax.annotation.concurrent.NotThreadSafe;


/**
 * Страница регистрации нового пользователя
 */
@Route("register")
@PageTitle("Регистрация")
@AnonymousAllowed
@NotThreadSafe
public class RegisterView extends VerticalLayout {

  /**
   * Создаёт страницу регистрации
   *
   * @param userService сервис пользователей, выполняющий саму регистрацию
   * @throws NullPointerException если входной параметр null
   */
  public RegisterView(UserService userService) {
    if (userService == null) {
      throw new NullPointerException("userService");
    }

    this.setSizeFull();
    this.setAlignItems(Alignment.CENTER);
    this.setJustifyContentMode(JustifyContentMode.CENTER);

    TextField username = new TextField("Логин");
    username.setWidthFull();

    PasswordField password = new PasswordField("Пароль");
    password.setWidthFull();

    TextField fullName = new TextField("ФИО");
    fullName.setWidthFull();

    ComboBox<RoleName> role = new ComboBox<>("Роль");
    role.setItems(RoleName.values());
    role.setValue(RoleName.EMPLOYEE);
    role.setAllowCustomValue(false);
    role.setWidthFull();

    Button register = new Button("Зарегистрироваться", e -> {
      try {
        userService.register(
            username.getValue(),
            password.getValue(),
            fullName.getValue(),
            role.getValue().getName());

        Notification.show("Регистрация успешна!");
        this.getUI().ifPresent(ui -> ui.navigate("login"));
      } catch (IllegalArgumentException ex) {
        Notification notification = Notification.show("Ошибка: " + ex.getMessage());
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });

    Button toLogin = new Button("Войти", e ->
        this.getUI().ifPresent(ui -> ui.navigate("login")));

    FormLayout form = new FormLayout();
    form.setWidth("400px");
    form.add(username, password, fullName, role, register, toLogin);
    form.getStyle().set("margin", "0 auto");

    this.add(form);
  }
}
