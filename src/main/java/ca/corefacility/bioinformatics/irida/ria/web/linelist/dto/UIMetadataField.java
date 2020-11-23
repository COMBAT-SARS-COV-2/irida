package ca.corefacility.bioinformatics.irida.ria.web.linelist.dto;

/**
 * This is a generic class to represent all possible headers (MetadataFields)
 * in a line list.
 */
public class UIMetadataField  {
	private final boolean editable;
	private final Long id;
	private final String title;
	private final String type;
	private final String key;
	private final String dataIndex;


	public UIMetadataField(Long id, String label, String type, String dataIndex, boolean editable) {
		this.id = id;
		this.title = label;
		this.type = type;
		this.editable = editable;
		this.key = "field-" + id;
		this.dataIndex = dataIndex;
	}

	public boolean isEditable() {
		return editable;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	public String getDataIndex() {
		return dataIndex;
	}
}
