package ca.corefacility.bioinformatics.irida.processing.impl;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import uk.ac.babraham.FastQC.Graphs.LineGraph;
import uk.ac.babraham.FastQC.Graphs.QualityBoxPlot;
import uk.ac.babraham.FastQC.Modules.BasicStats;
import uk.ac.babraham.FastQC.Modules.DuplicationLevel;
import uk.ac.babraham.FastQC.Modules.OverRepresentedSeqs;
import uk.ac.babraham.FastQC.Modules.OverRepresentedSeqs.OverrepresentedSeq;
import uk.ac.babraham.FastQC.Modules.PerBaseQualityScores;
import uk.ac.babraham.FastQC.Modules.PerSequenceQualityScores;
import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFactory;
import uk.ac.babraham.FastQC.Sequence.QualityEncoding.PhredEncoding;
import ca.corefacility.bioinformatics.irida.model.OverrepresentedSequence;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisFastQC;
import ca.corefacility.bioinformatics.irida.processing.FileProcessor;
import ca.corefacility.bioinformatics.irida.processing.FileProcessorException;
import ca.corefacility.bioinformatics.irida.repositories.AnalysisRepository;

import com.google.common.collect.ImmutableSet;

/**
 * Executes FastQC on a {@link SequenceFile} and stores the report in the
 * database. This is a terrible, ugly, hacky class because most of the internal
 * statistics computed by FastQC are <code>private</code> fields, so we reflect
 * on those fields and make them <code>public</code> to get the values.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * 
 */
public class FastqcFileProcessor implements FileProcessor {
	private static final Logger logger = LoggerFactory.getLogger(FastqcFileProcessor.class);

	private final AnalysisRepository analysisRepository;
	private final MessageSource messageSource;

	public FastqcFileProcessor(AnalysisRepository analysisRepository, MessageSource messageSource) {
		this.analysisRepository = analysisRepository;
		this.messageSource = messageSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SequenceFile process(final SequenceFile sequenceFile) throws FileProcessorException {
		Path fileToProcess = sequenceFile.getFile();
		AnalysisFastQC analysis = new AnalysisFastQC(ImmutableSet.of(sequenceFile));
		try {
			uk.ac.babraham.FastQC.Sequence.SequenceFile fastQCSequenceFile = SequenceFactory
					.getSequenceFile(fileToProcess.toFile());
			BasicStats basicStats = new BasicStats();
			PerBaseQualityScores pbqs = new PerBaseQualityScores();
			PerSequenceQualityScores psqs = new PerSequenceQualityScores();
			OverRepresentedSeqs overRep = new OverRepresentedSeqs();
			QCModule[] moduleList = new QCModule[] { basicStats, pbqs, psqs, overRep };

			logger.debug("Launching FastQC analysis modules on all sequences.");
			while (fastQCSequenceFile.hasNext()) {
				Sequence sequence = fastQCSequenceFile.next();
				for (QCModule module : moduleList) {
					module.processSequence(sequence);
				}
			}

			logger.debug("Finished FastQC analysis modules.");
			handleBasicStats(basicStats, analysis);
			handlePerBaseQualityScores(pbqs, analysis);
			handlePerSequenceQualityScores(psqs, analysis);
			handleDuplicationLevel(overRep.duplicationLevelModule(), analysis);
			Set<OverrepresentedSequence> overrepresentedSequences = handleOverRepresentedSequences(overRep);

			logger.trace("Saving FastQC analysis.");
			analysis.setOverrepresentedSequences(overrepresentedSequences);
			analysis.setDescription(messageSource.getMessage("fastqc.file.processor.analysis.description", null,
					LocaleContextHolder.getLocale()));
			analysis.setExecutionManagerAnalysisId("internal-fastqc");

			analysisRepository.save(analysis);
		} catch (Exception e) {
			logger.error("FastQC failed to process the sequence file. Stack trace follows.", e);
			throw new FileProcessorException("FastQC failed to parse the sequence file.");
		}
		return sequenceFile;
	}

	/**
	 * Handle writing the {@link BasicStats} to the database.
	 * 
	 * @param stats
	 *            the {@link BasicStats} computed by fastqc.
	 * @param analysis
	 *            the {@link Analysis} to update.
	 */
	private void handleBasicStats(BasicStats stats, AnalysisFastQC analysis) {
		analysis.setFileType(stats.getFileType());
		analysis.setEncoding(PhredEncoding.getFastQEncodingOffset(stats.getLowestChar()).name());
		analysis.setMinLength(stats.getMinLength());
		analysis.setMaxLength(stats.getMaxLength());
		analysis.setTotalSequences(stats.getActualCount());
		analysis.setFilteredSequences(stats.getFilteredCount());
		analysis.setGcContent(stats.getGCContent());
		analysis.setTotalBases(stats.getACount() + stats.getGCount() + stats.getCCount() + stats.getTCount()
				+ stats.getNCount());
	}

	/**
	 * Handle writing the {@link PerBaseQualityScores} to the database.
	 * 
	 * @param scores
	 *            the {@link PerBaseQualityScores} computed by fastqc.
	 * @param analysis
	 *            the {@link Analysis} to update.
	 */
	private void handlePerBaseQualityScores(PerBaseQualityScores scores, AnalysisFastQC analysis) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		QualityBoxPlot bp = (QualityBoxPlot) scores.getResultsPanel();
		BufferedImage b = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics g = b.getGraphics();
		bp.paint(g, b.getWidth(), b.getHeight());

		ImageIO.write(b, "PNG", os);
		byte[] image = os.toByteArray();
		analysis.setPerBaseQualityScoreChart(image);
	}

