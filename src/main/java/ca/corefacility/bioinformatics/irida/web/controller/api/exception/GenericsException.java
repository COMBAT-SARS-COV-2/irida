package ca.corefacility.bioinformatics.irida.web.controller.api.exception;

/**
 * An exception that can be used when the {@link ca.corefacility.bioinformatics.irida.web.controller.api.GenericController}
 * fails to construct a resource.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class GenericsException extends RuntimeException {

	private static final long serialVersionUID = 7337013431300992746L;

	public GenericsException(String message) {
        super(message);
    }
}
