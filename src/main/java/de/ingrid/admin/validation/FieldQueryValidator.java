package de.ingrid.admin.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.admin.command.FieldQueryCommandObject;

@Service
public class FieldQueryValidator extends AbstractValidator<FieldQueryCommandObject> {

    public final Errors validate(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "busUrl");
        rejectIfEmptyOrWhitespace(errors, "regex");
        rejectIfEmptyOrWhitespace(errors, "key");
        rejectIfEmptyOrWhitespace(errors, "value");
        rejectIfEmptyOrWhitespace(errors, "option");

        return errors;
    }
}
