package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.order.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository {

    Order add(Order order);

    Order update(Order order);

    Result<Order, Error> getById(UUID id);

    Result<Order, Error> getAnyCreated();

    List<Order> getAllAssigned();
}
