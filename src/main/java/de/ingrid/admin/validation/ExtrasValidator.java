package de.ingrid.admin.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.admin.object.Extras;

@Service
public class ExtrasValidator extends AbstractValidator<Extras> {

    public final Errors validate(final Errors errors) {

        return errors;
    }
}
