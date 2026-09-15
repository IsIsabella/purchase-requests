package bft.purchaserequests.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Сущность пользователя системы
 */
@Entity
@Table(name = "users")
@NotThreadSafe
public class User {

  /**
   * Уникальный идентификатор пользователя
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  /**
   * Логин пользователя
   */
  @Column(nullable = false, unique = true)
  private String username;
  /**
   * BCrypt-хеш пароля
   */
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;
  /**
   * Полное имя пользователя
   */
  @Column(name = "full_name")
  private String fullName;
  /**
   * Роль пользователя - ВК на запись справочника {@code ref_role}
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private RefRole role;

  /**
   * Возвращает идентификатор пользователя
   *
   * @return идентификатор
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * Возвращает имя пользователя
   *
   * @return имя пользователя
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Устанавливает имя пользователя
   *
   * @param username имя пользователя
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Возвращает хеш пароля
   *
   * @return хеш пароля
   */
  public String getPasswordHash() {
    return this.passwordHash;
  }

  /**
   * Устанавливает хеш пароля
   *
   * @param passwordHash хеш пароля
   */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /**
   * Возвращает полное имя пользователя
   *
   * @return полное имя
   */
  public String getFullName() {
    return this.fullName;
  }

  /**
   * Устанавливает полное имя пользователя
   *
   * @param fullName полное имя
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * Возвращает роль пользователя
   *
   * @return роль
   */
  public RefRole getRole() {
    return this.role;
  }

  /**
   * Устанавливает роль пользователя
   *
   * @param role роль
   */
  public void setRole(RefRole role) {
    this.role = role;
  }
}
