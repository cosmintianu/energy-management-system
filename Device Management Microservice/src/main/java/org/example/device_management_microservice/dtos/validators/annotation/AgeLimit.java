package org.example.device_management_microservice.dtos.validators.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.device_management_microservice.dtos.validators.AgeValidator;

import java.lang.annotation.*;

@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = AgeValidator.class)
public @interface AgeLimit {
    int value() default 18;                        // min age
    String message() default "the age must be at least {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
