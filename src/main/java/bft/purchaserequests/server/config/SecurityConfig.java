package bft.purchaserequests.server.config;

import bft.purchaserequests.client.LoginView;
import bft.purchaserequests.server.repository.UserRepository;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения
 *
 * <p>Настраивает:</p>
 * <ul>
 *     <li>аутентификацию пользователей через {@link UserRepository};</li>
 *     <li>BCrypt-хеширование паролей;</li>
 *     <li>применяет {@link VaadinSecurityConfigurer};</li>
 *     <li>доступ к REST API только для аутентифицированных пользователей;</li>
 *     <li>авторизацию через {@code @PreAuthorize} - включена аннотацией
 *     {@link EnableMethodSecurity} на этом классе.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
@ThreadSafe
public class SecurityConfig {

  private final UserRepository userRepository;

  /**
   * Создаёт конфигурацию безопасности.
   *
   * @param userRepository репозиторий пользователей
   * @throws NullPointerException если входной параметр null
   */
  public SecurityConfig(UserRepository userRepository) {
    if (userRepository == null) {
      throw new NullPointerException("userRepository");
    }

    this.userRepository = userRepository;
  }

  /**
   * Создаёт кодировщик паролей
   *
   * @return BCrypt-кодировщик
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Настраивает загрузку пользователя для Spring Security
   *
   * @return сервис загрузки пользователей для Spring Security
   * @throws UsernameNotFoundException если пользователь с таким именем не найден в БД
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return username -> this.userRepository.findByUsername(username)
        .map(user -> User.withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole().getName())
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("Не найден пользователь: " + username));
  }

  /**
   * Настраивает цепочку фильтров Spring Security и интеграцию с Vaadin Security
   *
   * @param http объект конфигурации HTTP-безопасности
   * @return цепочка фильтров безопасности
   * @throws NullPointerException если входной параметр null
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    if (http == null) {
      throw new NullPointerException("http");
    }

    http.authorizeHttpRequests(
        auth -> auth.requestMatchers("/rest/**").authenticated());
    http.with(VaadinSecurityConfigurer.vaadin(),
        configurer -> configurer.loginView(LoginView.class));
    return http.build();
  }
}
