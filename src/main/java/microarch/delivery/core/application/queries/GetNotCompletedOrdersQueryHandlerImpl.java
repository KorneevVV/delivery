package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {

    private final OrderRepository orderRepository;

    public GetNotCompletedOrdersQueryHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Result<GetNotCompletedOrdersResponse, Error> handle(GetNotCompletedOrdersQuery query) {
        var orders = orderRepository.getAllNotCompleted().stream().map(this::mapToDto).toList();

        return Result.success(new GetNotCompletedOrdersResponse(orders));
    }

    private OrderDto mapToDto(Order order) {
        return new OrderDto(order.getId(), mapToDto(order.getLocation()));
    }

    private LocationDto mapToDto(Location location) {
        return new LocationDto(location.getX(), location.getY());
    }
}
