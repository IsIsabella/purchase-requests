package bft.purchaserequests.server.dto;


import java.math.BigDecimal;
import javax.annotation.concurrent.Immutable;

/**
 * Данные для обновления содержимого существующей заявки
 *
 * @param name         новое название заявки
 * @param description  новое описание заявки
 * @param amount       новая сумма заявки
 * @param approvedById идентификатор пользователя, которого нужно назначить утверждающим
 *                     (null, если менять не нужно)
 */
@Immutable
public record PurchaseRequestUpdateRequest(String name, String description, BigDecimal amount,
                                           Integer approvedById) {

}
