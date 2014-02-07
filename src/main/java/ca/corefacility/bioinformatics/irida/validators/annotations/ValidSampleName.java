package ca.corefacility.bioinformatics.irida.validators.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

/**
 * A collection of patterns that validate the name of a sample.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * 
 */
@Pattern.List({ @Pattern(regexp = "^[^\\']+$", message = "{irida.name.invalid.single.quote}"),
		@Pattern(regexp = "^[^\\.]+$", message = "{irida.name.invalid.period}"),
		@Pattern(regexp = "^[^ ]+$", message = "{irida.name.invalid.space}") })
@ValidProjectName
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface ValidSampleName {

	public static char[] BLACKLISTED_CHARACTERS = { '\'', '.', ' ' };

	String message() default "{irida.name.invalid.default}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
