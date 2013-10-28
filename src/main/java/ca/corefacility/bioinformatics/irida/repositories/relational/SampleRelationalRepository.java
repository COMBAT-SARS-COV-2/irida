package ca.corefacility.bioinformatics.irida.repositories.relational;

import java.util.List;

import javax.sql.DataSource;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Sample;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.joins.impl.SampleSequenceFileJoin;
import ca.corefacility.bioinformatics.irida.repositories.SampleRepository;

/**
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
@Repository
@Transactional
public class SampleRelationalRepository extends GenericRelationalRepository<Sample> implements SampleRepository {

	public SampleRelationalRepository() {
	}

	public SampleRelationalRepository(DataSource source, SessionFactory sessionFactory) {
		super(source, sessionFactory, Sample.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ProjectSampleJoin> getSamplesForProject(Project project) {
		Session session = sessionFactory.getCurrentSession();

		Criteria crit = session.createCriteria(ProjectSampleJoin.class);
		crit.add(Restrictions.eq("project", project));
		@SuppressWarnings("unchecked")
		List<ProjectSampleJoin> list = crit.list();

		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sample getSampleByExternalSampleId(Project p, String sampleId) {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createQuery("select ps.sample from ProjectSampleJoin ps where "
				+ "ps.id = :projectId and ps.sample.externalSampleId = :externalSampleId");
		q.setLong("projectId", p.getId());
		q.setString("externalSampleId", sampleId);
		
		Sample sample = (Sample) q.uniqueResult();
		if (sample == null) {
			throw new EntityNotFoundException("Sample with id " + sampleId + " not found");
		}

		return sample;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SampleSequenceFileJoin getSampleForSequenceFile(SequenceFile sf) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(SampleSequenceFileJoin.class);
		crit.add(Restrictions.eq("sequenceFile", sf));

		return (SampleSequenceFileJoin) crit.uniqueResult();
	}

}
