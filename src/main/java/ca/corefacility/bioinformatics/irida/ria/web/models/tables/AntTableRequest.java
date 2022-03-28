package ca.corefacility.bioinformatics.irida.ria.web.models.tables;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

/**
 * Table request for an <a href="https://ant.design/components/table/">Ant Design UI Table</a>
 * <p>
 * Implements multi-column sort.
 */
public class AntTableRequest {
	private int pageSize;
	private int current;
	private List<AntSort> order;

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public Sort getSort() {
		if (order != null && order.size() > 0) {
			return Sort.by(order.stream().map(AntSort::getOrder).collect(Collectors.toList()));
		}
		return Sort.unsorted();
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getCurrent() {
		// Returning -1 since ant sends at index 1;
		return current - 1;
	}

	public void setOrder(List<AntSort> order) {
		this.order = order;
	}
}
