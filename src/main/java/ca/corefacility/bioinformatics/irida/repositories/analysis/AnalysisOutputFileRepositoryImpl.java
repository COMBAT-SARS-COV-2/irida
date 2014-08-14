package ca.corefacility.bioinformatics.irida.repositories.analysis;

import java.nio.file.Path;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisOutputFile;
import ca.corefacility.bioinformatics.irida.repositories.filesystem.FilesystemSupplementedRepositoryImpl;

public class AnalysisOutputFileRepositoryImpl extends FilesystemSupplementedRepositoryImpl<AnalysisOutputFile> {

	@Autowired
	public AnalysisOutputFileRepositoryImpl(EntityManager entityManager,
			@Qualifier("outputFileBaseDirectory") Path baseDirectory) {
		super(entityManager, baseDirectory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnalysisOutputFile save(AnalysisOutputFile entity) {
		return super.saveInternal(entity);
	}

}
