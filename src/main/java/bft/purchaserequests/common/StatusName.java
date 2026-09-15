package bft.purchaserequests.common;


import javax.annotation.concurrent.Immutable;

/**
 * Типобезопасные значения справочника статусов заявок
 */
@Immutable
public enum StatusName {
    NEW("NEW", "Новая"),
    PENDING_APPROVAL("PENDING_APPROVAL", "На согласовании"),
    APPROVED("APPROVED", "Одобрена"),
    REJECTED("REJECTED", "Отклонена"),
    COMPLETED("COMPLETED", "Завершена");

    private final String name;
    private final String transcript;

    /**
     * Создаёт значение справочника статуса
     *
     * @param name       название, используемое в БД
     * @param transcript русскоязычное название статуса
     */
    StatusName(String name, String transcript) {
        this.name = name;
        this.transcript = transcript;
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
     * Возвращает русскоязычное название статуса
     *
     * @return русскоязычное название статуса
     */
    public String getTranscript() {
        return this.transcript;
    }

    /**
     * Возвращает русскоязычное название статуса
     *
     * @return русскоязычное название статуса
     */
    @Override
    public String toString() {
        return this.transcript;
    }
}
