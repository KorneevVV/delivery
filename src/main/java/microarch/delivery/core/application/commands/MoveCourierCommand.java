package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.shared.Location;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveCourierCommand {

    private final UUID courierId;
    private final Location location;

    public static Result<MoveCourierCommand, Error> create(UUID courierId, Location location) {
        var err = Guard.againstNullOrEmpty(courierId, "courierId");
        if (err != null) {
            return Result.failure(err);
        }

        if (location == null) {
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        }

        return Result.success(new MoveCourierCommand(courierId, location));
    }
}
