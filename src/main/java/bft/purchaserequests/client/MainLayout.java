package bft.purchaserequests.client;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Общий каркас приложения: шапка с заголовком, кнопкой выхода и боковое меню навигации
 */
@PermitAll
@NotThreadSafe
public class MainLayout extends AppLayout {

  /**
   * Создаёт каркас приложения
   *
   * @param authContext контекст аутентификации Vaadin, используется для выхода из системы
   * @throws NullPointerException если входной параметр null
   */
  public MainLayout(AuthenticationContext authContext) {
    if (authContext == null) {
      throw new NullPointerException("authContext");
    }

    DrawerToggle toggle = new DrawerToggle();

    H1 title = new H1("Заявки на закупку");
    title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

    Button logout = new Button("Выйти", e -> authContext.logout());

    HorizontalLayout header = new HorizontalLayout(toggle, title, logout);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.expand(title);
    header.setWidthFull();
    header.addClassNames("py-0", "px-m");

    this.addToNavbar(header);
    Button listButton = new Button("Список заявок",
        e -> getUI().ifPresent(ui -> ui.navigate(RequestListView.class)));

    this.addToDrawer(listButton);
  }
}
