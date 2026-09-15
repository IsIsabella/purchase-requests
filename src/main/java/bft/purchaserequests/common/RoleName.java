package bft.purchaserequests.common;


import javax.annotation.concurrent.Immutable;

/**
 * Типобезопасные значения справочника ролей
 */
@Immutable
public enum RoleName {
  ADMIN("ADMIN", "Администратор"),
  MANAGER("MANAGER", "Менеджер"),
  EMPLOYEE("EMPLOYEE", "Сотрудник");

  private final String name;
  private final String transcript;

  /**
   * Создаёт значение справочника роли
   *
   * @param name       название роли, используемое в БД
   * @param transcript русскоязычное название роли
   */
  RoleName(String name, String transcript) {
    this.name = name;
    this.transcript = transcript;
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
   * Возвращает русскоязычное название роли
   *
   * @return русскоязычное название роли
   */
  public String getTranscript() {
    return this.transcript;
  }

  /**
   * Возвращает русскоязычное название роли
   *
   * @return русскоязычное название роли
   */
  @Override
  public String toString() {
    return this.transcript;
  }
}
