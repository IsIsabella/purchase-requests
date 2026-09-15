package bft.purchaserequests.server.dto;


import bft.purchaserequests.common.PurchaseRequest;
import java.math.BigDecimal;
import javax.annotation.concurrent.Immutable;

/**
 * Данные для создания новой заявки на закупку, приходящие от клиента
 *
 * <p>Используется вместо передачи JPA-сущности {@link PurchaseRequest}.</p>
 *
 * @param name        название заявки, не должно быть пустым
 * @param description описание заявки, может бытьnull
 * @param amount      сумма закупки, должна быть положительным числом
 */
@Immutable
public record PurchaseRequestCreateRequest(String name, String description, BigDecimal amount) {

}
