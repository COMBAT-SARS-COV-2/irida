package ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientHandlerException;

import ca.corefacility.bioinformatics.irida.exceptions.UploadConnectionException;
import ca.corefacility.bioinformatics.irida.exceptions.UploadException;
import ca.corefacility.bioinformatics.irida.model.upload.UploadResult;
import ca.corefacility.bioinformatics.irida.model.upload.UploadSample;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyAccountEmail;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyProjectName;
import ca.corefacility.bioinformatics.irida.pipeline.upload.UploadWorker;

/**
 * Class for performing the actual work of uploading files to Galaxy.
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
public class GalaxyUploadWorker extends Thread implements UploadWorker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(GalaxyUploadWorker.class);

	private GalaxyAPI galaxyAPI;
	
	private List<UploadSample> samples;
	private GalaxyProjectName dataLocation;
	private GalaxyAccountEmail userName;
	
	private UploadFinishedRunner finishedRunner = DEFAULT_FINISHED;
	private UploadExceptionRunner exceptionRunner = DEFAULT_EXCEPTION;
	
	private UploadResult uploadResult = null;
	
	/**
	 * Constructs a new GalaxyUploadWorker for performing the upload of files to Galaxy within a new Thread.
	 * @param galaxyAPI  The GalaxyAPI to connect to an instance of Galaxy.
	 * @param samples  The list of samples to upload.
	 * @param dataLocation  The location of the Galaxy project to upload into.
	 * @param userName  The user to upload the data as.
	 */
	public GalaxyUploadWorker(GalaxyAPI galaxyAPI, List<UploadSample> samples,
			GalaxyProjectName dataLocation, GalaxyAccountEmail userName) {
		checkNotNull(galaxyAPI, "galaxyAPI is null");
		checkNotNull(samples, "samples is null");
		checkNotNull(dataLocation, "dataLocation is null");
		checkNotNull(userName, "userName is null");
		
		this.galaxyAPI = galaxyAPI;
		this.samples = samples;
		this.dataLocation = dataLocation;
		this.userName = userName;
	}
	
	@Override
	public void run() {
		try {
			uploadResult = uploadSamples(samples, dataLocation, userName);
			finishedRunner.finish(uploadResult);
		} catch (ConstraintViolationException e) {
			exceptionRunner.exception(new UploadException(e));
		} catch (UploadException e) {
			exceptionRunner.exception(e);
		} catch (Exception e) {
			// handle any remaining exceptions
			exceptionRunner.exception(new UploadException(e));
		}
	}
	
	/**
	 * Uploads the given list of samples to the passed data location name with
	 * the passed user.
	 * 
	 * @param samples
	 *            The set of samples to upload.
	 * @param dataLocation
	 *            The name of the data location to upload to.
	 * @param userName
	 *            The name of the user who should own the files.
	 * @return An UploadResult containing information about the location of the
	 *         uploaded files.
	 * @throws UploadException
	 *             If an error occurred.
	 * @throws ConstraintViolationException
	 *             If the samples, dataLocation or userName are invalid.
	 */
	private UploadResult uploadSamples(List<UploadSample> samples,
			GalaxyProjectName dataLocation,
			GalaxyAccountEmail userName) throws UploadException,
			ConstraintViolationException {

		logger.debug("Uploading samples to Galaxy Library " + dataLocation
				+ ", userEmail=" + userName + ", samples=" + samples);

		try {
			return galaxyAPI.uploadSamples(samples, dataLocation, userName);
		} catch (ClientHandlerException e) {
			throw new UploadConnectionException(
					"Could not upload to Galaxy", e);
		}
	}

	@Override
	public void runOnUploadFinished(UploadFinishedRunner finishedRunner) {
		checkNotNull(finishedRunner, "finishedRunner is null");
		this.finishedRunner = finishedRunner;
	}

	@Override
	public void runOnUploadException(UploadExceptionRunner exceptionRunner) {
		checkNotNull(exceptionRunner, "exceptionRunner is null");
		this.exceptionRunner = exceptionRunner;
	}

	@Override
	public UploadResult getUploadResult() {
		return uploadResult;
	}
}
