package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.shared.Volume;
import microarch.delivery.core.domain.model.shared.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "couriers")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Courier extends Aggregate<UUID> {

    private static final int MAX_VOLUME = 20;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Embedded
    private Location location;

    @Getter
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "max_volume", nullable = false))
    private Volume maxVolume;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courier_id", nullable = false)
    private List<Assignment> assignments = new ArrayList<>();

    private Courier(UUID id, String name, Location location, Volume maxVolume) {
        super(id);
        this.name = name;
        this.location = location;
        this.maxVolume = maxVolume;
        this.assignments = new ArrayList<>();
    }

    public static Result<Courier, Error> create(String name, Location location) {
        Objects.requireNonNull(location, "location");

        var err = Guard.againstNullOrEmpty(name, "name");
        if (err != null) {
            return Result.failure(err);
        }

        return Result.success(new Courier(
                UUID.randomUUID(),
                name,
                location,
                Volume.create(MAX_VOLUME).getValueOrThrow()));
    }

    public List<Assignment> getAssignments() {
        return List.copyOf(this.assignments);
    }

    public boolean canTake(Volume volume) {
        Objects.requireNonNull(volume, "volume");

        return activeVolume() + volume.getValue() <= this.maxVolume.getValue();
    }

    public Result<Assignment, Error> assign(UUID orderId, Volume volume, Location location) {
        Objects.requireNonNull(volume, "volume");
        Objects.requireNonNull(location, "location");

        var err = Guard.againstNullOrEmpty(orderId, "orderId");
        if (err != null) {
            return Result.failure(err);
        }

        if (hasAssignmentFor(orderId)) {
            return Result.failure(Errors.assignmentAlreadyExists());
        }

        if (!canTake(volume)) {
            return Result.failure(Errors.maxVolumeExceeded());
        }

        var assignmentResult = Assignment.create(orderId, volume, location);
        if (assignmentResult.isFailure()) {
            return Result.failure(assignmentResult.getError());
        }

        var assignment = assignmentResult.getValue();
        this.assignments.add(assignment);
        return Result.success(assignment);
    }

    public UnitResult<Error> completeAssignment(UUID orderId) {
        var err = Guard.againstNullOrEmpty(orderId, "orderId");
        if (err != null) {
            return UnitResult.failure(err);
        }

        var assignment = this.assignments.stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .findFirst();
        if (assignment.isEmpty()) {
            return UnitResult.failure(Errors.assignmentNotFound());
        }

        return assignment.get().complete(this.location);
    }

    public void moveTo(Location location) {
        this.location = Objects.requireNonNull(location, "location");
    }

    private int activeVolume() {
        return this.assignments.stream()
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED)
                .mapToInt(assignment -> assignment.getVolume().getValue())
                .sum();
    }

    private boolean hasAssignmentFor(UUID orderId) {
        return this.assignments.stream()
                .anyMatch(assignment -> assignment.getOrderId().equals(orderId));
    }

    public static class Errors {

        public static Error maxVolumeExceeded() {
            return Error.of(
                    "courier.max.volume.exceeded",
                    "Courier cannot take assignments with volume greater than max volume");
        }

        public static Error assignmentAlreadyExists() {
            return Error.of(
                    "courier.assignment.already.exists",
                    "Courier already has assignment for this order");
        }

        public static Error assignmentNotFound() {
            return Error.of(
                    "courier.assignment.not.found",
                    "Courier assignment was not found");
        }
    }
}
