package ca.corefacility.bioinformatics.irida.service.remote.model.resource;

/**
 * Object wrapping a list of returned resources from a remote IRIDA API. This
 * type will be returned when listing objects from the API. Example: "/projects"
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 * @param <Type>
 *            The type of object being stored in the list extends
 *            {@link RemoteResource}
 */
public class ListResourceWrapper<Type extends RemoteResource> {
	private ResourceList<Type> resource;

	public ResourceList<Type> getResource() {
		return resource;
	}

	public void setResource(ResourceList<Type> resource) {
		this.resource = resource;
	}
}
