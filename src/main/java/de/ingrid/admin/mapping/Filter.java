package de.ingrid.admin.mapping;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class Filter implements Externalizable {

	public static enum FilterType {
        GREATER_THAN, LOWER_THAN, CONTAINS, NOT_CONTAINS, EQUAL, NOT_EQUAL, BEFORE, AFTER
	}

	private Comparable<? extends Serializable> _expression;

	private FilterType _filterType = FilterType.GREATER_THAN;

	public Filter() {
		// externalizable
	}

	public Filter(Comparable<? extends Serializable> expression,
			FilterType filterType) {
		_expression = expression;
		_filterType = filterType;
	}

	public Comparable<? extends Serializable> getExpression() {
		return _expression;
	}

	public FilterType getFilterType() {
		return _filterType;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		_filterType = FilterType.valueOf(in.readUTF());
		_expression = (Comparable<? extends Serializable>) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(_filterType.name());
		out.writeObject(_expression);
	}

}
