package ca.corefacility.bioinformatics.irida.service;

import org.springframework.security.access.prepost.PreAuthorize;

import ca.corefacility.bioinformatics.irida.model.user.PasswordReset;

/**
 * Service for managing {@link PasswordReset} entities.
 * 
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@PreAuthorize("permitAll")
public interface PasswordResetService extends CRUDService<String, PasswordReset> {
}
