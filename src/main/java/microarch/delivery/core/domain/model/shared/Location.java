package microarch.delivery.core.domain.model.shared;

import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location extends ValueObject<Location> {

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 10;
    private int x;
    private int y;

    public static Result<Location, Error> create(int x, int y) {
        var err = Guard.combine(
                Guard.againstLessThan(x, MIN_VALUE, "x"),
                Guard.againstLessThan(y, MIN_VALUE, "y"),
                Guard.againstGreaterThan(x, MAX_VALUE, "x"),
                Guard.againstGreaterThan(y, MAX_VALUE, "y"));
        if (err != null)
            return Result.failure(err);
        return Result.success(new Location(x, y));
    }

    public int distanceTo(Location location) {
        Objects.requireNonNull(location, "location");

        return Math.abs(this.x - location.x) + Math.abs(this.y - location.y);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.x, this.y);
    }
}
