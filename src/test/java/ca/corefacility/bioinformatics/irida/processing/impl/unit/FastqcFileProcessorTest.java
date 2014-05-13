package ca.corefacility.bioinformatics.irida.processing.impl.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.corefacility.bioinformatics.irida.model.OverrepresentedSequence;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.processing.FileProcessorException;
import ca.corefacility.bioinformatics.irida.processing.impl.FastqcFileProcessor;
import ca.corefacility.bioinformatics.irida.repositories.OverrepresentedSequenceRepository;
import ca.corefacility.bioinformatics.irida.repositories.SequenceFileRepository;

/**
 * Tests for {@link FastqcFileProcessor}.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * 
 */
public class FastqcFileProcessorTest {
	private FastqcFileProcessor fileProcessor;
	private SequenceFileRepository sequenceFileRepository;
	private OverrepresentedSequenceRepository overrepresentedSequenceRepository;
	private static final Logger logger = LoggerFactory.getLogger(FastqcFileProcessorTest.class);

	private static final String SEQUENCE = "ACGTACGTN";
	private static final String FASTQ_FILE_CONTENTS = "@testread\n" + SEQUENCE + "\n+\n?????????\n@testread2\n"
			+ SEQUENCE + "\n+\n?????????";
	private static final String FASTA_FILE_CONTENTS = ">test read\n" + SEQUENCE;

	@Before
	public void setUp() {
		sequenceFileRepository = mock(SequenceFileRepository.class);
		overrepresentedSequenceRepository = mock(OverrepresentedSequenceRepository.class);
		fileProcessor = new FastqcFileProcessor(sequenceFileRepository, overrepresentedSequenceRepository);
	}

	@Test(expected = FileProcessorException.class)
	public void testHandleFastaFile() throws IOException {
		// fastqc fails to handle fasta files (there's no quality scores,
		// dummy), but that's A-OK.
		Path fasta = Files.createTempFile(null, null);
		Files.write(fasta, FASTA_FILE_CONTENTS.getBytes());
		SequenceFile sf = new SequenceFile(fasta);
		Runtime.getRuntime().addShutdownHook(new DeleteFileOnExit(fasta));
		when(sequenceFileRepository.findOne(any())).thenReturn(sf);

		fileProcessor.process(sf);
	}

	@Test
	public void testHandleFastqFile() throws IOException {
		// fastqc shouldn't barf on a fastq file.
		Path fastq = Files.createTempFile(null, null);
		Files.write(fastq, FASTQ_FILE_CONTENTS.getBytes());
		Runtime.getRuntime().addShutdownHook(new DeleteFileOnExit(fastq));

		OverrepresentedSequence ovrs = new OverrepresentedSequence(SEQUENCE, 2, BigDecimal.valueOf(100.), "");
		when(overrepresentedSequenceRepository.save(any(OverrepresentedSequence.class))).thenReturn(ovrs);
		ArgumentCaptor<SequenceFile> argument = ArgumentCaptor.forClass(SequenceFile.class);

		SequenceFile sf = new SequenceFile(fastq);
		when(sequenceFileRepository.findOne(any())).thenReturn(sf);
		sf.setId(1L);
		try {
			fileProcessor.process(sf);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		verify(sequenceFileRepository).save(argument.capture());
		SequenceFile updated = argument.getValue();
		assertEquals("GC Content was not set correctly.", Short.valueOf((short) 50), updated.getGcContent());
		assertEquals("Filtered sequences was not 0.", Integer.valueOf(0), updated.getFilteredSequences());
		assertEquals("File type was not correct.", "Conventional base calls", updated.getFileType());
		assertEquals("Max length was not correct.", Integer.valueOf(SEQUENCE.length()), updated.getMaxLength());
		assertEquals("Min length was not correct.", Integer.valueOf(SEQUENCE.length()), updated.getMinLength());
		assertEquals("Total sequences was not correct.", Integer.valueOf(2), updated.getTotalSequences());
		assertEquals("Encoding was not correct.", "Illumina <1.3", updated.getEncoding());
		assertEquals("Total number of bases was not correct.", Long.valueOf(SEQUENCE.length() * 2),
				updated.getTotalBases());

		assertNotNull("Per-base quality score chart was not created.", updated.getPerBaseQualityScoreChart());
		assertTrue("Per-base quality score chart was created, but was empty.",
				((byte[]) updated.getPerBaseQualityScoreChart()).length > 0);

		assertNotNull("Per-sequence quality score chart was not created.", updated.getPerSequenceQualityScoreChart());
		assertTrue("Per-sequence quality score chart was created, but was empty.",
				((byte[]) updated.getPerSequenceQualityScoreChart()).length > 0);

		assertNotNull("Duplication level chart was not created.", updated.getDuplicationLevelChart());
		assertTrue("Duplication level chart was not created.", ((byte[]) updated.getDuplicationLevelChart()).length > 0);

		ArgumentCaptor<OverrepresentedSequence> overrepresentedSequenceCaptor = ArgumentCaptor
				.forClass(OverrepresentedSequence.class);

		verify(overrepresentedSequenceRepository).save(overrepresentedSequenceCaptor.capture());
		OverrepresentedSequence overrepresentedSequence = overrepresentedSequenceCaptor.getValue();
		assertEquals("Sequence was not the correct sequence.", SEQUENCE, overrepresentedSequence.getSequence());
		assertEquals("The count was not correct.", 2, overrepresentedSequence.getOverrepresentedSequenceCount());
		assertEquals("The percent was not correct.", BigDecimal.valueOf(100.), overrepresentedSequence.getPercentage());

	}

	private static final class DeleteFileOnExit extends Thread {

		private final Path fileToDelete;

		public DeleteFileOnExit(Path fileToDelete) {
			this.fileToDelete = fileToDelete;
		}

		@Override
		public void run() {
			try {
				Files.deleteIfExists(fileToDelete);
			} catch (IOException e) {
				logger.debug("Couldn't delete path ["
						+ fileToDelete
						+ "]. This should be safe to ignore; FastQC opens an input stream on the file and never closes it.");
			}
		}

	}
}