	/**
	 * Handle writing the {@link PerSequenceQualityScores} to the database.
	 * 
	 * @param scores
	 *            the {@link PerSequenceQualityScores} computed by fastqc.
	 * @param analysis
	 *            the {@link Analysis} to update.
	 */
	private void handlePerSequenceQualityScores(PerSequenceQualityScores scores, AnalysisFastQC analysis)
			throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		LineGraph lg = (LineGraph) scores.getResultsPanel();
		BufferedImage b = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics g = b.getGraphics();
		lg.paint(g, b.getWidth(), b.getHeight());

		ImageIO.write(b, "PNG", os);
		byte[] image = os.toByteArray();
		analysis.setPerSequenceQualityScoreChart(image);
	}

	/**
	 * Handle writing the {@link DuplicationLevel} to the database.
	 * 
	 * @param duplicationLevel
	 *            the {@link DuplicationLevel} calculated by fastqc.
	 * @param analysis
	 *            the {@link SequenceFile} to update.
	 */
	private void handleDuplicationLevel(DuplicationLevel duplicationLevel, AnalysisFastQC analysis) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		LineGraph lg = (LineGraph) duplicationLevel.getResultsPanel();
		BufferedImage b = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		Graphics g = b.getGraphics();
		lg.paint(g, b.getWidth(), b.getHeight());

		ImageIO.write(b, "PNG", os);
		byte[] image = os.toByteArray();
		analysis.setDuplicationLevelChart(image);
	}

	/**
	 * Handle getting over represented sequences from fastqc.
	 * 
	 * @param seqs
	 *            overrepresented sequences.
	 * @return a collection of {@link OverrepresentedSequence} corresponding to
	 *         the FastQC {@link OverRepresentedSeqs}.
	 */
	private Set<OverrepresentedSequence> handleOverRepresentedSequences(OverRepresentedSeqs seqs) {

		OverrepresentedSeq[] sequences = seqs.getOverrepresentedSequences();
		if (sequences == null) {
			return Collections.emptySet();
		}

		Set<OverrepresentedSequence> overrepresentedSequences = new HashSet<>(sequences.length);

		for (OverrepresentedSeq s : sequences) {
			String sequenceString = s.seq();
			int count = s.count();
			BigDecimal percent = BigDecimal.valueOf(s.percentage());
			String possibleSource = s.contaminantHit();

			overrepresentedSequences.add(new OverrepresentedSequence(sequenceString, count, percent, possibleSource));
		}
		return overrepresentedSequences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean modifiesFile() {
		return false;
	}
}
