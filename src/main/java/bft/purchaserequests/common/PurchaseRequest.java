package bft.purchaserequests.common;


import jakarta.persistence.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность заявки на закупку
 */
@Entity
@Table(name = "purchase_request")
@NotThreadSafe
public class PurchaseRequest {
    /**
     * Уникальный идентификатор заявки
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * Наименование заявки
     */
    @Column(nullable = false)
    private String name;
    /**
     * Описание заявки
     */
    @Column
    private String description;
    /**
     * Сумма заявки
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    /**
     * Текущий статус заявки - ВК на запись справочника {@code ref_status})
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private RefStatus status;
    /**
     * Пользователь, создавший заявку - ВК на запись в {@code users})
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    /**
     * Пользователь, утвердивший заявку или изменивший её статус - ВК на запись в {@code users})
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    /**
     * Дата и время создания заявки - выставляется автоматически
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * Дата и время последнего изменения заявки - обновляется автоматически
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Выставляет дату создания перед первым сохранением новой заявки
     */
    @PrePersist
    protected void createDate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Обновляет дату последнего изменения перед каждым обновлением заявки
     */
    @PreUpdate
    protected void updateDate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Возвращает идентификатор заявки
     *
     * @return идентификатор, либо null, если заявка ещё не сохранена
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Возвращает наименование заявки
     *
     * @return наименование
     */
    public String getName() {
        return this.name;
    }

    /**
     * Устанавливает наименование заявки
     *
     * @param name наименование
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает описание заявки
     *
     * @return описание
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Устанавливает описание заявки
     *
     * @param description описание
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает сумму заявки
     *
     * @return сумма
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * Устанавливает сумму заявки
     *
     * @param amount сумма
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Возвращает текущий статус заявки
     *
     * @return статус
     */
    public RefStatus getStatus() {
        return this.status;
    }

    /**
     * Устанавливает статус заявки
     *
     * @param status статус
     */
    public void setStatus(RefStatus status) {
        this.status = status;
    }

    /**
     * Возвращает пользователя, создавшего заявку
     *
     * @return создатель заявки
     */
    public User getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Устанавливает пользователя, создавшего заявку
     *
     * @param createdBy создатель заявки
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Возвращает пользователя, утвердившего заявку/изменившего статус
     *
     * @return утвердивший пользователь, либо null
     */
    public User getApprovedBy() {
        return this.approvedBy;
    }

    /**
     * Устанавливает пользователя, утвердившего заявку/изменившего статус
     *
     * @param approvedBy утвердивший пользователь
     */
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    /**
     * Возвращает дату создания заявки
     *
     * @return дата создания
     */
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Возвращает дату последнего изменения заявки
     *
     * @return дата изменения, либо null, если заявку ещё не меняли
     */
    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
