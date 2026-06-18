package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class DispatchService {

    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        if (order == null) {
            return Result.failure(GeneralErrors.valueIsRequired("order"));
        }

        if (couriers == null || couriers.isEmpty()) {
            return Result.failure(GeneralErrors.valueIsRequired("couriers"));
        }

        if (couriers.stream().anyMatch(Objects::isNull)) {
            return Result.failure(GeneralErrors.valueIsRequired("courier"));
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            return Result.failure(Errors.orderMustBeCreated());
        }

        Courier winner = null;
        int winnerDistance = Integer.MAX_VALUE;

        for (var courier : couriers) {
            if (!courier.canTake(order.getVolume())) {
                continue;
            }

            var distance = courier.getLocation().distanceTo(order.getLocation());
            if (distance < winnerDistance) {
                winner = courier;
                winnerDistance = distance;
            }
        }

        if (winner == null) {
            return Result.failure(Errors.courierNotFound());
        }

        var assignmentResult = winner.assign(order.getId(), order.getVolume(), order.getLocation());
        if (assignmentResult.isFailure()) {
            return Result.failure(assignmentResult.getError());
        }

        var orderAssignResult = order.assign();
        if (orderAssignResult.isFailure()) {
            return Result.failure(orderAssignResult.getError());
        }

        return Result.success(winner);
    }

    public static class Errors {

        public static Error orderMustBeCreated() {
            return Error.of(
                    "dispatch.order.must.be.created",
                    "Order must be created before dispatch");
        }

        public static Error courierNotFound() {
            return Error.of(
                    "dispatch.courier.not.found",
                    "Courier available for dispatch was not found");
        }
    }
}
