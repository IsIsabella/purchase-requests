package bft.purchaserequests.server.service;

import bft.purchaserequests.common.RefRole;
import bft.purchaserequests.common.User;
import bft.purchaserequests.server.repository.RefRoleRepository;
import bft.purchaserequests.server.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с пользователями: получение, регистрация и безопасное хеширование паролей
 *
 * <p>Пароль в открытом виде не хранится и не передаётся, хеширование происходит через
 * уже настроенный {@link PasswordEncoder}</p> Реализует паттерн singleton
 */
@Service
@ThreadSafe
public class UserService {

  private final UserRepository userRepository;
  private final RefRoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Создаёт сервис пользователей
   *
   * @param userRepository  репозиторий пользователей
   * @param roleRepository  репозиторий справочника ролей
   * @param passwordEncoder кодировщик паролей
   * @throws NullPointerException если какой-то из входных параметров null
   */
  public UserService(
      UserRepository userRepository,
      RefRoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    if (userRepository == null) {
      throw new NullPointerException("userRepository");
    }
    if (roleRepository == null) {
      throw new NullPointerException("roleRepository");
    }
    if (passwordEncoder == null) {
      throw new NullPointerException("passwordEncoder");
    }

    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Возвращает всех пользователей
   *
   * @return список пользователей
   */
  public List<User> findAll() {
    return this.userRepository.findAll();
  }

  /**
   * Находит пользователя по имени
   *
   * @param username имя пользователя
   * @return найденный пользователь, либо пустой {@link Optional}
   */
  public Optional<User> findByUsername(String username) {
    return this.userRepository.findByUsername(username);
  }

  /**
   * Находит пользователя по идентификатору
   *
   * @param id идентификатор пользователя
   * @return найденный пользователь, либо пустой {@link Optional}
   */
  public Optional<User> findById(Integer id) {
    return this.userRepository.findById(id);
  }

  /**
   * Устанавливает пользователю хеш пароля
   *
   * @param user        пользователь, которому назначается пароль
   * @param rawPassword исходный пароль в открытом виде
   * @throws IllegalArgumentException если пароль пустой
   */
  public void setRawPassword(User user, String rawPassword) {
    if (rawPassword.isEmpty()) {
      throw new IllegalArgumentException("Пароль не может быть пустым");
    }
    user.setPasswordHash(this.passwordEncoder.encode(rawPassword));
  }

  /**
   * Регистрирует нового пользователя с указанной ролью
   *
   * @param username    логин нового пользователя
   * @param rawPassword пароль в открытом виде (хешируется автоматически)
   * @param fullName    полное имя
   * @param roleName    название роли из справочника {@code ref_role}
   * @throws IllegalArgumentException если логин пустой, пользователь с таким логином уже
   *                                  существует, пароль пустой, либо роль с таким названием не
   *                                  найдена в справочнике
   */
  public void register(String username, String rawPassword,
      @Nullable String fullName, String roleName) {
    if (username.isEmpty()) {
      throw new IllegalArgumentException("Логин не может быть пустым");
    }
    if (rawPassword.isEmpty()) {
      throw new IllegalArgumentException("Пароль не может быть пустым");
    }
    if (roleName.isEmpty()) {
      throw new IllegalArgumentException("Роль не может быть пустой");
    }
    if (this.userRepository.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("Пользователь с таким именем уже существует");
    }

    RefRole role = this.roleRepository.findByName(roleName)
        .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));

    User user = new User();
    user.setUsername(username);
    user.setFullName(fullName);
    user.setRole(role);
    this.setRawPassword(user, rawPassword);

    this.userRepository.save(user);
  }
}
