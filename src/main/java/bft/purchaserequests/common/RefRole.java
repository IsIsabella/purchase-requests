package bft.purchaserequests.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Сущность роли пользователя
 */
@Entity
@Table(name = "ref_role")
@NotThreadSafe
public class RefRole {

  /**
   * Уникальный идентификатор роли
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  /**
   * Название роли
   */
  @Column(nullable = false, unique = true)
  private String name;
  /**
   * Русскоязычное название роли
   */
  @Column(nullable = false)
  private String transcript;

  /**
   * Возвращает идентификатор роли
   *
   * @return идентификатор
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * Возвращает название роли
   *
   * @return название роли
   */
  public String getName() {
    return this.name;
  }

  /**
   * Устанавливает название роли
   *
   * @param name название роли
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Возвращает русскоязычное название роли
   *
   * @return русскоязычное название роли
   */
  public String getTranscript() {
    return this.transcript;
  }

  /**
   * Устанавливает русскоязычное название роли
   *
   * @param transcript русскоязычное название роли
   */
  public void setTranscript(String transcript) {
    this.transcript = transcript;
  }
}
