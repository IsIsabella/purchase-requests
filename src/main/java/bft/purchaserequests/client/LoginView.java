package bft.purchaserequests.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayoutVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Страница входа в систему
 *
 * <p>Форма отправляется стандартным HTML POST на {@code /login},
 * обрабатывает его Spring Security.</p>
 */
@Route("login")
@PageTitle("Вход")
@AnonymousAllowed
@NotThreadSafe
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

  private final LoginForm login = new LoginForm();

  /**
   * Создаёт страницу входа
   */
  public LoginView() {
    this.setSizeFull();
    this.setAlignItems(Alignment.CENTER);
    this.setJustifyContentMode(JustifyContentMode.CENTER);

    this.login.setI18n(this.createRussianI18n());
    // функции восстановления пароля нет - не показываем ссылку
    this.login.setForgotPasswordButtonVisible(false);
    this.login.setAction("login");

    Button registerLink = new Button("Регистрация", e ->
        this.getUI().ifPresent(ui -> ui.navigate("register")));

    this.add(this.login, registerLink);
  }

  /**
   * Строит русскую локализацию для встроенного компонента {@link LoginForm}
   *
   * @return готовый объект локализации
   */
  private LoginI18n createRussianI18n() {
    LoginI18n i18n = LoginI18n.createDefault();

    i18n.setHeader(new LoginI18n.Header());
    i18n.getHeader().setTitle("Вход в систему");
    i18n.getHeader().setDescription("Заявки на закупку");

    LoginI18n.Form form = i18n.getForm();
    form.setTitle("Вход");
    form.setUsername("Логин");
    form.setPassword("Пароль");
    form.setSubmit("Войти");

    LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
    errorMessage.setTitle("Неверный логин или пароль");
    errorMessage.setMessage("Проверьте правильность введённых данных и попробуйте снова");
    errorMessage.setUsername("Логин");
    errorMessage.setPassword("Пароль");

    return i18n;
  }

  /**
   * Показывает сообщение об ошибке входа, если Spring Security перенаправил сюда с параметром
   * ошибки
   *
   * @param event событие перехода на страницу
   */
  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
      this.login.setError(true);
    }
  }
}