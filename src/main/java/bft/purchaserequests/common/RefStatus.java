package bft.purchaserequests.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Сущность статуса заявки
 */
@Entity
@Table(name = "ref_status")
@NotThreadSafe
public class RefStatus {

  /**
   * Уникальный идентификатор статуса
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  /**
   * Название статуса
   */
  @Column(nullable = false, unique = true)
  private String name;
  /**
   * Русскоязычное название статуса
   */
  @Column(nullable = false)
  private String transcript;

  /**
   * Возвращает идентификатор статуса
   *
   * @return идентификатор
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * Возвращает название статуса
   *
   * @return название статуса
   */
  public String getName() {
    return this.name;
  }

  /**
   * Устанавливает название статуса
   *
   * @param name название статуса
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Возвращает русскоязычное название статуса
   *
   * @return русскоязычное название статуса
   */
  public String getTranscript() {
    return this.transcript;
  }

  /**
   * Устанавливает русскоязычное название статуса
   *
   * @param transcript русскоязычное название статуса
   */
  public void setTranscript(String transcript) {
    this.transcript = transcript;
  }
}
