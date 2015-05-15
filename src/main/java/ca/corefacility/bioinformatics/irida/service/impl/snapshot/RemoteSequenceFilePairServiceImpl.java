package ca.corefacility.bioinformatics.irida.service.impl.snapshot;

import java.util.Iterator;
import java.util.Set;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.corefacility.bioinformatics.irida.model.sequenceFile.RemoteSequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.RemoteSequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.repositories.sequencefile.RemoteSequenceFilePairRepository;
import ca.corefacility.bioinformatics.irida.repositories.sequencefile.RemoteSequenceFileRepository;
import ca.corefacility.bioinformatics.irida.service.impl.CRUDServiceImpl;
import ca.corefacility.bioinformatics.irida.service.snapshot.RemoteSequenceFilePairService;

/**
 * {@link CRUDServiceImpl} implementation of
 * {@link RemoteSequenceFilePairService}
 */
@Service
public class RemoteSequenceFilePairServiceImpl extends CRUDServiceImpl<Long, RemoteSequenceFilePair> implements
		RemoteSequenceFilePairService {

	private RemoteSequenceFileRepository fileRepository;

	@Autowired
	public RemoteSequenceFilePairServiceImpl(RemoteSequenceFilePairRepository repository,
			RemoteSequenceFileRepository fileRepository, Validator validator) {
		super(repository, validator, RemoteSequenceFilePair.class);
		this.fileRepository = fileRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteSequenceFilePair mirrorPair(SequenceFilePair pair) {
		Set<SequenceFile> files = pair.getFiles();

		Iterator<SequenceFile> filesIterator = files.iterator();
		RemoteSequenceFile f1 = new RemoteSequenceFile(filesIterator.next());
		RemoteSequenceFile f2 = new RemoteSequenceFile(filesIterator.next());

		f1 = fileRepository.save(f1);
		f2 = fileRepository.save(f2);

		RemoteSequenceFilePair remoteSequenceFilePair = new RemoteSequenceFilePair(f1, f2);

		return create(remoteSequenceFilePair);
	}

}
