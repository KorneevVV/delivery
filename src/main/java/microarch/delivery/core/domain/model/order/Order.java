package microarch.delivery.core.domain.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.domain.model.shared.Volume;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Order extends Aggregate<UUID> {

    @Embedded
    private Location location;

    @Embedded
    private Volume volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    private Order(UUID id, Location location, Volume volume, OrderStatus status) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = status;
    }

    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(volume, "volume");

        var err = Guard.againstNullOrEmpty(id, "id");
        if (err != null) {
            return Result.failure(err);
        }

        return Result.success(new Order(id, location, volume, OrderStatus.CREATED));
    }

    public UnitResult<Error> assign() {
        if (this.status != OrderStatus.CREATED) {
            return UnitResult.failure(Errors.orderCannotBeAssigned());
        }

        this.status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (this.status != OrderStatus.ASSIGNED) {
            return UnitResult.failure(Errors.orderCannotBeCompleted());
        }

        this.status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    public static class Errors {

        public static Error orderCannotBeAssigned() {
            return Error.of(
                    "order.cannot.be.assigned",
                    "Order can be assigned only when it is created");
        }

        public static Error orderCannotBeCompleted() {
            return Error.of(
                    "order.cannot.be.completed",
                    "Order can be completed only when it is assigned");
        }
    }
}
