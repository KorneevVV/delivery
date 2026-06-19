package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;

public final class AssignOrderCommand {

    private AssignOrderCommand() {
    }

    public static Result<AssignOrderCommand, Error> create() {
        return Result.success(new AssignOrderCommand());
    }
}
