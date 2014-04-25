package ca.corefacility.bioinformatics.irida.service;

import org.springframework.security.access.prepost.PreAuthorize;

import ca.corefacility.bioinformatics.irida.model.RemoteAPI;

/**
 * Service for storing and retrieving {@link RemoteAPIs}
 * @author "Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>"
 *
 */
public interface RemoteAPIService extends CRUDService<Long, RemoteAPI> {

	@Override
	@PreAuthorize("permitAll")
	public RemoteAPI read(Long id);
	
	@Override
	@PreAuthorize("permitAll")
	public Iterable<RemoteAPI> findAll();
		
}
