package ca.corefacility.bioinformatics.irida.processing;

/**
 * Exception thrown when a {@link FileProcessor} fails to complete execution.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * 
 */
public class FileProcessorException extends RuntimeException {

	private static final long serialVersionUID = 7389408065012958110L;

	public FileProcessorException(String message) {
		super(message);
	}
}
