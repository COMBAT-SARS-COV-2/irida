package ca.corefacility.bioinformatics.irida.service.impl.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.repository.PagingAndSortingRepository;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.InvalidPropertyException;
import ca.corefacility.bioinformatics.irida.service.CRUDService;
import ca.corefacility.bioinformatics.irida.service.impl.CRUDServiceImpl;
import ca.corefacility.bioinformatics.irida.utils.model.IdentifiableTestEntity;

/**
 * Testing the behavior of {@link CRUDServiceImpl}
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class CRUDServiceImplTest {

	private CRUDService<Long, IdentifiableTestEntity> crudService;
	private PagingAndSortingRepository<IdentifiableTestEntity, Long> crudRepository;
	private Validator validator;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		crudRepository = mock(PagingAndSortingRepository.class);
		crudService = new CRUDServiceImpl<>(crudRepository, validator, IdentifiableTestEntity.class);
	}

	@Test
	public void testAddValidObject() {
		IdentifiableTestEntity i = new IdentifiableTestEntity();
		i.setNonNull("Definitely not null.");
		i.setLabel("labelled");

		try {
			crudService.create(i);
		} catch (ConstraintViolationException constraintViolations) {
			fail();
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdateMissingEntity() {
		Long id = new Long(1);
		Map<String, Object> updatedProperties = new HashMap<>();
		when(crudRepository.exists(id)).thenReturn(Boolean.FALSE);

		crudService.update(id, updatedProperties);
	}

	@Test
	public void testUpdateWithBadPropertyName() {
		IdentifiableTestEntity entity = new IdentifiableTestEntity();
		entity.setId(1l);
		Map<String, Object> updatedProperties = new HashMap<>();
		updatedProperties.put("noSuchField", new Object());
		when(crudRepository.findOne(1l)).thenReturn(entity);

		try{
			crudService.update(entity.getId(), updatedProperties);
			fail();
		}catch(InvalidPropertyException ex){
			assertNotNull(ex.getAffectedClass());
		}
	}

	@Test
	public void testUpdateWithBadPropertyType() {
		IdentifiableTestEntity entity = new IdentifiableTestEntity();
		entity.setId(new Long(1));
		Map<String, Object> updatedProperties = new HashMap<>();
		updatedProperties.put("integerValue", new Object());
		when(crudRepository.findOne(1l)).thenReturn(entity);

		try{
			crudService.update(entity.getId(), updatedProperties);
			fail();
		}catch(InvalidPropertyException ex){
			assertNotNull(ex.getAffectedClass());
		}
	}

	@Test
	public void testUpdateInvalidEntry() {
		IdentifiableTestEntity i = new IdentifiableTestEntity();
		i.setNonNull("Definitely not null.");
		i.setIntegerValue(Integer.MIN_VALUE);
		Long id = new Long(1);
		i.setId(id);
		when(crudRepository.exists(id)).thenReturn(Boolean.TRUE);
		when(crudRepository.findOne(id)).thenReturn(i);

		Map<String, Object> updatedFields = new HashMap<>();
		updatedFields.put("nonNull", null);
		try {
			crudService.update(id, updatedFields);
			fail();
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			assertEquals(1, violations.size());
			ConstraintViolation<?> v = violations.iterator().next();
			assertEquals("nonNull", v.getPropertyPath().toString());
		}
	}

	@Test
	public void testRead() {
		IdentifiableTestEntity i = new IdentifiableTestEntity();
		i.setId(new Long(1));
		i.setNonNull("Definitely not null");

		when(crudRepository.findOne(i.getId())).thenReturn(i);

		try {
			i = crudService.read(i.getId());
			assertNotNull(i);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	@Test
	public void testList() {
		int itemCount = 10;
		List<IdentifiableTestEntity> entities = new ArrayList<>();
		for (int i = 0; i < itemCount; i++) {
			entities.add(new IdentifiableTestEntity());
		}
		when(crudRepository.findAll()).thenReturn(entities);

		Iterable<IdentifiableTestEntity> items = crudService.findAll();

		assertEquals(entities, items);
	}

	@Test
	public void testExists() {
		IdentifiableTestEntity i = new IdentifiableTestEntity();
		i.setId(new Long(1));
		when(crudRepository.exists(i.getId())).thenReturn(Boolean.TRUE);
		assertTrue(crudService.exists(i.getId()));
	}

	@Test
	public void testValidDelete() {
		IdentifiableTestEntity i = new IdentifiableTestEntity();
		i.setId(new Long(1));

		when(crudService.exists(i.getId())).thenReturn(Boolean.TRUE);

		try {
			crudService.delete(i.getId());
		} catch (EntityNotFoundException e) {
			fail();
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testInvalidDelete() {
		Long id = new Long(1);
		when(crudRepository.exists(id)).thenReturn(Boolean.FALSE);

		crudService.delete(id);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetMissingEntity() {
		Long id = new Long(1);
		when(crudRepository.findOne(id)).thenReturn(null);

		crudService.read(id);
	}

	@Test
	public void testCount() {
		long count = 30;
		when(crudRepository.count()).thenReturn(count);
		assertEquals(count, crudService.count());
	}
}
