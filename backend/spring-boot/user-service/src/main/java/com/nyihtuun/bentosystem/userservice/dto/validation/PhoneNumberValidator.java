package com.nyihtuun.bentosystem.userservice.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    // E.164 format (recommended)
    // Examples:
    // +819012345678
    // +12025550123
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+[1-9]\\d{7,14}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // let @NotBlank handle null/empty
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}