package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.shared.Volume;
import microarch.delivery.core.domain.model.shared.Location;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Assignment extends BaseEntity<UUID> {

    private static final int COMPLETION_DISTANCE = 1;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Embedded
    private Volume volume;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status;

    private Assignment(UUID id, UUID orderId, Volume volume, Location location, AssignmentStatus status) {
        super(id);
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = status;
    }

    static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {
        Objects.requireNonNull(volume, "volume");
        Objects.requireNonNull(location, "location");

        var err = Guard.againstNullOrEmpty(orderId, "orderId");
        if (err != null) {
            return Result.failure(err);
        }

        return Result.success(
                new Assignment(UUID.randomUUID(), orderId, volume, location, AssignmentStatus.ASSIGNED));
    }

    UnitResult<Error> complete(Location courierLocation) {
        Objects.requireNonNull(courierLocation, "courierLocation");

        if (this.status == AssignmentStatus.COMPLETED) {
            return UnitResult.failure(Errors.assignmentAlreadyCompleted());
        }

        if (courierLocation.distanceTo(this.location) > COMPLETION_DISTANCE) {
            return UnitResult.failure(Errors.courierIsTooFar());
        }

        this.status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

    public static class Errors {

        public static Error courierIsTooFar() {
            return Error.of(
                    "assignment.courier.is.too.far",
                    "Courier must be one cell or closer to complete assignment");
        }

        public static Error assignmentAlreadyCompleted() {
            return Error.of(
                    "assignment.already.completed",
                    "Assignment is already completed");
        }
    }
}
